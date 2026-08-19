package dev.identityforge.auth.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identityforge")
public record IdentityForgeProperties(
        String issuer,
        String jwkFile,
        Bootstrap bootstrap,
        Security security
) {
    public record Bootstrap(
            String adminUsername,
            String adminEmail,
            String adminPassword,
            String clientAppSecret
    ) {}

    public record Security(
            int maximumLoginAttempts,
            Duration lockDuration,
            int tokenEndpointCapacity,
            Duration tokenEndpointRefillPeriod
    ) {}
}
