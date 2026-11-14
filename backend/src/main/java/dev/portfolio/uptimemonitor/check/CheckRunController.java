package dev.portfolio.uptimemonitor.check;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;





@RestController 
@RequestMapping("/api/monitors/{monitorId}/checks")   // {monitorId} = variable piece of the URL
public class CheckRunController {
    private final MonitorCheckService monitorCheckService;
    private final CheckHistoryService checkHistoryService;

    public CheckRunController(MonitorCheckService monitorCheckService, CheckHistoryService checkHistoryService) {
        this.monitorCheckService = monitorCheckService;
        this.checkHistoryService = checkHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckRunResponse run(@PathVariable long monitorId) {
        CheckRun checkRun = monitorCheckService.runCheck(monitorId);
        return CheckRunResponse.from(checkRun);
    }

    @GetMapping
    public CheckHistoryResponse history(
        @PathVariable long monitorId, 
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return checkHistoryService.findHistory(monitorId, pageable);
    }
}
