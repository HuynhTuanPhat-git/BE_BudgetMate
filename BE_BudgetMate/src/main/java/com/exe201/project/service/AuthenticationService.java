package com.exe201.project.service;

import com.exe201.project.dto.request.AuthenticationRequest;
import com.exe201.project.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(AuthenticationRequest request);

//    AuthenticationResponse refreshToken(RefreshTokenRequest request);
}
