package group2026.cozylife;

/**
 * CozyLife device state identifiers and their raw-value divisors.
 */
public enum DeviceStateId {

    TEMPERATURE("8", 10),
    HUMIDITY("4", 1),
    BATTERY("9", 10);

    private final String apiId;
    private final int divisor;

    DeviceStateId(String apiId, int divisor) {
        this.apiId = apiId;
        this.divisor = divisor;
    }

    public String apiId() {
        return apiId;
    }

    public int divisor() {
        return divisor;
    }
}