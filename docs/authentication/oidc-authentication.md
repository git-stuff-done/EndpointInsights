# OIDC Authentication

## Overview

The system uses OpenID Connect (OIDC) for user authentication, providing secure login through an external identity provider. Users authenticate once and receive a JWT token for subsequent API requests.

## Authentication Flow

### Initial Login
1. User initiates login through OIDC provider
2. OIDC provider redirects back with authorization code
3. Backend exchanges code for ID token
4. System validates token and extracts user information
5. Client receives JWT token for subsequent requests

## Components

### SecurityConfig
Configures Spring Security for OIDC integration:
- Client registration with provider details
- OAuth2 login flow with custom success handler
- Session management configuration

### OAuth2JsonSuccessHandler
Handles successful OIDC authentication:
- Validates OIDC user and ID token
- Extracts required claims (username, email)
- Returns JSON response with token details
- Provides token expiration information

## Configuration

### OIDC Provider Settings
```yaml
spring.security.oauth2.client:
  provider.oidc:
    issuer-uri: https://auth.example.com
    jwk-set-uri: https://auth.example.com/jwks.json
  registration.oidc:
    client-id: endpoint-insights
    scope: openid,profile,email,groups
```

### Required Claims
The system expects these claims in the ID token:
- `sub` - User identifier
- `preferred_username` - Display username
- `email` - User email address
- `groups` - User group memberships for authorization

## Security Features

### Token Validation
- ID token signature verified against OIDC provider
- Required claims presence validated
- Token expiration enforced
- Malformed tokens rejected with 400

### Public Endpoints
These endpoints bypass authentication:
- `/login/**` - Authentication flows
- `/oauth2/**` - OAuth2 callbacks

## Benefits

- **Standards-based**: Industry-standard OIDC protocol
- **Secure**: Cryptographic token validation
- **Centralized**: Single identity provider for all users
- **Flexible**: Configurable claim mapping