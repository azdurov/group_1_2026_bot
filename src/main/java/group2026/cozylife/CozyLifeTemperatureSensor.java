package group2026.cozylife;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CozyLifeTemperatureSensor {

    private static final Locale LOCALE = Locale.forLanguageTag("ru");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String deviceId;
    private final String name;
    private final double temperature;
    private final double humidity;
    private final double battery;
    private final boolean online;

    public static CozyLifeTemperatureSensor from(
            CozyLifeResponse.DeviceList.Device device,
            CozyLifeResponse.DeviceStates.DeviceState state
    ) {
        if (state == null) {
            return new CozyLifeTemperatureSensor(device.device_id(), device.device_name(), 0, 0, 0, false);
        }

        return new CozyLifeTemperatureSensor(
                device.device_id(),
                device.device_name(),
                parseOrZero(state.temperature()) / DeviceStateId.TEMPERATURE.divisor(),
                parseOrZero(state.humidity()) / DeviceStateId.HUMIDITY.divisor(),
                parseOrZero(state.battery()) / DeviceStateId.BATTERY.divisor(),
                Boolean.TRUE.equals(state.online()));
    }

    public String formatHistory(List<CozyLifeHistoryPoint> temperature, List<CozyLifeHistoryPoint> humidity) {
        Map<LocalDateTime, Double> tempByTime = toMap(temperature);
        Map<LocalDateTime, Double> humByTime = toMap(humidity);

        Set<LocalDateTime> times = new TreeSet<>(Comparator.reverseOrder());
        times.addAll(tempByTime.keySet());
        times.addAll(humByTime.keySet());

        StringBuilder sb = new StringBuilder(256);
        sb.append(name).append(" — последние 24 часа\n")
                .append("🔋 ").append(String.format(LOCALE, "%.0f", battery)).append("% · ")
                .append(online ? "🟢 онлайн" : "🔴 офлайн").append('\n');

        for (LocalDateTime time : times) {
            sb.append(time.format(TIME_FORMAT))
                    .append("  ")
                    .append(formatTemperature(tempByTime.get(time)))
                    .append("  ")
                    .append(formatHumidity(humByTime.get(time)))
                    .append('\n');
        }
        return sb.toString().strip();
    }

    private static Map<LocalDateTime, Double> toMap(List<CozyLifeHistoryPoint> points) {
        return points.stream().collect(Collectors.toMap(
                CozyLifeHistoryPoint::time,
                CozyLifeHistoryPoint::value,
                (a, b) -> b));
    }

    private static String formatTemperature(Double value) {
        return value == null ? "     —" : String.format(LOCALE, "%5.1f°", value);
    }

    private static String formatHumidity(Double value) {
        return value == null ? "  —" : String.format(LOCALE, "%3.0f%%", value);
    }

    private static double parseOrZero(String value) {
        try {
            return value == null ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}