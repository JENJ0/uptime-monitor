package dev.portfolio.uptimemonitor.check;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

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

@SpringBootTest
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
}
