package dev.identityforge.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ClientSecurityConfig {
    @Bean
    SecurityFilterChain clientSecurityFilterChain(
            HttpSecurity http, ClientRegistrationRepository registrations) throws Exception {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, "/oauth2/authorization");
        // PKCE is explicit even though this demo client also authenticates with a secret.
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(login -> login.authorizationEndpoint(endpoint ->
                        endpoint.authorizationRequestResolver(resolver)))
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }
}

