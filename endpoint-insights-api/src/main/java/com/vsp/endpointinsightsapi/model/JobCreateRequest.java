package com.vsp.endpointinsightsapi.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobCreateRequest {

	@NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    // @NotBlank(message = "createdBy is required")
    // @Size(max = 100, message = "createdBy must be at most 100 characters")
    // private String createdBy;

    // @NotBlank(message = "testType is required")
    // @Size(max = 50, message = "testType must be at most 50 characters")
    // private String testType;
}
