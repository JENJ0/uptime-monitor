package dev.portfolio.uptimemonitor.monitor;

public record CreateMonitorRequest(
    String name,
    String url,
    int intervalSeconds,
    int timeoutMs,
    int expectedStatus
) {
    
}
