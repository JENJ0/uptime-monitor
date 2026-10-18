package dev.portfolio.uptimemonitor.monitor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;

public record CreateMonitorRequest(
    @NotBlank @Size(max = 100) String name,
    @Pattern(regexp = "https?://.+") @NotBlank @Size(max = 2048) String url,
    @Min(value = 30) int intervalSeconds,
    @Min(value = 100) @Max(value = 10000) int timeoutMs,
    @Min(value = 100) @Max(value = 599) int expectedStatus
) {
    
}
