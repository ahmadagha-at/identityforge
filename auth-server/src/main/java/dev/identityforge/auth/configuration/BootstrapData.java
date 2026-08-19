package dev.identityforge.auth.configuration;

import dev.identityforge.auth.client.RegisteredClientService;
import dev.identityforge.auth.user.ApplicationUser;
import dev.identityforge.auth.user.ApplicationUserRepository;
import dev.identityforge.auth.user.UserRole;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
public class BootstrapData implements ApplicationRunner {
    private final ApplicationUserRepository users;
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    private final IdentityForgeProperties properties;

    public BootstrapData(ApplicationUserRepository users, RegisteredClientRepository clients,
                         PasswordEncoder encoder, IdentityForgeProperties properties) {
        this.users = users;
        this.clients = clients;
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        createAdmin();
        createBrowserClient();
    }

    private void createAdmin() {
        var bootstrap = properties.bootstrap();
        if (!users.existsByUsernameIgnoreCase(bootstrap.adminUsername())) {
            users.save(new ApplicationUser(bootstrap.adminUsername(), bootstrap.adminEmail(),
                    encoder.encode(bootstrap.adminPassword()),
                    Set.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN)));
        }
    }

    private void createBrowserClient() {
        if (clients.findByClientId("identityforge-client") != null) return;
        RegisteredClient client = RegisteredClient.withId(java.util.UUID.randomUUID().toString())
                .clientId("identityforge-client")
                .clientSecret(encoder.encode(properties.bootstrap().clientAppSecret()))
                .clientName("IdentityForge Client Application")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8081/login/oauth2/code/identityforge")
                .redirectUri("http://localhost:8081/login/oauth2/code/identityforge")
                .postLogoutRedirectUri("http://127.0.0.1:8081/")
                .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder().requireProofKey(true)
                        .requireAuthorizationConsent(true).build())
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofHours(8)).reuseRefreshTokens(false).build())
                .build();
        clients.save(client);
    }
}
