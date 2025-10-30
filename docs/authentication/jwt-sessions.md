# JWT Session Management

## Overview

The system uses stateless JWT-based sessions for API authorization. After OIDC authentication, clients receive a JWT token that must be included in subsequent API requests for authorization.

## Session Flow

### Request Authorization
1. Client includes JWT in `Authorization: Bearer <token>` header
2. `AuthenticationInterceptor` validates token signature using JWKS
3. System extracts user context (username, email, role)
4. Request proceeds if user has required permissions

## Components

### AuthenticationInterceptor
Validates JWT tokens on API requests:
- Decodes and validates JWT signature
- Extracts user context from token claims
- Enforces role-based access control
- Sets up `UserContext` for request processing

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
  endpoints:
    public-endpoints:
      - "/api/health"
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
// Get the full user context (optional)
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

// Permission checks
boolean canWrite = CurrentUser.hasWriteAccess();
boolean canRead = CurrentUser.hasReadAccess();
```

### Example Usage in Controllers
```java
@RestController
public class SomeController {

    @PostMapping("/api/data")
    public ResponseEntity<String> createData(@RequestBody DataRequest request) {
        // Check permissions
        if (!CurrentUser.hasWriteAccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Log with user context
        log.info("User {} creating new data record", CurrentUser.getLogIdentifier());

        // Access user info for business logic
        String createdBy = CurrentUser.getUsername();
        // ... process request

        return ResponseEntity.ok("Data created");
    }
}

## Benefits

- **Stateless**: No server-side session storage required
- **Scalable**: Tokens validated locally without provider roundtrips
- **Fast**: Local validation using cached JWKS
- **Auditable**: User context available throughout request lifecycle
- **Configurable**: Flexible role mapping via group claims