package com.exe201.project.dto.request;

public record UserCreationRequest(
        String email,
        String password,
        String fullName,
        String phone,
        String address
) {
}
