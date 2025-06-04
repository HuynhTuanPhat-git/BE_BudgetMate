package com.exe201.project.dto.request;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {
}
