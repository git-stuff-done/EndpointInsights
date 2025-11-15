# JWT Authorization

## Overview

The system uses stateless JWT-based authorization for API requests. After [OIDC authentication](authentication-oidc.md), clients receive a JWT token that must be included in subsequent API requests as a Bearer token.

## Session Flow

### Request Authorization
1. Client includes JWT in `Authorization: Bearer <token>` header
2. `AuthorizationInterceptor` validates token signature using JWKS
3. System extracts user context (username, email, role)
4. Request proceeds if user has required permissions

## Components

### [AuthorizationInterceptor](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/authentication/AuthorizationInterceptor.java)
Validates JWT tokens on API requests:
- Decodes and validates JWT signature against OIDC provider's JWKS
- Extracts user context from token claims
- Enforces role-based access control
- Sets up UserContext for request processing

### [CurrentUser](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/util/CurrentUser.java)
Utility class for accessing user context throughout the application:
- Retrieves UserContext from request-scoped attributes
- Provides convenience methods for common user properties
- Returns safe default values for public endpoints
- Thread-safe via Spring's RequestContextHolder

### [UserContext](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/model/UserContext.java)
Immutable user session data:
- Contains user identification (userId, username, email)
- Stores role-based permissions
- Provides permission check methods
- Includes logging helper methods

### [AuthenticationProperties](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/config/AuthenticationProperties.java)
Configuration properties for authentication settings:
- Defines role group mappings
- Configures JWT claim names
- Lists public endpoints
- Supports environment-based configuration

### [AuthenticationConfig](../../endpoint-insights-api/src/main/java/com/vsp/endpointinsightsapi/config/AuthenticationConfig.java)
Registers the authorization interceptor:
- Applies interceptor to `/api/**` endpoints
- Can be disabled via configuration
- Integrates with Spring MVC

## Configuration

### Access Control
```yaml
app.authentication:
  groups:
    write: "ei:write"  # Full access to all endpoints
    read: "ei:read"    # Read-only access
  claims:
    username: preferred_username
    email: email
    groups: groups
```

## Security Features

### Token Validation
- JWT signature verified against OIDC provider's JWKS
- Required claims validated (subject, username, email)
- Token expiration enforced
- Malformed tokens rejected with 401

### Role-Based Authorization
- Users assigned roles based on group membership
- `WRITE` role: Full API access
- `READ` role: Read-only operations
- `NONE` role: Access denied (403)

### Public Endpoints
Certain endpoints bypass authentication:
- `/api/health` - Health check endpoint

## Usage

### Client Implementation
Include JWT token in requests:
```javascript
fetch('/api/endpoint', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

### User Context Access
Backend components can access current user information anywhere in the application:

```java
public void SomeFunction() {
    Optional<UserContext> userContext = CurrentUser.get();
    if (userContext.isPresent()) {
        UserContext user = userContext.get();
        String email = user.getEmail();
        UserRole role = user.getRole();
    }

    // Convenience methods for common operations
    String userId = CurrentUser.getUserId();        // Returns "system" if no user
    String username = CurrentUser.getUsername();    // Returns "system" if no user
    String logId = CurrentUser.getLogIdentifier();  // username + userId for logging

    boolean canWrite = CurrentUser.hasWriteAccess();
    boolean canRead = CurrentUser.hasReadAccess();
}

```

Additionally, controller methods can be annotated with @RequiredRoles() to specify roles required to access an endpoint for broad control.
This prevents us from needing to write full role validation in every method.

### Example Usage in Controllers
```java
@RequiredRoles(roles = {UserRole.WRITE})
@RestController
public class SomeController {
    @PostMapping("/api/data")
    public ResponseEntity<String> createData(@RequestBody DataRequest request) {
        log.info("User {} creating new data record", CurrentUser.getLogIdentifier());

        String createdBy = CurrentUser.getUsername();

        // Process request...

        return ResponseEntity.ok("Data created");
    }
}
```

## Testing

### Testing JWT Authorization

#### 1. Authenticate and Obtain Token
First, authenticate via the OIDC flow to obtain a JWT token:
```bash
# Start the authentication flow (will redirect to identity provider)
curl -v http://localhost:8080/oauth2/authorization/oidc
```

After completing the OIDC flow in a browser, the success handler returns a JSON response:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": 1234567890,
  "username": "testuser",
  "email": "test@example.com"
}
```

#### 2. Test Protected Endpoints with Token
```bash
# Set the token as an environment variable
export TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Test authorized request (should return 200)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/batches

# Test with write access (if user has ei:write group)
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "test"}' \
  http://localhost:8080/api/batches
```

#### 3. Test Authorization Failures
```bash
# Test without token (should return 401)
curl -v http://localhost:8080/api/batches

# Test with invalid token (should return 401)
curl -H "Authorization: Bearer invalid.token.here" \
  http://localhost:8080/api/batches

# Test with expired token (should return 401)
curl -H "Authorization: Bearer $EXPIRED_TOKEN" \
  http://localhost:8080/api/batches
```

#### 4. Test Public Endpoints
```bash
# Health endpoint should work without authentication
curl http://localhost:8080/api/health
```

#### 5. Test Role-Based Access
```bash
# User with READ role trying to write (should return 403)
# This requires implementing write-only endpoints with permission checks
curl -X POST -H "Authorization: Bearer $READ_ONLY_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "test"}' \
  http://localhost:8080/api/batches
```

### Expected Responses

**Successful Authorization (200)**
```json
{
  "data": "..."
}
```

**Missing/Invalid Token (401)**
```json
{
  "error": "Unauthorized",
  "status": 401
}
```

**Insufficient Permissions (403)**
```json
{
  "error": "Forbidden",
  "status": 403
}
```