package dev.portfolio.uptimemonitor.check;

import org.springframework.stereotype.Service;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import dev.portfolio.uptimemonitor.monitor.MonitorNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class CheckHistoryService {
    private final CheckRunRepository checkRunRepository;
    private final MonitorRepository monitorRepository;

    public CheckHistoryService(CheckRunRepository checkRunRepository, MonitorRepository monitorRepository) {
        this.checkRunRepository = checkRunRepository;
        this.monitorRepository = monitorRepository;
    }

    @Transactional(readOnly = true)
    public CheckHistoryResponse findHistory(long monitorId, Pageable pageable) {
        Pageable pageOnly = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        if (!monitorRepository.existsById(monitorId)) {
            throw new MonitorNotFoundException(monitorId);
        }
        Page<CheckRun> page = checkRunRepository.findByMonitorIdOrderByCheckedAtDescIdDesc(monitorId, pageOnly);
        return CheckHistoryResponse.from(page);
    }
}
