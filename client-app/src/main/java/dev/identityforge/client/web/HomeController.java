package dev.identityforge.client.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserInfoClient userInfo;

    public HomeController(UserInfoClient userInfo) {
        this.userInfo = userInfo;
    }

    @GetMapping("/")
    String index(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("authenticated", user != null);
        if (user != null) {
            model.addAttribute("subject", user.getSubject());
            model.addAttribute("claims", user.getClaims());
        }
        return "index";
    }

    @GetMapping("/profile")
    String profile(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("subject", user.getSubject());
        model.addAttribute("idTokenClaims", user.getClaims());
        model.addAttribute("userInfo", userInfo.load());
        return "profile";
    }
}
