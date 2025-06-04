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
import com.exe201.project.service.CloudinaryService;
import com.exe201.project.service.EmailService;
import com.exe201.project.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

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
        String email = jwtUtils.getEmailFromJwtToken(token);
        Date expirationDate = jwtUtils.getExpDateFromToken(token);
        if(!expirationDate.before(new Date())){
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setStatus(UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        }else{
            throw new RuntimeException("Time to verify email is expired");
        }
    }

    @Override
    public UserResponse getAuthenticatedUserDTO() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    public User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponse updateUserProfile(
            String fullName,
            String phone,
            String address,
            MultipartFile file
    ) throws IOException {
        User user = getAuthenticatedUser();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        if(!file.isEmpty()){
            Map url = cloudinaryService.upload(file);
            String avatarUrl = (String) url.get("url");
            user.setAvatar(avatarUrl);
        }
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updatePassword(String oldPassword, String newPassword) {
        User user = getAuthenticatedUser();
        if(passwordEncoder.matches(oldPassword, user.getPassword())){
            user.setPassword(passwordEncoder.encode(newPassword));
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        }else{
            throw new RuntimeException("Old password is incorrect");
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateByUserId(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public void activateByUserId(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void banByUserId(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(user.getStatus().equals(UserStatus.BANNED)){
            throw new RuntimeException("User is banned");
        }else if(user.getStatus().equals(UserStatus.INACTIVE)){
            throw new RuntimeException("User is not active");
        }
        emailService.sendEmail(email, emailService.subjectResetPassword(), emailService.bodyResetPassword(email));
    }

    @Override
    public UserResponse resetPassword(String token, String newPassword) {
        String email = jwtUtils.getEmailFromJwtToken(token);
        Date expirationDate = jwtUtils.getExpDateFromToken(token);
        if(!expirationDate.before(new Date())){
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setPassword(passwordEncoder.encode(newPassword));
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        }else{
            throw new RuntimeException("Time to reset password is expired");
        }
    }
}
