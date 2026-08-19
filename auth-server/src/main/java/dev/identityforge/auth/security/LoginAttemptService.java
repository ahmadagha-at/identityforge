package dev.identityforge.auth.security;

import dev.identityforge.auth.audit.AuditEventType;
import dev.identityforge.auth.audit.AuditService;
import dev.identityforge.auth.configuration.IdentityForgeProperties;
import dev.identityforge.auth.user.ApplicationUser;
import dev.identityforge.auth.user.ApplicationUserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {
    private final ApplicationUserRepository users;
    private final AuditService auditService;
    private final IdentityForgeProperties properties;

    public LoginAttemptService(ApplicationUserRepository users, AuditService auditService,
                               IdentityForgeProperties properties) {
        this.users = users;
        this.auditService = auditService;
        this.properties = properties;
    }

    @Transactional
    public void failed(String username, String ipAddress) {
        users.findByUsernameIgnoreCase(username).ifPresent(user -> {
            user.recordFailure(properties.security().maximumLoginAttempts(),
                    Instant.now().plus(properties.security().lockDuration()));
            AuditEventType type = user.isLocked(Instant.now())
                    ? AuditEventType.ACCOUNT_LOCKED : AuditEventType.LOGIN_FAILED;
            auditService.record(type, username, null, ipAddress,
                    "Authentication failed; the response intentionally omits account details.");
        });
    }

    @Transactional
    public void succeeded(String username, String ipAddress) {
        ApplicationUser user = users.findByUsernameIgnoreCase(username).orElseThrow();
        user.recordSuccess();
        auditService.record(AuditEventType.LOGIN_SUCCEEDED, username, null, ipAddress,
                "Authentication succeeded.");
    }
}

