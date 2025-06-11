package com.exe201.project.controller;

import com.exe201.project.dto.request.AuthenticationRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.AuthenticationResponse;
import com.exe201.project.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

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