package dev.identityforge.auth.web;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsentController {
    private final RegisteredClientRepository clients;

    public ConsentController(RegisteredClientRepository clients) {
        this.clients = clients;
    }

    @GetMapping("/oauth2/consent")
    String consent(Principal principal,
                   @RequestParam("client_id") String clientId,
                   @RequestParam("scope") String scope,
                   @RequestParam("state") String state,
                   Model model) {
        RegisteredClient client = clients.findByClientId(clientId);
        if (client == null) throw new IllegalArgumentException("Unknown OAuth client.");
        Set<String> requestedScopes = new LinkedHashSet<>(Arrays.asList(scope.split(" ")));
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", client.getClientName());
        model.addAttribute("principalName", principal.getName());
        model.addAttribute("state", state);
        model.addAttribute("scopes", requestedScopes);
        return "consent";
    }
}

