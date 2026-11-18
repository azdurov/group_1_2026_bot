package group2026.cozylife;

import java.time.LocalDateTime;

public record CozyLifeHistoryPoint(
        LocalDateTime time,
        double value
) {
}