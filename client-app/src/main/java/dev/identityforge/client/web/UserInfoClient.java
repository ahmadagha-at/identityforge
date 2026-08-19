package dev.identityforge.client.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class UserInfoClient {
    private final WebClient webClient;
    private final String userInfoUri;

    public UserInfoClient(WebClient webClient,
                          @Value("${spring.security.oauth2.client.provider.identityforge.user-info-uri}")
                          String userInfoUri) {
        this.webClient = webClient;
        this.userInfoUri = userInfoUri;
    }

    public Map<String, Object> load() {
        try {
            Map<String, Object> result = webClient.get()
                    .uri(userInfoUri)
                    .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction
                            .clientRegistrationId("identityforge"))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            return result == null ? Map.of() : result;
        } catch (WebClientResponseException exception) {
            throw new ClientGatewayException("The UserInfo request was rejected.", exception);
        }
    }
}
