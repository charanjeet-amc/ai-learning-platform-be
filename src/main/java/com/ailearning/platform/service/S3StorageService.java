package com.ailearning.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3StorageService(S3Client s3Client,
                            @Value("${aws.s3.bucket-name}") String bucketName,
                            @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
    }

    /**
     * Upload a multipart file to S3.
     * Returns the public URL of the uploaded object.
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String key = "ai-learning/" + folder + "/" + UUID.randomUUID() + ext;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        log.info("Uploaded file to S3: {}", key);
        return buildUrl(key);
    }

    /**
     * Upload raw bytes (e.g. images extracted from documents) to S3.
     */
    public String uploadBytes(byte[] data, String folder, String fileName) throws IOException {
        String key = "ai-learning/" + folder + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        log.info("Uploaded bytes to S3: {}", key);
        return buildUrl(key);
    }

    /**
     * Delete a file from S3 by its object key.
     */
    public void deleteFile(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
        log.info("Deleted file from S3: {}", key);
    }

    private String buildUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
