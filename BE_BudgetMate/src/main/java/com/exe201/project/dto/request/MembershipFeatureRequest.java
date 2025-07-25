package com.exe201.project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MembershipFeatureRequest(
        @NotNull(message = "Feature ID is required")
        Long featureId,
        
        @Min(value = 0, message = "Limit value must be 0 or greater (0 = not allowed, null = unlimited)")
        Integer limitValue,
        
        Boolean isEnabled,
        
        String description,

        Integer creditPrice
) {
}
