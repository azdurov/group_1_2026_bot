package group2026.cozylife;

import group2026.config.TimeZones;
import group2026.http.ApiRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CozyLifeClient {

    private static final String DEVICE_LIST_PATH = "/v2/app/device_with_group/list";
    private static final String DEVICE_STATES_PATH = "/app/v2/device/states";
    private static final String STATISTICS_PATH = "/app/statistic/dpid_log_group_by_hour";

    private static final String TOKEN_PARAM = "token";
    private static final String COUNT_PARAM = "count";
    private static final String PAGE_PARAM = "page";
    private static final String DEVICE_IDS_PARAM = "device_ids[]";
    private static final String DPIDS_PARAM = "dpids[]";
    private static final String DEVICE_ID_PARAM = "device_id";
    private static final String START_TS_PARAM = "start_timestamp";
    private static final String END_TS_PARAM = "end_timestamp";

    private static final String COUNT = "100";
    private static final String PAGE = "1";
    private static final long SECONDS_PER_HOUR = 3600;

    private final CozyLifeHttpClient httpClient;
    private final CozyLifeTokenProvider tokenProvider;

    /**
     * Cached device list; devices are not expected to change.
     */
    private volatile List<CozyLifeResponse.DeviceList.Device> devicesCache;

    public List<CozyLifeTemperatureSensor> getTemperatureSensors() {
        List<CozyLifeResponse.DeviceList.Device> devices = getDevices();
        if (devices.isEmpty()) {
            return List.of();
        }

        Map<String, CozyLifeResponse.DeviceStates.DeviceState> states = getStates(devices);

        return devices.stream()
                .map(device -> CozyLifeTemperatureSensor.from(
                        device, states.get(device.device_id())))
                .toList();
    }

    private List<CozyLifeResponse.DeviceList.Device> getDevices() {
        if (devicesCache != null) {
            return devicesCache;
        }
        List<CozyLifeResponse.DeviceList.Device> devices = fetchDevices();
        devicesCache = devices;
        return devices;
    }

    private List<CozyLifeResponse.DeviceList.Device> fetchDevices() {
        Map<String, List<String>> query = authQuery();
        query.put(COUNT_PARAM, List.of(COUNT));
        query.put(PAGE_PARAM, List.of(PAGE));

        CozyLifeResponse.DeviceList response =
                getWithTokenRetry(DEVICE_LIST_PATH, query, CozyLifeResponse.DeviceList.class);

        if (response.info() == null
                || response.info().device_bind() == null
                || response.info().device_bind().list() == null) {
            return List.of();
        }
        return response.info().device_bind().list();
    }

    private Map<String, List<String>> authQuery() {
        Map<String, List<String>> query = new LinkedHashMap<>();
        query.put(TOKEN_PARAM, List.of(tokenProvider.getToken()));
        return query;
    }

    private <T> T getWithTokenRetry(String path, Map<String, List<String>> query, Class<T> type) {
        try {
            return httpClient.get(path, query, type);
        } catch (ApiRequestException e) {
            if (!e.isUnauthorized()) {
                throw e;
            }
            tokenProvider.invalidate();
            query.put(TOKEN_PARAM, List.of(tokenProvider.getToken()));
            return httpClient.get(path, query, type);
        }
    }

    private Map<String, CozyLifeResponse.DeviceStates.DeviceState> getStates(
            List<CozyLifeResponse.DeviceList.Device> devices
    ) {
        Map<String, List<String>> query = authQuery();
        query.put(DEVICE_IDS_PARAM, devices.stream()
                .map(CozyLifeResponse.DeviceList.Device::device_id)
                .toList());

        CozyLifeResponse.DeviceStates response =
                getWithTokenRetry(DEVICE_STATES_PATH, query, CozyLifeResponse.DeviceStates.class);

        Map<String, CozyLifeResponse.DeviceStates.DeviceState> result = new LinkedHashMap<>();
        if (response.info() != null) {
            for (CozyLifeResponse.DeviceStates.StateItem item : response.info()) {
                if (item.device_id() != null) {
                    result.put(item.device_id(), item.state());
                }
            }
        }
        return result;
    }

    public List<CozyLifeHistoryPoint> getTemperatureHistory(String deviceId, LocalDateTime from, LocalDateTime to) {
        return getStatistics(deviceId, from, to, DeviceStateId.TEMPERATURE);
    }

    public List<CozyLifeHistoryPoint> getHumidityHistory(String deviceId, LocalDateTime from, LocalDateTime to) {
        return getStatistics(deviceId, from, to, DeviceStateId.HUMIDITY);
    }

    private List<CozyLifeHistoryPoint> getStatistics(
            String deviceId, LocalDateTime from, LocalDateTime to, DeviceStateId state
    ) {
        Map<String, List<String>> query = authQuery();
        query.put(DPIDS_PARAM, List.of(state.apiId()));
        query.put(DEVICE_ID_PARAM, List.of(deviceId));
        query.put(START_TS_PARAM, List.of(String.valueOf(toEpochSecond(from))));
        query.put(END_TS_PARAM, List.of(String.valueOf(toEpochSecond(to))));

        CozyLifeResponse.Statistics response =
                getWithTokenRetry(STATISTICS_PATH, query, CozyLifeResponse.Statistics.class);

        if (response.info() == null || response.info().result() == null) {
            return List.of();
        }

        return response.info().result().getOrDefault(state.apiId(), List.of()).stream()
                .map(item -> new CozyLifeHistoryPoint(
                        Instant.ofEpochSecond(item.hour() * SECONDS_PER_HOUR)
                                .atZone(TimeZones.DEFAULT).toLocalDateTime(),
                        item.avg() / state.divisor()))
                .toList();
    }

    private long toEpochSecond(LocalDateTime dateTime) {
        return dateTime.atZone(TimeZones.DEFAULT).toEpochSecond();
    }
}