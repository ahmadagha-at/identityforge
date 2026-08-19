package dev.identityforge.auth.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConsentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsentControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean RegisteredClientRepository clients;

    @Test
    void rendersRequestedScopesForTheKnownClient() throws Exception {
        RegisteredClient client = RegisteredClient.withId("client-id")
                .clientId("identityforge-client")
                .clientName("IdentityForge Client Application")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://127.0.0.1:8081/login/oauth2/code/identityforge")
                .scope("openid")
                .scope("profile")
                .build();
        when(clients.findByClientId("identityforge-client")).thenReturn(client);

        mockMvc.perform(get("/oauth2/consent")
                        .principal(() -> "alice")
                        .param("client_id", "identityforge-client")
                        .param("scope", "openid profile")
                        .param("state", "test-state"))
                .andExpect(status().isOk())
                .andExpect(view().name("consent"))
                .andExpect(model().attribute("principalName", "alice"))
                .andExpect(model().attribute("scopes", Set.of("openid", "profile")));
    }
}
