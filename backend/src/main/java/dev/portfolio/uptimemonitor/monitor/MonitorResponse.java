package dev.portfolio.uptimemonitor.monitor;
import java.time.Instant;

public record MonitorResponse(
    Long id,
    String name,
    String url,
    int intervalSeconds,
    int timeoutMs,
    int expectedStatus,
    Instant createdAt
) {
    public static MonitorResponse from(Monitor monitor) {
        return new MonitorResponse(
            monitor.getId(),
            monitor.getName(),
            monitor.getUrl(),
            monitor.getIntervalSeconds(),
            monitor.getTimeoutMs(),
            monitor.getExpectedStatus(),
            monitor.getCreatedAt()
        );
    }
}
