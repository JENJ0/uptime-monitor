package dev.portfolio.uptimemonitor.monitor;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitors")
public class Monitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Long instead of long to allow for null values before persistence
    private String name;
    private String url;
    private int intervalSeconds;
    private int timeoutMs;
    private int expectedStatus;
    private Instant createdAt;

    protected Monitor() {
    }

    public Monitor(String name, String url, int intervalSeconds, int timeoutMs, int expectedStatus) {
        this.name = name;
        this.url = url;
        this.intervalSeconds = intervalSeconds;
        this.timeoutMs = timeoutMs;
        this.expectedStatus = expectedStatus;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getExpectedStatus() {
        return expectedStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}