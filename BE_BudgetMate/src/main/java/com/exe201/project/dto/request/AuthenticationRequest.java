package com.exe201.project.dto.request;

public record AuthenticationRequest(
        String email,
        String password
) {
}
