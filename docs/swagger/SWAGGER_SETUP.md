# Swagger/OpenAPI Setup Guide

This guide explains the Swagger/OpenAPI setup for the Endpoint Insights API.

## What's Been Set Up

### 1. SpringDoc OpenAPI Dependency
Added `springdoc-openapi-starter-webmvc-ui` v2.4.0 to `pom.xml`. This library:
- Automatically generates OpenAPI 3.0 specification from your code
- Provides the Swagger UI interface
- Scans Spring controllers and generates documentation

### 2. OpenAPI Configuration (`OpenApiConfig.java`)
A Spring configuration class that:
- Defines API metadata (title, version, description)
- Sets up contact information and license
- Configures OAuth2 Bearer token security scheme for JWT

### 3. Application Configuration
Updated `application.yaml` with Springdoc settings:
- Swagger UI at `/api`
- OpenAPI specs at `/api/docs` and `/api/docs.yaml`
- UI customizations (sorting, Try-it-out enabled, etc.)

### 4. Example Controller Annotations
Enhanced `HealthController.java` with:
- `@Tag` - Groups endpoints by category
- `@Operation` - Describes endpoint purpose
- `@ApiResponse` - Documents response codes and descriptions

## Accessing the API Documentation

### 1. Interactive Swagger UI
```
http://localhost:8080/swagger-ui.html
```
Features:
- Browse all endpoints organized by tags
- Click "Try it out" to test endpoints
- Click "Authorize" to add OAuth2 tokens
- View request/response schemas

### 2. Machine-Readable OpenAPI Specs
```
JSON: http://localhost:8080/api/docs
YAML: http://localhost:8080/api/docs.yaml
```
Use these to import into API tools like Postman or Insomnia.

## Common Swagger Annotations

### Controller Level
```java
@RestController
@RequestMapping("/api/resource")
@Tag(name = "Resources", description = "Resource management endpoints")
public class ResourceController {
    // ... endpoints
}
```

### Method Level
```java
@GetMapping("/{id}")
@Operation(summary = "Get resource by ID", description = "Retrieves a specific resource")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Resource found"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
public ResponseEntity<ResourceDTO> getResource(@PathVariable UUID id) {
    // ...
}
```

### Parameter Documentation
```java
@GetMapping
public ResponseEntity<List<ResourceDTO>> list(
    @Parameter(description = "Resource name filter", example = "Test")
    @RequestParam(required = false) String name,
    
    @Parameter(description = "Page number", example = "0")
    @RequestParam(defaultValue = "0") int page
) {
    // ...
}
```

### DTO Documentation
```java
@Schema(description = "Response data transfer object for a resource")
@Data
public class ResourceDTO {

    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Resource name", example = "Production API", minLength = 1, maxLength = 255)
    private String name;

    @Schema(description = "Number of items", example = "42", minimum = "0")
    private Integer itemCount;
}
```

## Adding Documentation to Your Controllers

### Templates to Follow

Based on `HealthController.java`, follow this pattern for each controller:

```java
@RestController
@RequestMapping("/api/your-resource")
@Tag(name = "YourResource", description = "Your resource management")
public class YourResourceController {

    @PostMapping
    @Operation(summary = "Create a new resource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resource created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResourceDTO> create(@RequestBody CreateRequest request) {
        // Implementation
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resource found"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResourceDTO> getById(@PathVariable UUID id) {
        // Implementation
    }
}
```

## Supported HTTP Status Codes

| Code | Annotation | Meaning |
|------|-----------|---------|
| 200 | `@ApiResponse(responseCode = "200")` | OK - Request successful |
| 201 | `@ApiResponse(responseCode = "201")` | Created - Resource created |
| 204 | `@ApiResponse(responseCode = "204")` | No Content - Successful deletion |
| 400 | `@ApiResponse(responseCode = "400")` | Bad Request - Validation error |
| 401 | `@ApiResponse(responseCode = "401")` | Unauthorized - Auth required |
| 403 | `@ApiResponse(responseCode = "403")` | Forbidden - Insufficient permissions |
| 404 | `@ApiResponse(responseCode = "404")` | Not Found - Resource doesn't exist |
| 409 | `@ApiResponse(responseCode = "409")` | Conflict - Resource already exists |
| 500 | `@ApiResponse(responseCode = "500")` | Internal Server Error |

## Integrating with API Clients

The simplest way to test the API is to use the **Swagger UI directly**:

1. Navigate to `http://localhost:8080/api`
2. Find the endpoint you want to test
3. Click **"Try it out"**
4. Fill in any parameters
5. Click **"Execute"**

You can also add authentication by clicking the **"Authorize"** button and entering your OAuth2 Bearer token.

### Optional: Import into Postman/Insomnia (Advanced)
If you prefer to work in Postman or Insomnia:

**Postman:**
1. Open Postman
2. Click File → Import
3. Select "Link" tab
4. Enter: `http://localhost:8080/api/docs`
5. Click Import

**Insomnia:**
1. Open Insomnia
2. Click Create → Import → From URL
3. Enter: `http://localhost:8080/api/docs`
4. Click Import

## Production Configuration

To disable Swagger UI in production, add to `application-prod.yaml`:
```yaml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Swagger UI shows 404 | Ensure app is running on correct port (default 8080) |
| Endpoints not appearing | Add `@RestController` to controller class |
| Missing endpoint in UI | Verify `@GetMapping`, `@PostMapping`, etc. are present |
| OAuth2 not working | Check `OpenApiConfig.java` and OIDC config in `application.yaml` |
| Fields missing in schema | Add `@Schema` annotations to DTO fields |

## Files Modified/Created

| File | Change |
|------|--------|
| `pom.xml` | Added springdoc-openapi dependency |
| `config/OpenApiConfig.java` | Created - OpenAPI configuration bean |
| `application.yaml` | Added Springdoc configuration section |
| `controller/HealthController.java` | Enhanced with Swagger annotations |
| `docs/SWAGGER_README.md` | Quick start guide |
| `docs/SWAGGER_SETUP.md` | This file |

## Next Steps

1. **Add annotations to other controllers** (Batches, Jobs, TestRuns, Dashboard)
2. **Document DTOs** with `@Schema` annotations
3. **Test in Swagger UI** - verify all endpoints appear correctly
4. **Export OpenAPI spec** for sharing with frontend team

---

**Setup Date**: April 2, 2026
**Status**: Ready to use
**OpenAPI Version**: 3.0
**SpringDoc Version**: 2.4.0
