package com.exe201.project.service;

import com.exe201.project.dto.request.UserCreationRequest;
import com.exe201.project.dto.response.UserResponse;
import com.exe201.project.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    UserResponse register(UserCreationRequest request) throws MessagingException;

    UserResponse verifyEmail(String token);

    UserResponse getAuthenticatedUserDTO();

    User getAuthenticatedUser();

    public UserResponse updateUserProfile(
            String fullName,
            String phone,
            String address,
            MultipartFile file
    ) throws IOException;

    UserResponse updatePassword(String oldPassword, String newPassword);

    List<UserResponse> getAllUsers();

    void deactivateByUserId(long id);

    void activateByUserId(long id);

    void banByUserId(long id);

    void forgotPassword(String email) throws MessagingException;

    UserResponse resetPassword(String token, String newPassword);
}
