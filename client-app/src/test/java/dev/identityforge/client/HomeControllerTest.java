package dev.identityforge.client;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import dev.identityforge.client.web.HomeController;
import dev.identityforge.client.web.UserInfoClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserInfoClient userInfo;

    @Test
    void rendersAnonymousHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("authenticated", false));
    }

    @Test
    void rendersAuthenticatedHomePage() throws Exception {
        mockMvc.perform(get("/").with(oidcLogin()
                        .idToken(token -> token.subject("alice-subject"))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("subject", "alice-subject"));
    }

    @Test
    void loadsProtectedProfileAndUserInfo() throws Exception {
        when(userInfo.load()).thenReturn(Map.of("sub", "alice-subject", "name", "Alice"));

        mockMvc.perform(get("/profile").with(oidcLogin()
                        .idToken(token -> token.subject("alice-subject"))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("subject", "alice-subject"))
                .andExpect(model().attribute("userInfo",
                        Map.of("sub", "alice-subject", "name", "Alice")));
    }
}
