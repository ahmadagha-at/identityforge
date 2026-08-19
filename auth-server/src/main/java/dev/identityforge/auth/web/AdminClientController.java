package dev.identityforge.auth.web;

import dev.identityforge.auth.client.RegisteredClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.time.Duration;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/clients")
public class AdminClientController {
    private final RegisteredClientService clients;

    public AdminClientController(RegisteredClientService clients) {
        this.clients = clients;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    void create(@Valid @RequestBody CreateClientRequest request, Principal principal,
                HttpServletRequest servletRequest) {
        clients.create(new RegisteredClientService.CreateClientCommand(
                request.clientId(), request.clientName(), request.confidential(), request.rawSecret(),
                request.redirectUris(), request.scopes(), request.allowClientCredentials(),
                Duration.ofMinutes(request.accessTokenMinutes()),
                Duration.ofHours(request.refreshTokenHours())),
                principal.getName(), servletRequest.getRemoteAddr());
    }

    public record CreateClientRequest(
            @NotBlank @Size(max = 100) String clientId,
            @NotBlank @Size(max = 200) String clientName,
            boolean confidential,
            @Size(max = 200) String rawSecret,
            @NotEmpty Set<String> redirectUris,
            @NotEmpty Set<String> scopes,
            boolean allowClientCredentials,
            @Min(1) int accessTokenMinutes,
            @Min(1) int refreshTokenHours
    ) {}
}
