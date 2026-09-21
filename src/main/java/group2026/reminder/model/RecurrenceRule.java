package group2026.reminder.model;

import java.util.Locale;

public record RecurrenceRule(
        Frequency frequency,
        long interval
) {

    public enum Frequency {
        DAILY("DAILY"),
        WEEKLY("WEEKLY"),
        MONTHLY("MONTHLY"),
        YEARLY("YEARLY");

        private final String rrule;

        Frequency(String rrule) {
            this.rrule = rrule;
        }
    }

    public static RecurrenceRule of(Frequency frequency, long interval) {
        return new RecurrenceRule(frequency, interval);
    }

    /**
     * RRULE-строка для хранения и обработки, например FREQ=WEEKLY;INTERVAL=1.
     */
    public String toRrule() {
        return "FREQ=" + frequency.rrule + ";INTERVAL=" + interval;
    }

    public static RecurrenceRule fromRrule(String rrule) {
        String freqPart = "FREQ";
        long interval = 1;

        for (String part : rrule.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].toUpperCase(Locale.ROOT);
            String value = kv[1].toUpperCase(Locale.ROOT);
            if (key.equals("FREQ")) {
                freqPart = value;
            } else if (key.equals("INTERVAL")) {
                interval = Long.parseLong(value);
            }
        }

        Frequency frequency = switch (freqPart) {
            case "DAILY" -> Frequency.DAILY;
            case "WEEKLY" -> Frequency.WEEKLY;
            case "MONTHLY" -> Frequency.MONTHLY;
            case "YEARLY" -> Frequency.YEARLY;
            default -> throw new IllegalArgumentException("Неизвестная частота RRULE: " + freqPart);
        };

        return new RecurrenceRule(frequency, interval);
    }

    public String toRussian() {
        return switch (frequency) {
            case DAILY -> interval == 1 ? "каждый день" : "каждые " + interval + " дн";
            case WEEKLY -> interval == 1 ? "раз в неделю" : "раз в " + interval + " нед";
            case MONTHLY -> interval == 1 ? "раз в месяц" : "раз в " + interval + " мес";
            case YEARLY -> interval == 1 ? "раз в год" : "раз в " + interval + " г";
        };
    }
}