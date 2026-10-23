package dev.portfolio.uptimemonitor.check;
import java.time.Instant;

public record CheckRunResponse(
    long id,
    long monitorId,
    Instant checkedAt,
    long durationMs,
    Integer httpStatus,
    boolean successful,
    String errorType
) {
    public static CheckRunResponse from(CheckRun run) {
        return new CheckRunResponse(
            run.getId(),
            run.getMonitor().getId(),
            run.getCheckedAt(),
            run.getDurationMs(),
            run.getHttpStatus(),
            run.isSuccessful(),
            run.getErrorType()
        );
    }
}