package com.exe201.project.dto.request;

import com.exe201.project.enums.DurationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MembershipRequest(
        @NotBlank(message = "Membership name is required")
        @Size(min = 2, max = 50, message = "Membership name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Membership description is required")
        @Size(min = 2, max = 300, message = "Membership description must be between 2 and 300 characters")
        String description,

        @NotNull(message = "Membership price is required")
        @Min(value = 0, message = "Price must be larger than 0")
        Double price,

        @NotNull(message = "Membership duration is required")
        @Min(value = 0, message = "Duration must be larger than 0")
        Double duration,

        @NotNull(message = "Membership type is required")
        DurationType type,
        
        @Valid
        List<MembershipFeatureRequest> features
) {
}
