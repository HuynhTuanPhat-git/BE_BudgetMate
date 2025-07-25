package com.exe201.project.dto.response;

import lombok.Builder;

@Builder
public record MembershipFeatureResponse(
        Long id,
        FeatureResponse feature,
        Integer limitValue, // null = unlimited, 0 = not allowed, >0 = specific limit
        Boolean isEnabled,
        String description
) {
}
