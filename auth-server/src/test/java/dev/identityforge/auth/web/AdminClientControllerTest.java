package dev.identityforge.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.identityforge.auth.client.RegisteredClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminClientControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean RegisteredClientService clients;

    @Test
    void createsAValidClient() throws Exception {
        mockMvc.perform(post("/admin/clients")
                        .principal(() -> "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "reporting-client",
                                  "clientName": "Reporting Client",
                                  "confidential": true,
                                  "rawSecret": "a-strong-client-secret",
                                  "redirectUris": ["http://127.0.0.1:8090/callback"],
                                  "scopes": ["openid"],
                                  "allowClientCredentials": false,
                                  "accessTokenMinutes": 10,
                                  "refreshTokenHours": 8
                                }
                                """))
                .andExpect(status().isCreated());

        verify(clients).create(any(), eq("admin"), eq("127.0.0.1"));
    }

    @Test
    void rejectsAnInvalidClientRequest() throws Exception {
        mockMvc.perform(post("/admin/clients")
                        .principal(() -> "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "",
                                  "clientName": "",
                                  "confidential": true,
                                  "redirectUris": [],
                                  "scopes": [],
                                  "allowClientCredentials": false,
                                  "accessTokenMinutes": 0,
                                  "refreshTokenHours": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"));
    }
}
