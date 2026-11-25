package dev.portfolio.uptimemonitor.check;

import java.time.Instant;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
public class CheckService {
    private static final Duration MAX_CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private final RestClient restClient;

    public CheckService(RestClient restClient) {  
        this.restClient = restClient;
    }
    
    public CheckResult runCheckAgainst(String targetUrl, int expectedStatus, Duration timeout) {
        RestClient client = clientWith(timeout);
        Instant checkedAt = Instant.now();
        long startTime = System.nanoTime();
        int httpStatus;                             
        try {
            httpStatus = client.get()
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
        boolean successful = httpStatus == expectedStatus;
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

    static Duration connectTimeoutFor(Duration timeout) {
        return timeout.compareTo(MAX_CONNECT_TIMEOUT) < 0 ? timeout : MAX_CONNECT_TIMEOUT;
    }

    private RestClient clientWith(Duration timeout) {
        if (!timeout.isPositive()) {
            throw new IllegalArgumentException("Timeout must be positive, was: " + timeout);
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutFor(timeout));
        requestFactory.setReadTimeout((int) timeout.toMillis());
        return restClient.mutate().requestFactory(requestFactory).build();
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


