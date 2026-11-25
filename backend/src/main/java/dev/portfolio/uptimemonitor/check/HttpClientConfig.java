package dev.portfolio.uptimemonitor.check;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClient restClient() {
        // Timeouts are set for each check in CheckService, from the monitor's timeoutMs.
        return RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}
