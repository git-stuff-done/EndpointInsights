# Swagger/OpenAPI REST API Documentation - Quick Start

## Access the Swagger UI

Once your Spring Boot application is running, open your browser and go to:
```
https://d2wravsw1nwfu2.cloudfront.net/swagger-ui.html
```

You should see an interactive API documentation interface with all your endpoints.

## Get the OpenAPI Specification

### JSON Format
```
https://d2wravsw1nwfu2.cloudfront.net/api/docs
```

### YAML Format
```
https://d2wravsw1nwfu2.cloudfront.net/api/docs.yaml
```

## What's Available

### Health Check Endpoints (Example)
These endpoints are documented with Swagger annotations:
- `GET /api/health` - Public health check (no auth required)
- `GET /api/health-secure` - Secure health check (requires READ or WRITE role)

### Features
-  Interactive "Try it out" functionality to test endpoints
-  "Authorize" button to add OAuth2 Bearer tokens
-  Response examples and schemas
-  HTTP status codes documented
-  Endpoints organized by tags

## Next Steps

To expand Swagger documentation to other controllers:

1. Add `@Tag` annotation to the controller class
2. Add `@Operation` annotation to each endpoint method
3. Add `@ApiResponses` to document possible HTTP status codes
4. Add `@Parameter` annotations for request parameters
5. Add `@Schema` annotations to DTO classes

See `SWAGGER_SETUP.md` for detailed guidance on annotations.

---

