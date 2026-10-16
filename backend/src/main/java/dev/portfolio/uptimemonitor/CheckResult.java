package dev.portfolio.uptimemonitor;

import java.time.Instant;

public record CheckResult(
    String targetUrl,
    Instant checkedAt,
    long durationMs,
    Integer httpStatus,
    boolean successful,
    String errorType
) {
}
