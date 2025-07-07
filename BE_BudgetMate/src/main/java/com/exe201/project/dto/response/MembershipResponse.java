package com.exe201.project.dto.response;

import com.exe201.project.enums.DurationType;
import com.exe201.project.enums.Status;
import lombok.Builder;

import java.util.List;

@Builder
public record MembershipResponse(
        Long id,
        String name,
        String description,
        Double price,
        Double duration,
        DurationType type,
        Status status,
        List<MembershipFeatureResponse> features
) {
}
