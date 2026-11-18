package group2026.cozylife;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CozyLifeResponse {

    private CozyLifeResponse() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Login(
            String ret,
            String desc,
            LoginInfo info
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record LoginInfo(
                String token,
                String country_number_code,
                String phone,
                Long uid
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeviceList(
            String ret,
            DeviceListInfo info
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record DeviceListInfo(DeviceBind device_bind) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record DeviceBind(List<Device> list) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Device(
                String device_id,
                String device_name,
                String device_product_id
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeviceStates(
            String ret,
            List<StateItem> info
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StateItem(String device_id, DeviceState state) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record DeviceState(
                @JsonProperty("4") String humidity,
                @JsonProperty("8") String temperature,
                @JsonProperty("9") String battery,
                Boolean online
        ) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Statistics(
            String ret,
            StatisticsInfo info
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StatisticsInfo(Map<String, List<StatisticsItem>> result) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record StatisticsItem(
                @JsonProperty("_id") long id,
                double avg,
                long hour
        ) {
        }
    }
}