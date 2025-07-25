package com.exe201.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Category color is required")
        @Pattern(
                regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$",
                message = "Color must be in hex format #rrggbb (e.g., #007bff)"
        )
        String color
) {
}
