create table application_user (
    id uuid primary key,
    version bigint not null default 0,
    username varchar(100) not null unique,
    email varchar(320) not null unique,
    password_hash varchar(200) not null,
    failed_login_attempts integer not null default 0,
    locked_until timestamptz,
    created_at timestamptz not null
);

create table application_user_role (
    user_id uuid not null references application_user(id) on delete cascade,
    role varchar(50) not null,
    primary key (user_id, role)
);

create table audit_log_entry (
    id uuid primary key,
    event_type varchar(60) not null,
    principal_name varchar(100),
    client_id varchar(100),
    ip_address varchar(64),
    occurred_at timestamptz not null,
    details varchar(1000) not null
);

create index idx_audit_occurred_at on audit_log_entry(occurred_at desc);
create index idx_audit_principal on audit_log_entry(principal_name);

create table oauth2_registered_client (
    id varchar(100) primary key,
    client_id varchar(100) not null,
    client_id_issued_at timestamptz not null default now(),
    client_secret varchar(200),
    client_secret_expires_at timestamptz,
    client_name varchar(200) not null,
    client_authentication_methods varchar(1000) not null,
    authorization_grant_types varchar(1000) not null,
    redirect_uris varchar(2000),
    post_logout_redirect_uris varchar(2000),
    scopes varchar(1000) not null,
    client_settings varchar(2000) not null,
    token_settings varchar(2000) not null
);

create unique index uk_oauth2_client_id on oauth2_registered_client(client_id);

create table oauth2_authorization (
    id varchar(100) primary key,
    registered_client_id varchar(100) not null references oauth2_registered_client(id),
    principal_name varchar(200) not null,
    authorization_grant_type varchar(100) not null,
    authorized_scopes varchar(1000),
    attributes bytea,
    state varchar(500),
    authorization_code_value bytea,
    authorization_code_issued_at timestamptz,
    authorization_code_expires_at timestamptz,
    authorization_code_metadata bytea,
    access_token_value bytea,
    access_token_issued_at timestamptz,
    access_token_expires_at timestamptz,
    access_token_metadata bytea,
    access_token_type varchar(100),
    access_token_scopes varchar(1000),
    oidc_id_token_value bytea,
    oidc_id_token_issued_at timestamptz,
    oidc_id_token_expires_at timestamptz,
    oidc_id_token_metadata bytea,
    oidc_id_token_claims bytea,
    refresh_token_value bytea,
    refresh_token_issued_at timestamptz,
    refresh_token_expires_at timestamptz,
    refresh_token_metadata bytea,
    user_code_value bytea,
    user_code_issued_at timestamptz,
    user_code_expires_at timestamptz,
    user_code_metadata bytea,
    device_code_value bytea,
    device_code_issued_at timestamptz,
    device_code_expires_at timestamptz,
    device_code_metadata bytea
);

create index idx_oauth2_authorization_client on oauth2_authorization(registered_client_id);
create index idx_oauth2_authorization_principal on oauth2_authorization(principal_name);

create table oauth2_authorization_consent (
    registered_client_id varchar(100) not null references oauth2_registered_client(id) on delete cascade,
    principal_name varchar(200) not null,
    authorities varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);
