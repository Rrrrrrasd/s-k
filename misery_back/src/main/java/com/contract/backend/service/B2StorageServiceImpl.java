package com.contract.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class B2StorageServiceImpl implements S3StorageService {

    private final S3Client s3Client;

    @Value("${b2.bucket-name}")
    private String bucketName;

    public B2StorageServiceImpl(
            @Value("${b2.endpoint}") String endpoint,
            @Value("${b2.access-key}") String accessKey,
            @Value("${b2.secret-key}") String secretKey
    ) {
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.US_EAST_1) // B2는 region 설정 무시됨
                .build();
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        String key = generateFileKey(file.getOriginalFilename());

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        return key;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    private String generateFileKey(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "contracts/" + timestamp + "_" + UUID.randomUUID() + "_" + originalFileName;
    }
}
