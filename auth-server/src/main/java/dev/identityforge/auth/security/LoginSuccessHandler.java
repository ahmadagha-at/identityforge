package dev.identityforge.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final LoginAttemptService attempts;

    public LoginSuccessHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        attempts.succeeded(authentication.getName(), request.getRemoteAddr());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

