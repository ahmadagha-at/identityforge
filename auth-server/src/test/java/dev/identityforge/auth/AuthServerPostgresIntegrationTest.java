package dev.identityforge.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "identityforge.jwk-file=target/test-keys/test-jwk.json",
        "identityforge.bootstrap.admin-password=test-password-12345",
        "identityforge.bootstrap.client-app-secret=test-client-secret"
})
class AuthServerPostgresIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void applicationStartsWithRealPostgres() {}
}
