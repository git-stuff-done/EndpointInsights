# OIDC Authentication

## Overview

The system uses OpenID Connect (OIDC) for user authentication, providing secure login through an external identity provider. Users authenticate once and receive a JWT token for subsequent API requests.

## Authentication Flow

We make use of the [Authorization Code](https://openid.net/specs/openid-connect-basic-1_0.html#CodeFlow) flow in OIDC for user authentication. The general flow for this is as follows:
1. User initiates login with backend application.
2. Backend application generates a redirect to the Identity Provider (IdP) which contains the following information:
    - Client ID (to identify the application)
    - Redirect URI (where to send the user back to after authentication)
    - Requested Scopes (what details about the user the app needs)
    - Response Type (identifies the flow type `code` for our purposes)
    - State value (to protect against CSRF)
    - Optionally: Nonce value (to bind an authentication request to a specific user session)
3. User follows the redirect url to the identity provider; completes authentication, and consents to sharing their information with the specified application.
4. User is sent back to application with the authorization code.
5. Backend application [exchanges](https://openid.net/specs/openid-connect-basic-1_0.html#ObtainingTokens) authorization code and client secret with the Identity Provider for an access token and [id token](https://openid.net/specs/openid-connect-basic-1_0.html#IDToken).

This is generally where the code authentication flow ends, with the application (client) having confirmed that the user is authenticated and given some information about the user (from the ID token). In our application, we extend this to fit our client's requirements.

After our application obtains the id token, we expose it to the user so that the frontend of our application can store it and use it as a Bearer token for future requests. (ID tokens are JWTs signed by the Identity Provider which can be cryptographically verified given the Identity Provider's JSON Web Keys)


## Configuration

### OIDC Provider Settings
```yaml
spring.security.oauth2.client:
  provider.oidc:
    issuer-uri: https://auth.example.com
    authorization-uri: https://auth.example.com/api/oidc/authorization
    token-uri: https://auth.example.com/api/oidc/token
    user-info-uri: https://auth.example.com/api/oidc/userinfo
    jwk-set-uri: https://auth.example.com/jwks.json
  registration.oidc:
    client-id: endpoint-insights
    client-secret: your-secret-here
    redirect-uri:  http://localhost:8080/login/oauth2/code/oidc
    authorization-grant-type: authorization_code
    scope: openid,profile,email,groups
    client-name: OIDC
```

### Required Claims
The system expects these claims in the ID token:
- `sub` - User identifier
- `preferred_username` - Display username
- `email` - User email address
- `groups` - User group memberships for authorization

### A note on limitations
Normally, ID tokens only optionally contain user details (username, email, groups, etc) and such information should normally be fetched from the Identity Provider's userinfo endpoint. However in this case, due to the requirement of deploying our application without an external session provider, such as Redis, we are required to expect that the ID token contains our required claims.

This was done to maintain the simplicity of using the Identity Provider's JWKS to ensure the validity of bearer tokens without implementing token minting and signing ourselves.

In a production application, user details should be fetched from the user info endpoint on the first login and stored in a persistent data store. Then future authentications would use the combination of `iss` and `sub` claims to uniquely identify a user and correlate that user with their associated information in the persistent data store. This could be done in our application, but it was determined to add too much complexity for minimal benefit.

## Components

### [OAuth2JsonSuccessHandler](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/authentication/OAuth2JsonSuccessHandler.java)
Handles successful OIDC authentication:
- Validates OIDC user and ID token
- Extracts required claims (username, email)
- Returns JSON response with token details
- Provides token expiration information

### [AuthorizationInterceptor](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/authentication/AuthorizationInterceptor.java)
Validates bearer tokens from authenticated users (see [JWT Authorization](authorization-jwt.md) for details):
- Allows users to access public endpoints without authorization
- Validates Authorization header and extracts Bearer token
- Validates the Bearer token contains the proper claims
- Determines if the Bearer token is authorized to access the requested resource
- Validates that the Bearer token is signed by a trusted JSON Web Key
- Populates the CurrentUser class for request context

### [SecurityConfig](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/config/SecurityConfig.java)
Configures Spring Security for OIDC integration:
- Client registration with provider details
- OAuth2 login flow with custom success handler
- Spring Security Filter Chain
- CSRF disabled for stateless JWT API
- Session creation only for OAuth2 authorization code flow

## Security Features

### Token Validation
- ID token signature verified against OIDC provider
- Required claims presence validated
- Token expiration enforced
- Malformed authentication responses rejected with 400

### Public Endpoints
These endpoints bypass authentication:
- `/login/**` - Authentication flows
- `/oauth2/**` - OAuth2 callbacks
- `/api/health` - Health check (see [JWT Authorization](authorization-jwt.md))

## Testing

### Testing OIDC Authentication Flow

#### 1. Initiate Login
```bash
# This will redirect to the identity provider
curl -v http://localhost:8080/oauth2/authorization/oidc
```

The response will be a 302 redirect to your OIDC provider's authorization endpoint with query parameters:
- `client_id`
- `redirect_uri`
- `response_type=code`
- `scope=openid profile email groups`
- `state` (CSRF protection)

#### 2. Complete Authentication in Browser
Since the OIDC flow requires browser interaction:
1. Open browser to `http://localhost:8080/oauth2/authorization/oidc`
2. Authenticate with identity provider
3. Grant consent if prompted
4. You will be redirected back to the application

#### 3. Verify Success Response
After successful authentication, the success handler returns JSON:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": 1234567890,
  "username": "testuser",
  "email": "test@example.com"
}
```

#### 4. Verify Token Claims
You can decode the JWT token (without verification) to inspect claims:
```bash
# Extract the payload (second part) of the JWT
echo "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." | cut -d. -f2 | base64 -d | jq
```

Expected claims:
```json
{
  "sub": "user-id-123",
  "preferred_username": "testuser",
  "email": "test@example.com",
  "groups": ["ei:write"],
  "iss": "https://auth.example.com",
  "exp": 1234567890
}
```

### Testing Error Cases

#### Missing Required Claims
If the ID token is missing required claims, the success handler will return 400:
```json
{
  "error": "Bad Request",
  "status": 400
}
```

Check server logs for specific error messages:
- "ID token missing preferred_username"
- "ID token missing email"
- "ID token missing expiry time"

#### Invalid Provider Configuration
If the OIDC provider configuration is incorrect:
```bash
# Check logs for JWT decoder initialization
# Should see: "Initialized JWT decoder with JWKS URI: https://..."
```

## Possible Enhancements

- Add Proof-Key Code Exchange (PKCE) to improve security between backend application and identity provider
- Add token refresh flow to improve user experience
- Add comprehensive audit logging (successful/failed auth attempts, token validations)
- Implement logout endpoint to invalidate sessions