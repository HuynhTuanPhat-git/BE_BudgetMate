package com.exe201.project.dto.response;

import lombok.Builder;

@Builder
public record FeatureResponse(
        Long id,
        String name,
        String description,
        String featureKey,
        Boolean isActive
) {
}
