package com.exe201.project.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken
) {
}
