package dev.identityforge.auth.web;

import dev.identityforge.auth.user.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthPageController {
    private final UserRegistrationService registrations;

    public AuthPageController(UserRegistrationService registrations) {
        this.registrations = registrations;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String registrationForm(Model model) {
        model.addAttribute("registration", new RegistrationForm("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    String register(@Valid @ModelAttribute("registration") RegistrationForm form,
                    BindingResult binding, HttpServletRequest request) {
        if (binding.hasErrors()) return "register";
        registrations.register(form.username(), form.email(), form.password(), request.getRemoteAddr());
        return "redirect:/login?registered";
    }

    public record RegistrationForm(
            @NotBlank @Size(min = 3, max = 100) String username,
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 100) String password
    ) {}
}

