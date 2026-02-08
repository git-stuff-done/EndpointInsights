package com.vsp.endpointinsightsapi.model;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobCreateRequest {

	@NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    private String description;

    private String gitUrl;

    private String runCommand;

    private String compileCommand;

    @NotBlank(message = "testType is required")
    private TestType testType;

    private Map<String, Object> config;

    // @NotBlank(message = "createdBy is required")
    // @Size(max = 100, message = "createdBy must be at most 100 characters")
    // private String createdBy;
}
