package dev.identityforge.auth.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.stereotype.Component;

@Component
public class JwkService {
    private final IdentityForgeProperties properties;

    public JwkService(IdentityForgeProperties properties) {
        this.properties = properties;
    }

    /**
     * Loads a persisted private JWK or creates one atomically for local and Compose use.
     * Production systems should replace this file-based mechanism with a managed key service.
     */
    public RSAKey loadOrCreate() {
        Path path = Path.of(properties.jwkFile());
        try {
            if (Files.exists(path)) {
                JWK key = JWK.parse(Files.readString(path, StandardCharsets.UTF_8));
                if (!(key instanceof RSAKey rsaKey) || !rsaKey.isPrivate()) {
                    throw new IllegalStateException("The configured JWK is not a private RSA key.");
                }
                return rsaKey;
            }
            Files.createDirectories(path.toAbsolutePath().getParent());
            RSAKey generated = new RSAKeyGenerator(2048).keyIDFromThumbprint(true).generate();
            Files.writeString(path, generated.toJSONString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return generated;
        } catch (IOException | java.text.ParseException | JOSEException exception) {
            throw new IllegalStateException("Signing key initialization failed.", exception);
        }
    }

    public JWKSet asJwkSet() {
        return new JWKSet(loadOrCreate());
    }
}
