package dev.portfolio.uptimemonitor.check;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import dev.portfolio.uptimemonitor.TestcontainersConfiguration;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import java.util.List;
import org.springframework.data.domain.Page;

@SpringBootTest(properties = "uptime.scheduler.enabled=false")
@Import(TestcontainersConfiguration.class)
class CheckRunRepositoryTest {

    @Autowired private CheckRunRepository checkRunRepository;
    @Autowired private MonitorRepository monitorRepository;
    
    @Test
    void historyIsPagedNewestFirstAndOnlyForThatMonitor() {
        Instant now = Instant.now();
        Monitor history = monitorRepository.save(new Monitor("History", "http://localhost:1", 60, 2000, 200));
        Monitor other = monitorRepository.save(new Monitor("Other", "http://localhost:1", 60, 2000, 200));

        CheckRun oldest = saveCheck(history, now.minusSeconds(30));
        CheckRun middle = saveCheck(history, now.minusSeconds(20));
        CheckRun newest = saveCheck(history, now.minusSeconds(10));
        saveCheck(other, now);



        Page<CheckRun> first = checkRunRepository.findByMonitorIdOrderByCheckedAtDescIdDesc(history.getId(), PageRequest.of(0, 2));
        Page<CheckRun> second = checkRunRepository.findByMonitorIdOrderByCheckedAtDescIdDesc(history.getId(), PageRequest.of(1, 2));

        assertEquals(List.of(newest.getId(), middle.getId()), ids(first));
        assertEquals(List.of(oldest.getId()), ids(second));
        assertEquals(3, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
    }

    private CheckRun saveCheck(Monitor monitor, Instant checkedAt) {
        CheckResult result = new CheckResult(monitor.getUrl(), checkedAt, 100, 200, true, null);
        CheckRun checkRun = new CheckRun(monitor, result);
        return checkRunRepository.save(checkRun);
    }
    
    private List<Long> ids(Page<CheckRun> page) {
            return page.getContent().stream().map(CheckRun::getId).toList();
    }

}
