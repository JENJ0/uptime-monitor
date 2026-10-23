package dev.portfolio.uptimemonitor.check;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;




@RestController 
@RequestMapping("/api/monitors/{monitorId}/checks")   // {monitorId} = variable piece of the URL
public class CheckRunController {
    private final MonitorCheckService monitorCheckService;

    public CheckRunController(MonitorCheckService monitorCheckService) {
        this.monitorCheckService = monitorCheckService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckRunResponse run(@PathVariable long monitorId) {
        CheckRun checkRun = monitorCheckService.runCheck(monitorId);
        return CheckRunResponse.from(checkRun);
    }
    
}
