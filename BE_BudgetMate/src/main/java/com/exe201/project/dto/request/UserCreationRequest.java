package com.exe201.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserCreationRequest(
        @NotEmpty(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotEmpty(message = "Password is required")
        String password,

        @NotEmpty(message = "Full name is required")
        String fullName,

        @NotEmpty(message = "Phone number is required")
        String phone,

        @NotEmpty(message = "Address is required")
        String address
) {
}
