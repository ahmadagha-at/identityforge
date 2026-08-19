package dev.identityforge.auth.user;

import dev.identityforge.auth.audit.AuditEventType;
import dev.identityforge.auth.audit.AuditService;
import dev.identityforge.auth.common.ConflictException;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {
    private final ApplicationUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserRegistrationService(ApplicationUserRepository users, PasswordEncoder passwordEncoder,
                                   AuditService auditService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public void register(String username, String email, String rawPassword, String ipAddress) {
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("The username is already registered.");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("The email address is already registered.");
        }
        users.save(new ApplicationUser(username.trim(), email.trim().toLowerCase(),
                passwordEncoder.encode(rawPassword), Set.of(UserRole.ROLE_USER)));
        auditService.record(AuditEventType.USER_REGISTERED, username, null, ipAddress,
                "A new user account was registered.");
    }
}

