package dev.identityforge.client;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.identityforge.client.web.UserInfoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
class ClientSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserInfoClient userInfo;

    @Test
    void redirectsProtectedProfileToAuthorizationFlow() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/identityforge"));
    }

    @Test
    void permitsThePublicHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
