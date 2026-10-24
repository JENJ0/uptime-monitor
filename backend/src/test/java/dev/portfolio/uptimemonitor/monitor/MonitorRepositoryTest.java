package dev.portfolio.uptimemonitor.monitor;

import dev.portfolio.uptimemonitor.check.CheckRun;
import dev.portfolio.uptimemonitor.check.CheckRunRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import dev.portfolio.uptimemonitor.TestcontainersConfiguration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.portfolio.uptimemonitor.CheckResult;




@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MonitorRepositoryTest {
    @Autowired private MonitorRepository monitorRepository;
    @Autowired private CheckRunRepository checkRunRepository;

    @Test
    void findDueMonitorsNoRecentCheck() {
        Instant now = Instant.now();
        Monitor neverChecked = monitorRepository.save(new Monitor("Never", "http://localhost:1", 60, 2000, 200));
        Monitor recentlyChecked = monitorRepository.save(new Monitor("Never", "http://localhost:1", 60, 2000, 200));
        Monitor longAgoChecked = monitorRepository.save(new Monitor("Never", "http://localhost:1", 60, 2000, 200));
        
        saveCheck(recentlyChecked, now.minusSeconds(5));
        saveCheck(longAgoChecked, now.minusSeconds(120));

        List<Long> dueIDs = monitorRepository.findDueMonitors(now).stream().map(Monitor::getId).toList();
        
        assertTrue(dueIDs.contains(neverChecked.getId()) && dueIDs.contains(longAgoChecked.getId()));
        assertFalse(dueIDs.contains(recentlyChecked.getId()));


    }   

    private void saveCheck(Monitor monitor, Instant checkedAt) {
        CheckResult result = new CheckResult(monitor.getUrl(), checkedAt, 100, 200, true, null);
        CheckRun checkRun = new CheckRun(monitor, result);
        checkRunRepository.save(checkRun);
    }
}