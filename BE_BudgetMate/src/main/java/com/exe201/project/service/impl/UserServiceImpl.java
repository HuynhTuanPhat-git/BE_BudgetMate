package com.exe201.project.service.impl;

import com.exe201.project.configuration.security.jwt.JwtUtils;
import com.exe201.project.dto.request.UserCreationRequest;
import com.exe201.project.dto.response.UserResponse;
import com.exe201.project.entity.Role;
import com.exe201.project.entity.User;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.exception.ResourceAlreadyExistException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.UserMapper;
import com.exe201.project.repository.RoleRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.EmailService;
import com.exe201.project.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserCreationRequest request) throws MessagingException {
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new ResourceAlreadyExistException("Email already in used");
        }else if(userRepository.findByPhone(request.phone()).isPresent()){
            throw new ResourceAlreadyExistException("Phone already in used");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setStatus(UserStatus.INACTIVE);
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        emailService.sendEmail(
                savedUser.getEmail(),
                emailService.subjectRegister(),
                emailService.bodyRegister(
                        savedUser.getEmail(),
                        savedUser.getFullName(),
                        savedUser.getPhone(),
                        savedUser.getAddress())
        );
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse verifyEmail(String token) {
        return null;
    }

    @Override
    public UserResponse getAuthenticatedUserDTO() {
        return null;
    }

    @Override
    public UserResponse getAuthenticatedUser() {
        return null;
    }

    @Override
    public UserResponse updateUserProfile(String firstName, String lastName, String phone, String address, MultipartFile file) throws IOException {
        return null;
    }

    @Override
    public UserResponse updatePassword(String oldPassword, String newPassword) {
        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public void deactivateByUserId(int id) {

    }

    @Override
    public void activateByUserId(int id) {

    }

    @Override
    public void banByUserId(int id) {

    }

    @Override
    public void unbanByUserId(int id) {

    }

    @Override
    public void forgotPassword(String email) throws MessagingException {

    }

    @Override
    public UserResponse resetPassword(String token, String newPassword) {
        return null;
    }
}
