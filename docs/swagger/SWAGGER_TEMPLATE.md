# Swagger Controller Template

## Quick Reference for Adding Swagger to Your Endpoints

### Step 1: Add Controller-Level Annotations

```java
@RestController
@RequestMapping("/api/your-resource")
@Tag(name = "YourResource", description = "Description of what this controller manages")
public class YourResourceController {
    // ... methods
}
```

### Step 2: Add Annotations to Each Endpoint

#### GET Endpoint Example
```java
@GetMapping("/{id}")
@Operation(
    summary = "Get resource by ID",
    description = "Retrieves a single resource by its unique identifier"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Resource found"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
public ResponseEntity<ResourceDTO> getById(
    @Parameter(description = "Resource ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id
) {
    // Implementation
}
```

#### POST Endpoint Example
```java
@PostMapping
@Operation(
    summary = "Create new resource",
    description = "Creates a new resource with the provided configuration"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Resource created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
    @ApiResponse(responseCode = "409", description = "Resource already exists"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
public ResponseEntity<ResourceDTO> create(
    @RequestBody CreateResourceRequest request
) {
    // Implementation
}
```

#### PUT Endpoint Example
```java
@PutMapping("/{id}")
@Operation(
    summary = "Update resource",
    description = "Updates an existing resource with new data"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Resource updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
public ResponseEntity<ResourceDTO> update(
    @PathVariable UUID id,
    @RequestBody UpdateResourceRequest request
) {
    // Implementation
}
```

#### DELETE Endpoint Example
```java
@DeleteMapping("/{id}")
@Operation(
    summary = "Delete resource",
    description = "Permanently deletes a resource by its ID"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Resource deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
public ResponseEntity<Void> delete(
    @Parameter(description = "Resource ID", required = true)
    @PathVariable UUID id
) {
    // Implementation
}
```

### Step 3: Document Your DTOs

```java
@Schema(description = "Request object for creating a resource")
@Data
public class CreateResourceRequest {

    @Schema(
        description = "Name of the resource",
        example = "My Resource",
        minLength = 1,
        maxLength = 255,
        required = true
    )
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(
        description = "Description of the resource",
        example = "A sample resource for testing",
        maxLength = 1000
    )
    private String description;

    @Schema(
        description = "Status of the resource",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE", "ARCHIVED"}
    )
    private String status;

    @Schema(
        description = "Whether the resource is enabled",
        example = "true"
    )
    private Boolean enabled;
}
```

### Step 4: Document Response DTOs

```java
@Schema(description = "Response data for a resource")
@Data
public class ResourceDTO {

    @Schema(
        description = "Unique resource identifier",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    private UUID id;

    @Schema(
        description = "Resource name",
        example = "My Resource",
        minLength = 1,
        maxLength = 255
    )
    private String name;

    @Schema(
        description = "Resource description",
        example = "A sample resource"
    )
    private String description;

    @Schema(
        description = "Current status",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE", "ARCHIVED"}
    )
    private String status;

    @Schema(
        description = "Whether the resource is enabled",
        example = "true"
    )
    private Boolean enabled;

    @Schema(
        description = "When the resource was created",
        example = "2024-01-15T10:30:00",
        required = true
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "When the resource was last updated",
        example = "2024-01-16T14:45:00"
    )
    private LocalDateTime updatedAt;
}
```

## Complete Controller Template

```java
package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
@Tag(name = "Batches", description = "Test batch management and execution endpoints")
public class BatchesController {

    @GetMapping
    @Operation(summary = "List all test batches")
    @ApiResponse(responseCode = "200", description = "Batches retrieved successfully")
    public ResponseEntity<List<BatchResponseDTO>> listBatches(
        @Parameter(description = "Filter by batch name")
        @RequestParam(required = false) String batchName
    ) {
        // Implementation
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get batch by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Batch found"),
        @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public ResponseEntity<BatchResponseDTO> getBatch(
        @Parameter(description = "Batch ID", required = true)
        @PathVariable UUID id
    ) {
        // Implementation
        return ResponseEntity.ok(new BatchResponseDTO());
    }

    @PostMapping
    @Operation(summary = "Create new batch")
    @ApiResponse(responseCode = "201", description = "Batch created successfully")
    public ResponseEntity<BatchResponseDTO> createBatch(
        @RequestBody BatchRequestDTO request
    ) {
        // Implementation
        return ResponseEntity.status(HttpStatus.CREATED).body(new BatchResponseDTO());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update batch")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Batch updated"),
        @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public ResponseEntity<BatchResponseDTO> updateBatch(
        @PathVariable UUID id,
        @RequestBody BatchRequestDTO request
    ) {
        // Implementation
        return ResponseEntity.ok(new BatchResponseDTO());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete batch")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Batch deleted"),
        @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public ResponseEntity<Void> deleteBatch(
        @Parameter(description = "Batch ID", required = true)
        @PathVariable UUID id
    ) {
        // Implementation
        return ResponseEntity.noContent().build();
    }
}
```

## Key Annotations Reference

| Annotation | Usage | Example |
|-----------|-------|---------|
| `@Tag` | Group endpoints | `@Tag(name = "Batches")` |
| `@Operation` | Describe method | `@Operation(summary = "Get batch")` |
| `@ApiResponse` | Single response | `@ApiResponse(responseCode = "200")` |
| `@ApiResponses` | Multiple responses | `@ApiResponses(value = {...})` |
| `@Parameter` | Describe parameter | `@Parameter(description = "ID")` |
| `@Schema` | Describe DTO field | `@Schema(example = "value")` |
| `@RequestBody` | Request body | `@RequestBody CreateRequest req` |

## Checklist for Adding Swagger Docs

- [ ] Add `@Tag` to controller class
- [ ] Add `@Operation` with summary to each method
- [ ] Add `@ApiResponses` with all possible HTTP codes
- [ ] Add `@Parameter` to query/path parameters
- [ ] Add `@Schema` to DTO classes
- [ ] Include examples in schemas
- [ ] Specify constraints (minLength, maxLength, etc.)
- [ ] Test in Swagger UI: `http://localhost:8080/api`

## Example Flow

1. **Open Swagger UI**: http://localhost:8080/api
2. **Find your endpoint** under the tag you created
3. **Click "Try it out"** button
4. **Fill in parameters** with sample data
5. **Click "Execute"** to test
6. **View response** with status code and data

---

Start with `HealthController.java` as a reference, then apply the same pattern to your other controllers!
