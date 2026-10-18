package dev.portfolio.uptimemonitor.monitor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {
    private final MonitorRepository monitorRepository;

    public MonitorController(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitorResponse create(@Valid @RequestBody CreateMonitorRequest request) {
        Monitor monitor = new Monitor(
            request.name(),
            request.url(),
            request.intervalSeconds(),
            request.timeoutMs(),
            request.expectedStatus()
        );
        Monitor saved = monitorRepository.save(monitor);
        return MonitorResponse.from(saved);
    }

    @GetMapping
    public List<MonitorResponse> findAll() {
        List<Monitor> monitors = monitorRepository.findAll();
        return monitors.stream().map(MonitorResponse::from).toList();
    }
    
}