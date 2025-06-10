package com.exe201.project.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkCategoryResponse(
        List<CategoryResponse> createdCategories,
        int totalRequested,
        int totalCreated,
        List<String> skippedCategories,
        String message
) {
}
