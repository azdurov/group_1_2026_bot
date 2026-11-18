package group2026.cozylife;

import group2026.config.TimeZones;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CozyLifeService {

    private final CozyLifeClient client;

    public String getHistoryInfo() {
        List<CozyLifeTemperatureSensor> sensors = client.getTemperatureSensors();
        if (sensors.isEmpty()) {
            return "🌡 Температурные датчики CozyLife не найдены";
        }

        LocalDateTime to = LocalDateTime.now(TimeZones.DEFAULT);
        LocalDateTime from = to.minusHours(23);
        StringBuilder result = new StringBuilder(512);

        for (CozyLifeTemperatureSensor sensor : sensors) {
            List<CozyLifeHistoryPoint> temperature =
                    client.getTemperatureHistory(sensor.getDeviceId(), from, to);
            List<CozyLifeHistoryPoint> humidity =
                    client.getHumidityHistory(sensor.getDeviceId(), from, to);

            result.append(sensor.formatHistory(temperature, humidity)).append('\n');
        }

        return result.toString().strip();
    }
}