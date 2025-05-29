package com.exe201.project.controller;

import com.exe201.project.dto.request.AuthenticationRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successful")
                .data(authenticationService.login(request))
                .build();
    }

//    @PostMapping("/refresh")
//    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request){
//        return ApiResponse.<AuthenticationResponse>builder()
//                .message("Token refreshed")
//                .data(authenticationService.refreshToken(request))
//                .build();
//    }

}