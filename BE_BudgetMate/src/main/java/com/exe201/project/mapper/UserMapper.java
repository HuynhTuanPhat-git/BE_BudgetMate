package com.exe201.project.mapper;

import com.exe201.project.dto.response.UserResponse;
import com.exe201.project.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .streakDays(user.getStreakDays())
                .lastLoginDate(user.getLastLoginDate())
                .credits(user.getCredits())
                .status(user.getStatus())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .petId(user.getPet() != null ? user.getPet().getId() : null)
                .petName(user.getPet() != null ? user.getPet().getName() : null)
                .build();
    }

}
