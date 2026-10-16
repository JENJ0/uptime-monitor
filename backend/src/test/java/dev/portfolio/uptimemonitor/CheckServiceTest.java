package dev.portfolio.uptimemonitor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

class CheckServiceTest { // public not needed for JUnit 5
    private CheckService checkService;
    private HttpServer server;
    private String baseUrl;

    @BeforeEach 
    void startFakeServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ok", exchange -> {
            exchange.sendResponseHeaders(200, -1);  // status 200, no body
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            exchange.sendResponseHeaders(500, -1);  // status 500, no body
            exchange.close();
        });
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3000);  // sleep for 3 seconds to simulate timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();

        checkService = new CheckService(new HttpClientConfig().restClient());
    }

    @AfterEach 
    void stopFakeServer() {
        server.stop(0);
    }

    @Test 
    void expectedStatusIsSuccessful() {
        CheckResult result = checkService.runCheckAgainst(baseUrl + "/ok");
        assertTrue(result.successful());
        assertEquals(200, result.httpStatus());
        assertNull(result.errorType());
    }

    @Test 
    void unexpectedStatusIsUnsuccessful() {
        CheckResult result = checkService.runCheckAgainst(baseUrl + "/error");
        assertFalse(result.successful());
        assertEquals(500, result.httpStatus());
        assertEquals("UNEXPECTED_STATUS", result.errorType());
    }

    @Test
    void unreachableTargetIsConnectionRefused() {
        CheckResult result = checkService.runCheckAgainst("http://localhost:1");
        assertFalse(result.successful());
        assertNull(result.httpStatus());
        assertEquals("CONNECTION_REFUSED", result.errorType());
    }

    @Test
    void slowTargetIsTimeout() {
        CheckResult result = checkService.runCheckAgainst(baseUrl + "/slow");
        assertFalse(result.successful());
        assertNull(result.httpStatus());
        assertEquals("TIMEOUT", result.errorType());
        assertTrue(result.durationMs() >= 2000);
    }
}