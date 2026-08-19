package dev.identityforge.auth.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import dev.identityforge.auth.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthPageControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserRegistrationService registrations;

    @Test
    void rendersLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void rendersRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registration"));
    }

    @Test
    void registersAValidUser() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "alice")
                        .param("email", "alice@example.com")
                        .param("password", "a-secure-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(registrations).register(
                "alice", "alice@example.com", "a-secure-password", "127.0.0.1");
    }

    @Test
    void rejectsAnInvalidRegistrationForm() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "a")
                        .param("email", "not-an-email")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors(
                        "registration", "username", "email", "password"));
    }
}
