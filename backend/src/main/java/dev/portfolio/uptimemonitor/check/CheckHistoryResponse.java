package dev.portfolio.uptimemonitor.check;

import java.util.List;
import org.springframework.data.domain.Page;

public record CheckHistoryResponse(
    List<CheckRunResponse> items,
    int page,
    int size,
    long totalChecks,
    int totalPages
) {
    public static CheckHistoryResponse from(Page<CheckRun> page) {
        return new CheckHistoryResponse(
            page.getContent().stream().map(CheckRunResponse::from).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}