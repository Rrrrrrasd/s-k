package com.contract.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3StorageService {
    String upload(MultipartFile file) throws IOException;
    String getBucketName();
}
