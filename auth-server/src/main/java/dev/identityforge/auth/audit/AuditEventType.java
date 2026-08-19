package dev.identityforge.auth.audit;

public enum AuditEventType {
    USER_REGISTERED,
    LOGIN_SUCCEEDED,
    LOGIN_FAILED,
    ACCOUNT_LOCKED,
    CLIENT_REGISTERED,
    TOKEN_REVOKED,
    TOKEN_RATE_LIMITED
}

