package dev.portfolio.uptimemonitor.check;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.InetSocketAddress;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.sun.net.httpserver.HttpServer;

import dev.portfolio.uptimemonitor.TestcontainersConfiguration;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;

@SpringBootTest(properties = "uptime.scheduler.enabled=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CheckRunApiTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private CheckRunRepository checkRunRepository;
    @Autowired private MonitorRepository monitorRepository;

    private HttpServer server;
    
    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void unknownMonitorReturns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/monitors/999999/checks"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("999999")));
    }

    @Test 
    void checkOfSavedMonitorIsStored() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 200);
        server.createContext("/ok", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("OK".getBytes());
            exchange.getResponseBody().close();
        });
        server.start();
        Monitor monitor = new Monitor("Test", "http://localhost:" + server.getAddress().getPort() + "/ok", 60, 2000, 200);
        monitor = monitorRepository.save(monitor);
        long checksBefore = checkRunRepository.count();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/monitors/" + monitor.getId() + "/checks"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.successful").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(200));

        // Verify that the check run is stored in the database
        long id = monitor.getId();
        assertTrue(checkRunRepository.count() > checksBefore);
        assertTrue(checkRunRepository.findAll().stream().anyMatch(cr -> cr.getMonitor().getId() == id));
    }

    @Test
    void checkUsesTheMonitorTimeout() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 200);
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(1500);  // sleep for 1.5 seconds to simulate timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        Monitor monitor = new Monitor("Test", "http://localhost:" + server.getAddress().getPort() + "/slow", 60, 500, 200);
        monitor = monitorRepository.save(monitor);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/monitors/" + monitor.getId() + "/checks"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.successful").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.errorType").value("TIMEOUT"));
        
    }

    @Test
    void historyOfUnknownMonitorReturns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/monitors/999999/checks"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("999999")));
    }

    @Test 
    void historyOfMonitorWithoutChecksIsEmpty() throws Exception {
        Monitor monitor = new Monitor("Test", "http://localhost:1", 60, 2000, 200);
        monitor = monitorRepository.save(monitor);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/monitors/" + monitor.getId() + "/checks"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalChecks").value(0));
    }

    @Test 
    void historyIsPagedNewestFirst() throws Exception {
        Instant now = java.time.Instant.now();
        Monitor history = monitorRepository.save(new Monitor("History", "http://localhost:1", 60, 2000, 200));
        Monitor other = monitorRepository.save(new Monitor("Other", "http://localhost:1", 60, 2000, 200));

        CheckRun oldest = saveCheck(history, now.minusSeconds(30));
        CheckRun middle = saveCheck(history, now.minusSeconds(20));
        CheckRun newest = saveCheck(history, now.minusSeconds(10));
        saveCheck(other, now);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/monitors/" + history.getId() + "/checks?page=0&size=2"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(newest.getId()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].id").value(middle.getId()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalChecks").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/monitors/" + history.getId() + "/checks?page=1&size=2"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(oldest.getId()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalChecks").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2));
    }

    private CheckRun saveCheck(Monitor monitor, Instant checkedAt) {
        CheckRun checkRun = new CheckRun(monitor, new CheckResult(monitor.getUrl(), checkedAt, 100, 200, true, null));
        return checkRunRepository.save(checkRun);
    }
}
