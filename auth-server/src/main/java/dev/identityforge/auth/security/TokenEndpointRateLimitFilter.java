package dev.identityforge.auth.security;

import dev.identityforge.auth.audit.AuditEventType;
import dev.identityforge.auth.audit.AuditService;
import dev.identityforge.auth.configuration.IdentityForgeProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenEndpointRateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final IdentityForgeProperties properties;
    private final AuditService auditService;

    public TokenEndpointRateLimitFilter(IdentityForgeProperties properties,
                                        AuditService auditService) {
        this.properties = properties;
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/oauth2/token".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(
                properties.security().tokenEndpointCapacity(), Instant.now()));
        if (!bucket.tryConsume(properties)) {
            auditService.record(AuditEventType.TOKEN_RATE_LIMITED, null, null, key,
                    "The token endpoint rate limit was exceeded.");
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static final class Bucket {
        private int tokens;
        private Instant lastRefill;

        private Bucket(int tokens, Instant lastRefill) {
            this.tokens = tokens;
            this.lastRefill = lastRefill;
        }

        private synchronized boolean tryConsume(IdentityForgeProperties properties) {
            Instant now = Instant.now();
            if (lastRefill.plus(properties.security().tokenEndpointRefillPeriod()).isBefore(now)) {
                tokens = properties.security().tokenEndpointCapacity();
                lastRefill = now;
            }
            if (tokens == 0) return false;
            tokens--;
            return true;
        }
    }
}

