package com.exe201.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map delete(String publicId) throws IOException;
    Map upload(MultipartFile file) throws IOException;
}
