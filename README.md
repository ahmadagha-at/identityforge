# IdentityForge Simple

IdentityForge Simple is a learning project that demonstrates how modern login systems work.

Many applications offer buttons such as “Sign in with Google” or “Sign in with Microsoft.” In those cases, Google or Microsoft acts as the trusted login provider: they authenticate the user and give the application secure proof that the login was successful.

IdentityForge follows the same principle, but instead of using Google, Keycloak, or another existing provider, it includes a custom-built identity provider based on Spring Authorization Server.

The project consists of two applications:

* The authorization server handles user registration, login, consent, and token creation. It acts as the custom login provider.
* The client application contains the user-facing website. It redirects users to the authorization server for login and receives the result afterward.

The login uses the OAuth 2.0 Authorization Code Flow with PKCE. OpenID Connect adds the identity information needed to recognize the authenticated user. After a successful login, the client receives an ID token containing information about the user and an access token that can be used to request protected information from the UserInfo endpoint.

The purpose of the project is not to replace production-ready providers such as Keycloak or Google. Its purpose is to make the complete login process visible and understandable: how an application redirects a user to a trusted provider, how the provider authenticates the user, how authorization codes and tokens are exchanged, and how the application verifies who logged in.


## Architecture

```text
Browser
  |
  | Authorization Code + PKCE
  v
client-app :8081  <--------------------------->  auth-server :9000
  |                                                   |
  | protected profile and UserInfo                    | users, clients,
  | call with the access token                        | consents, grants,
  v                                                   | audit events, keys
local HTTP session                                    v
                                              auth PostgreSQL :5433
```

two Java applications are built:

- `auth-server` is the OAuth2 Authorization Server and OpenID Provider.
- `client-app` is a conventional Spring Security OAuth2/OIDC browser client.

PostgreSQL is infrastructure for persistent users, clients, consents, and authorizations; it is not another application module.

## Demonstrated flow

1. The client starts an Authorization Code flow.
2. Spring Security creates `state`, `nonce`, and an S256 PKCE challenge.
3. The authorization server authenticates the user and collects consent.
4. The client receives a one-time authorization code.
5. The client exchanges the code with its PKCE verifier and client authentication.
6. The client validates the ID token and creates a local authenticated session.
7. The protected profile page calls `/userinfo` with the access token.

The ID token authenticates the user to the client. The access token authorizes the call to `/userinfo`.

## Project structure

```text
identityforge/
├── auth-server/
├── client-app/
├── requests/
├── docker-compose.yml
├── .env
├── .env.example
└── pom.xml
```

## Main endpoints

| Application | Endpoint | Purpose |
|---|---|---|
| Client | `GET /` | Public home page |
| Client | `GET /oauth2/authorization/identityforge` | Start OIDC login |
| Client | `GET /profile` | Protected profile and UserInfo result |
| Client | `POST /logout` | Local client logout |
| Authorization server | `GET /login` | Custom login page |
| Authorization server | `GET/POST /register` | User registration |
| Authorization server | `GET /oauth2/authorize` | Authorization endpoint |
| Authorization server | `POST /oauth2/token` | Token endpoint |
| Authorization server | `GET /oauth2/jwks` | Public signing keys |
| Authorization server | `GET /userinfo` | OIDC UserInfo endpoint |
| Authorization server | `GET /.well-known/openid-configuration` | OIDC discovery |
| Authorization server | `POST /admin/clients` | Administrative client registration |


## Build and run

Run the complete test suite first:

```bash
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9-eclipse-temurin-21 mvn clean verify
```

Start PostgreSQL and both Java applications on the shared Compose network. The multi-stage Dockerfiles build both executable JARs inside Docker, so a local Maven installation is not required:

```bash
docker compose up --build -d
```

Inspect their status:

```bash
docker compose ps -a
docker compose logs --tail=300 auth-server
docker compose logs --tail=300 client-app
```

Check health:

```bash
curl http://127.0.0.1:9000/actuator/health
curl http://127.0.0.1:8081/actuator/health
```

Open the client at `http://127.0.0.1:8081`.

The bootstrap administrator credentials are stored in `.env`. The included values are local-development credentials and must be replaced before the system is exposed outside a developer machine.

## Docker networking

The browser reaches the public issuer at `http://127.0.0.1:9000`. Backend requests from `client-app` use the Compose DNS name `http://auth-server:9000`. Both URLs refer to the same authorization server from different network perspectives.

## Tests

The suite covers public and protected client routes, anonymous and authenticated views, profile rendering, UserInfo parsing and errors, login and registration controllers, consent rendering, user-registration behavior, and PostgreSQL startup through Testcontainers.

```bash
mvn clean verify
```

The Testcontainers test requires Docker. The remaining unit and MVC tests can run without Docker.

## HTTP request collection

`requests/identityforge.http` contains discovery, page-controller, UserInfo, refresh, revocation, and negative authorization requests. Start the interactive Authorization Code flow in a browser because it requires cookies, login, consent, `state`, and PKCE state managed by Spring Security.

## Security boundaries

- The client secret identifies the confidential client; it is not a user password.
- PKCE binds the authorization code to the client that started the flow.
- `state` protects the authorization response; OIDC `nonce` binds the ID token to the login request.
- The ID token is consumed by the client and is not sent as an API bearer token.
- The access token authorizes `/userinfo`.
- RSA signing keys are persisted in a Docker volume.
- Redirect URIs are exact values without wildcards.
- CSRF protection remains enabled for forms and logout.
- Client logout is local; it does not necessarily end the authorization-server session.

## Retained provider features

The authorization server retains JDBC authorization persistence, refresh-token rotation, registration, consent, account lockout, audit logging, rate limiting, administrative client registration, and persistent RSA keys. These features remain isolated on the provider side and do not complicate the client flow.
