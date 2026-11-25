package com.temple.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed view of the {@code app.*} configuration. Validated at startup so a
 * misconfigured deployment fails fast instead of running insecurely.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Security security,
        @Valid @NotNull Cors cors,
        @Valid @NotNull BootstrapAdmin bootstrapAdmin,
        @Valid @NotNull Photos photos,
        @Valid @NotNull Newsletter newsletter) {

    public record Security(
            @NotBlank(message = "JWT_SECRET must be set (openssl rand -base64 48)") String jwtSecret,
            @NotNull Duration jwtTtl,
            @NotBlank String issuer,
            @Min(1) int loginMaxAttempts,
            @NotNull Duration loginLockout) {
    }

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            allowedOrigins = allowedOrigins == null
                    ? List.of()
                    : allowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
    }

    public record BootstrapAdmin(String username, String password, String displayName, String email) {
        public boolean isConfigured() {
            return username != null && !username.isBlank() && password != null && !password.isBlank();
        }
    }

    public record Photos(@Min(1024) long maxBytes) {
    }

    public record Newsletter(
            @NotBlank String from,
            boolean enabled,
            @NotBlank String cron,
            @NotBlank String zone,
            @NotBlank String siteUrl) {
    }
}
