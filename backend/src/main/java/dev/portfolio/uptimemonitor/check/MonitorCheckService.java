package dev.portfolio.uptimemonitor.check;
import org.springframework.stereotype.Service;
import dev.portfolio.uptimemonitor.CheckService;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import dev.portfolio.uptimemonitor.CheckResult;
import dev.portfolio.uptimemonitor.monitor.MonitorNotFoundException;

@Service
public class MonitorCheckService {
    
    private final CheckRunRepository checkRunRepository;
    private final CheckService checkService;
    private final MonitorRepository monitorRepository;

    public MonitorCheckService(CheckRunRepository checkRunRepository, CheckService checkService, MonitorRepository monitorRepository) {
        this.checkRunRepository = checkRunRepository;
        this.checkService = checkService;
        this.monitorRepository = monitorRepository;
    }

    public CheckRun runCheck(long monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId)
        .orElseThrow(() -> new MonitorNotFoundException(monitorId));
        CheckResult checkResult = checkService.runCheckAgainst(monitor.getUrl(), monitor.getExpectedStatus());
        CheckRun checkRun = new CheckRun(monitor, checkResult);
        return checkRunRepository.save(checkRun);
    }
}


