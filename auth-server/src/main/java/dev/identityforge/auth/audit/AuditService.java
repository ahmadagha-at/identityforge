package dev.identityforge.auth.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEventType type, String principal, String clientId,
                       String ipAddress, String details) {
        repository.save(new AuditLogEntry(type, principal, clientId, ipAddress, details));
    }
}

