package com.exe201.project.dto.response;

import lombok.Builder;

@Builder
public record MembershipFeatureResponse(
        Long id,
        FeatureResponse feature,
        Integer limitValue,
        Boolean isEnabled,
        String description
) {
}
