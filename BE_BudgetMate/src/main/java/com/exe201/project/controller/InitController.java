package com.exe201.project.controller;

import com.exe201.project.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/init")
@RequiredArgsConstructor
public class InitController {

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getInitStatus() {
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .message("Initialization is handled automatically on application startup.")
                .data("Check application logs for initialization status.")
                .build()
        );
    }
}
