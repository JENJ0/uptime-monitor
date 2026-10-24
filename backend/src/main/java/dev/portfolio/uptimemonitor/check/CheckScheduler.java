package dev.portfolio.uptimemonitor.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;


@Component
public class CheckScheduler {
    private static final Logger log = LoggerFactory.getLogger(CheckScheduler.class);
    
    private final MonitorRepository monitorRepository;
    public CheckScheduler(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    @Scheduled(fixedDelayString = "PT5S")
    public void tick() {
        log.info("Scheduler tick: {} due monitors", monitorRepository.findDueMonitors(Instant.now()).size());
    }
}
