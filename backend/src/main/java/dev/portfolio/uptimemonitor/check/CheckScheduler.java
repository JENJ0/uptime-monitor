package dev.portfolio.uptimemonitor.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.List;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import org.springframework.core.task.TaskRejectedException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "uptime.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class CheckScheduler {
    private static final Logger log = LoggerFactory.getLogger(CheckScheduler.class);
    
    private final MonitorRepository monitorRepository;
    private final MonitorCheckService monitorCheckService;
    private final ThreadPoolTaskExecutor checkExecutor;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

    public CheckScheduler(MonitorRepository monitorRepository, MonitorCheckService monitorCheckService, @Qualifier("checkExecutor") ThreadPoolTaskExecutor checkExecutor) {
        this.monitorRepository = monitorRepository;
        this.monitorCheckService = monitorCheckService;
        this.checkExecutor = checkExecutor;
    }

    @Scheduled(fixedDelayString = "PT5S")
    public void tick() {
        List<Monitor> due = monitorRepository.findDueMonitors(Instant.now());
        log.info("Scheduler tick: {} due monitors", due.size());
        for (Monitor monitor : due) {
            long id = monitor.getId();
            try {
                if (!inFlight.add(id)) { continue; } // Skip if already in flight
                checkExecutor.execute(() -> runSafely(id));
            } catch (TaskRejectedException e) {
                inFlight.remove(id);
                log.warn("queue is full, skipping check for monitor {}", id);
            }    
        }
    }

    private void runSafely(long monitorId) {
        try {
            monitorCheckService.runCheck(monitorId);
        } catch (RuntimeException e) {
            log.error("Error running check for monitor {}", monitorId, e);
        } finally {
            inFlight.remove(monitorId);
        }
    }
}

