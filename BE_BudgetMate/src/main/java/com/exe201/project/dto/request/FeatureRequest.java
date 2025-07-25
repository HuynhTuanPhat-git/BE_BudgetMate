package com.exe201.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeatureRequest(
        @NotBlank(message = "Feature name is required")
        @Size(min = 2, max = 100, message = "Feature name must be between 2 and 100 characters")
        String name,
        
        @NotBlank(message = "Feature description is required")
        @Size(min = 10, max = 500, message = "Feature description must be between 10 and 500 characters")
        String description,
        
        @NotBlank(message = "Feature key is required")
        @Size(min = 2, max = 50, message = "Feature key must be between 2 and 50 characters")
        String featureKey,
        
        Boolean isActive
) {
}
