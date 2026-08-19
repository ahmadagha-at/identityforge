package dev.identityforge.auth.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "application_user")
public class ApplicationUser {

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private long version;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ApplicationUser() {}

    public ApplicationUser(String username, String email, String passwordHash, Set<UserRole> roles) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.createdAt = Instant.now();
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void recordFailure(int maximumAttempts, Instant lockUntil) {
        failedLoginAttempts++;
        if (failedLoginAttempts >= maximumAttempts) {
            lockedUntil = lockUntil;
            failedLoginAttempts = 0;
        }
    }

    public void recordSuccess() {
        failedLoginAttempts = 0;
        lockedUntil = null;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Set<UserRole> getRoles() { return Set.copyOf(roles); }
}
