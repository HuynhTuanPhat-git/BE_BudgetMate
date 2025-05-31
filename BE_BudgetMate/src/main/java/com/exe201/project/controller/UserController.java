package com.exe201.project.controller;

import com.exe201.project.dto.request.UserCreationRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.UserResponse;
import com.exe201.project.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> registerUser(@Valid @RequestBody UserCreationRequest request) throws MessagingException {
        UserResponse userDTO = userService.register(request);
        return ApiResponse.<UserResponse>builder()
                .message("User registered")
                .data(userDTO)
                .build();
    }

//    @PostMapping("/verify")
//    public ApiResponse<UserDTO> verifyUser(@RequestBody VerifyUserRequest request){
//        UserDTO userDTO = userService.verifyEmail(request.getToken());
//        return ApiResponse.<UserDTO>builder()
//                .message("User verified")
//                .data(userDTO)
//                .build();
//    }
//
//    @Operation(summary = "Get the current authenticated user")
//    @GetMapping("/p")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<UserDTO> getAuthUser(){
//        UserDTO userDTO = userService.getAuthenticatedUserDTO();
//        return ApiResponse.<UserDTO>builder()
//                .message("User profile retrieved")
//                .data(userDTO)
//                .build();
//    }
//
//    @PostMapping("/p/update")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<UserDTO> updateUserProfile(
//            @RequestParam("firstName") String firstName,
//            @RequestParam("lastName") String lastName,
//            @RequestParam("phone") String phone,
//            @RequestParam("address") String address,
//            @RequestParam("avatar") MultipartFile avatar
//    ) throws IOException {
//        UserDTO userDTO = userService.updateUserProfile(firstName, lastName, phone, address, avatar);
//        return ApiResponse.<UserDTO>builder()
//                .message("User profile updated")
//                .data(userDTO)
//                .build();
//    }
//
//    @PostMapping("/p/update/password")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<UserDTO> updatePassword(@RequestParam("oldPassword") String oldPassword,
//                                               @RequestParam("newPassword") String newPassword){
//        UserDTO userDTO = userService.updatePassword(oldPassword, newPassword);
//        return ApiResponse.<UserDTO>builder()
//                .message("Password updated")
//                .data(userDTO)
//                .build();
//    }
//
//
//    @GetMapping("/admin/getAll")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<List<UserDTO>> getAllUsers(){
//        List<UserDTO> userDTOs = userService.getAllUsers();
//        return ApiResponse.<List<UserDTO>>builder()
//                .message("Show all users")
//                .data(userDTOs)
//                .build();
//    }
//
//    @DeleteMapping("/admin/deactivate")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<?> deactivateUser(@RequestParam("user") int id){
//        userService.deactivateByUserId(id);
//        return ApiResponse.<List<UserDTO>>builder()
//                .message("Deactivate user successfully")
//                .build();
//    }
//
//    @PutMapping("/admin/activate")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<?> activateUser(@RequestParam("user") int id){
//        userService.activateByUserId(id);
//        return ApiResponse.<List<UserDTO>>builder()
//                .message("Activate user successfully")
//                .build();
//    }
//
//    @PutMapping("/admin/ban")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<?> banUser(@RequestParam("user") int id){
//        userService.banByUserId(id);
//        return ApiResponse.<List<UserDTO>>builder()
//                .message("Deactivate user successfully")
//                .build();
//    }
//
//    @PutMapping("/admin/unban")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<?> unbanUser(@RequestParam("user") int id){
//        userService.unbanByUserId(id);
//        return ApiResponse.<List<UserDTO>>builder()
//                .message("Deactivate user successfully")
//                .build();
//    }
//
//    @PostMapping("/forgot-password")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) throws MessagingException {
//        userService.forgotPassword(request.getEmail());
//        return ApiResponse.<Void>builder()
//                .message("Email sent")
//                .data(null)
//                .build();
//    }
//
//    @PostMapping("/reset-password")
//    @SecurityRequirement(name = "bearerAuth")
//    public ApiResponse<UserDTO> resetPassword(@RequestBody ResetPasswordRequest request){
//        return ApiResponse.<UserDTO>builder()
//                .message("Password reset")
//                .data(userService.resetPassword(request.getToken(), request.getNewPassword()))
//                .build();
//    }
}
