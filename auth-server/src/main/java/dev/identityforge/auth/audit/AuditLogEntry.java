package dev.identityforge.auth.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log_entry")
public class AuditLogEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    private AuditEventType eventType;

    @Column(name = "principal_name", length = 100)
    private String principalName;

    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 1000)
    private String details;

    protected AuditLogEntry() {}

    public AuditLogEntry(AuditEventType eventType, String principalName, String clientId,
                         String ipAddress, String details) {
        this.eventType = eventType;
        this.principalName = principalName;
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.details = details;
        this.occurredAt = Instant.now();
    }
}

