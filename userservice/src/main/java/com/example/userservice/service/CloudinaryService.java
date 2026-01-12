package com.example.userservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, String> uploadFile(MultipartFile file, String folder)throws IOException;
    void deleteFile(String publicId);
}
