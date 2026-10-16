package dev.portfolio.uptimemonitor;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
public class CheckService {

    private static final String TARGET_URL = "http://localhost:8080/api/hello";
    private static final int EXPECTED_STATUS = 200;

    private final RestClient restClient;

    public CheckService(RestClient restClient) {  
        this.restClient = restClient;
    }

    public CheckResult runCheck() {
       return runCheckAgainst(TARGET_URL);
    }
    public CheckResult runCheckAgainst(String targetUrl) {
        Instant checkedAt = Instant.now();
        long startTime = System.nanoTime();
        int httpStatus;                             
        try {
            httpStatus = restClient.get()
                .uri(targetUrl)
                .exchange((request, response) -> response.getStatusCode().value());
                // exchange() is required to handle 4xx and 5xx status codes without throwing exceptions
        } catch (ResourceAccessException e) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            return new CheckResult(
                    targetUrl,
                    checkedAt,
                    durationMs,
                    null,
                    false,
                    determineErrorType(e.getCause())
            );
            
        }
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        boolean successful = httpStatus == EXPECTED_STATUS;
        String errorType = successful ? null : "UNEXPECTED_STATUS";

        return new CheckResult(
                targetUrl,
                checkedAt,
                durationMs,
                httpStatus,
                successful,
                errorType
        );
    }

    private String determineErrorType(Throwable error) {
        return switch (error){
            case ConnectException _ -> "CONNECTION_REFUSED";
            case SocketTimeoutException _ -> "TIMEOUT";
            case UnknownHostException _ -> "UNKNOWN_HOST";
            case null, default -> "NETWORK_ERROR";
        };
    }
}


