package com.exe201.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCategoryRequest(
        @NotEmpty(message = "Categories list cannot be empty")
        @Valid
        List<CategoryRequest> categories
) {
}
