package dev.portfolio.uptimemonitor.check;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.sun.net.httpserver.HttpServer;

import dev.portfolio.uptimemonitor.TestcontainersConfiguration;
import dev.portfolio.uptimemonitor.monitor.Monitor;
import dev.portfolio.uptimemonitor.monitor.MonitorRepository;

@SpringBootTest(properties = {
    "uptime.scheduler.enabled=false",
    "spring.datasource.hikari.maximum-pool-size=1",
    "spring.datasource.hikari.connection-timeout=500"
}) 

@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ManualCheckConnectionTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MonitorRepository monitorRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private HttpServer server;

    @AfterEach 
    void cleanup() {
        executor.shutdownNow();
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void databaseConnectionIsFreeWhileTargetIsResponding() throws Exception {
        CountDownLatch arrived = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/held", exchange -> {
            arrived.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }            
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        Monitor monitor = monitorRepository.save(new Monitor("Test", "http://localhost:" + server.getAddress().getPort() + "/held", 60, 2000, 200));
        Future<Integer> status = executor.submit(() -> mockMvc
            .perform(MockMvcRequestBuilders.post("/api/monitors/" + monitor.getId() + "/checks"))
            .andReturn().getResponse().getStatus());
            
        assertTrue(arrived.await(5, TimeUnit.SECONDS));
        try{
            assertDoesNotThrow(() -> monitorRepository.count(), "The only pool connection is occupied!");
        } finally { 
            release.countDown();
        }
        assertEquals(201, status.get(5, TimeUnit.SECONDS));
    }
}
