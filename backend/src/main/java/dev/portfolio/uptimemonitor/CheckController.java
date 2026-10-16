package dev.portfolio.uptimemonitor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController 
@RequestMapping("/api/checks")
public class CheckController {
    private final CheckService checkService;

    public CheckController(CheckService checkService) {
        this.checkService = checkService;
    }

    @PostMapping
    public CheckResult runCheck() {
        return checkService.runCheck();
    }
}
