package com.exe201.project.dto.request.question;

import lombok.Builder;

@Builder
public record QuestionSearchRequest(
        String keyword,
        Boolean isActive,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size
) {
    public QuestionSearchRequest {
        if (sortBy == null) sortBy = "createdAt";
        if (sortDirection == null) sortDirection = "desc";
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
    }
}
