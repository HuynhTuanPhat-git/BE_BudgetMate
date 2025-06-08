package com.exe201.project.controller;

import com.exe201.project.dto.request.ForgotPasswordRequest;
import com.exe201.project.dto.request.ResetPasswordRequest;
import com.exe201.project.dto.request.UserCreationRequest;
import com.exe201.project.dto.request.VerifyUserRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.UserResponse;
import com.exe201.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<?>> registerUser(
            @Valid @RequestBody UserCreationRequest request
    ) throws MessagingException {
        UserResponse userDTO = userService.register(request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("User registered")
                        .data(userDTO)
                        .build()
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(@RequestBody VerifyUserRequest request){
        UserResponse userDTO = userService.verifyEmail(request.token());
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .message("User verified")
                        .data(userDTO)
                        .build()
        );
    }

    @Operation(summary = "Get the current authenticated user")
    @GetMapping("/c")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> getAuthUser(){
        UserResponse userDTO = userService.getAuthenticatedUserDTO();
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                .message("User profile retrieved")
                .data(userDTO)
                .build()
        );
    }

    @PostMapping("/c/update")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("avatar") MultipartFile avatar
    ) throws IOException {
        UserResponse userDTO = userService.updateUserProfile(fullName, phone, address, avatar);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                .message("User profile updated")
                .data(userDTO)
                .build()
        );
    }

    @PostMapping("/c/update/password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> updatePassword(@RequestParam("oldPassword") String oldPassword,
                                               @RequestParam("newPassword") String newPassword){
        UserResponse userDTO = userService.updatePassword(oldPassword, newPassword);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .message("Password updated")
                        .data(userDTO)
                        .build()
        );
    }


    @GetMapping("/admin/getAll")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        List<UserResponse> userDTOs = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .message("Show all users")
                        .data(userDTOs)
                        .build()
        );
    }

    @DeleteMapping("/admin/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> deactivateUser(@RequestParam("user") int id){
        userService.deactivateByUserId(id);
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .message("Deactivate user successfully")
                        .build()
        );
    }

    @PutMapping("/admin/activate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> activateUser(@RequestParam("user") int id){
        userService.activateByUserId(id);
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                .message("Activate user successfully")
                .build()
        );
    }

    @PutMapping("/admin/ban")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> banUser(@RequestParam("user") int id){
        userService.banByUserId(id);
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                .message("Deactivate user successfully")
                .build()
        );
    }

    @PostMapping("/forgot-password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) throws MessagingException {
        userService.forgotPassword(request.email());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                .message("Email sent")
                .data(null)
                .build()
        );
    }

    @PostMapping("/reset-password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(@RequestBody ResetPasswordRequest request){
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                .message("Password reset")
                .data(userService.resetPassword(request.token(), request.newPassword()))
                .build()
        );
    }
}
