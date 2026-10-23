package dev.portfolio.uptimemonitor.monitor;

public class MonitorNotFoundException extends RuntimeException {
    public MonitorNotFoundException(long monitorId) {
        super("Monitor with ID " + monitorId + " not found");
    }
}
