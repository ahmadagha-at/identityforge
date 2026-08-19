package dev.identityforge.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.identityforge.auth.audit.AuditService;
import dev.identityforge.auth.common.ConflictException;
import dev.identityforge.auth.user.ApplicationUserRepository;
import dev.identityforge.auth.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {
    @Mock ApplicationUserRepository users;
    @Mock AuditService auditService;

    @Test
    void rejectsAnExistingUsernameWithoutSaving() {
        when(users.existsByUsernameIgnoreCase("alice")).thenReturn(true);
        UserRegistrationService service = new UserRegistrationService(
                users, new BCryptPasswordEncoder(), auditService);

        assertThatThrownBy(() -> service.register(
                "alice", "alice@example.com", "a-secure-password", "127.0.0.1"))
                .isInstanceOf(ConflictException.class);

        verify(users, never()).save(any());
    }
}

