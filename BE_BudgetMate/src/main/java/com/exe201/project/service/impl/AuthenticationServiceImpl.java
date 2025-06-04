package com.exe201.project.service.impl;

import com.exe201.project.configuration.security.jwt.JwtUtils;
import com.exe201.project.configuration.security.jwt.UserDetailsImpl;
import com.exe201.project.configuration.security.jwt.UserDetailsServiceImpl;
import com.exe201.project.dto.request.AuthenticationRequest;
import com.exe201.project.dto.response.AuthenticationResponse;
import com.exe201.project.entity.User;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.exception.InactiveUserException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getStatus().equals(UserStatus.INACTIVE))
            throw new InactiveUserException("Account have not been activated yet!");
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateJwtToken(userDetails);
        return new AuthenticationResponse(token);

    }

//    @Override
//    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
//        var token = request.getAccessToken();
//        Date refreshableDate = new Date(
//                jwtUtils.getExpDateFromToken(token)
//                        .toInstant()
//                        .plus(7, ChronoUnit.DAYS)
//                        .toEpochMilli()
//        );
//        if(refreshableDate.after(new Date())){
//            String email = jwtUtils.getEmailFromJwtToken(token);
//            String newToken = jwtUtils.generateTokenFromUsername(email);
//            log.info("Token has been refreshed");
//            return new AuthenticationResponse(newToken);
//        }else{
//            throw new RuntimeException("Token is expired");
//        }
//    }
}
