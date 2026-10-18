package dev.portfolio.uptimemonitor.check;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import dev.portfolio.uptimemonitor.CheckResult;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import java.time.Instant;

@Entity 
@Table(name = "check_results")
public class CheckRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id")
    private Monitor monitor;

    private Instant checkedAt;
    private long durationMs;
    private Integer httpStatus;
    private boolean successful;
    private String errorType;

    protected CheckRun() {}

    public CheckRun(Monitor monitor, CheckResult result) {
        this.monitor = monitor;
        this.checkedAt = result.checkedAt();
        this.durationMs = result.durationMs();
        this.httpStatus = result.httpStatus();
        this.successful = result.successful();
        this.errorType = result.errorType();
    }

    public Long getId() {
        return id;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorType() {
        return errorType;
    }
}

