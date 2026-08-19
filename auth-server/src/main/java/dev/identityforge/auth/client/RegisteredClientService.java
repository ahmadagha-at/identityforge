package dev.identityforge.auth.client;

import dev.identityforge.auth.audit.AuditEventType;
import dev.identityforge.auth.audit.AuditService;
import dev.identityforge.auth.common.ConflictException;
import dev.identityforge.auth.common.InvalidRequestException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

@Service
public class RegisteredClientService {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public RegisteredClientService(RegisteredClientRepository clients, PasswordEncoder passwordEncoder,
                                   AuditService auditService) {
        this.clients = clients;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public void create(CreateClientCommand command, String actor, String ipAddress) {
        if (clients.findByClientId(command.clientId()) != null) {
            throw new ConflictException("The client ID is already registered.");
        }
        if (command.confidential() && (command.rawSecret() == null || command.rawSecret().length() < 16)) {
            throw new InvalidRequestException("Confidential clients require a secret of at least 16 characters.");
        }
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(command.clientId())
                .clientName(command.clientName())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(command.accessTokenTtl())
                        .refreshTokenTimeToLive(command.refreshTokenTtl())
                        .reuseRefreshTokens(false)
                        .build());
        if (command.confidential()) {
            builder.clientSecret(passwordEncoder.encode(command.rawSecret()))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        }
        command.redirectUris().forEach(builder::redirectUri);
        command.scopes().forEach(builder::scope);
        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
        if (command.allowClientCredentials()) {
            builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
        }
        clients.save(builder.build());
        auditService.record(AuditEventType.CLIENT_REGISTERED, actor, command.clientId(), ipAddress,
                "A registered client was created through the administrative API.");
    }

    public record CreateClientCommand(String clientId, String clientName, boolean confidential,
                                      String rawSecret, java.util.Set<String> redirectUris,
                                      java.util.Set<String> scopes, boolean allowClientCredentials,
                                      Duration accessTokenTtl, Duration refreshTokenTtl) {}
}
