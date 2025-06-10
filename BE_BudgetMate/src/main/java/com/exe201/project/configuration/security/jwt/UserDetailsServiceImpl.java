package com.exe201.project.configuration.security.jwt;

import com.exe201.project.entity.User;
import com.exe201.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Invalid Username or Password"));
        if (user.getStatus().equals("INACTIVE")) {
            throw new RuntimeException("Account is not active");
        } else if (user.getStatus().equals("BANNED")) {
            throw new RuntimeException("Account is banned");
        }
        return UserDetailsImpl.build(user);
    }
}
