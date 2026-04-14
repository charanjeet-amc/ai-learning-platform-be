package com.ailearning.platform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload a multipart file to Cloudinary.
     * Returns the secure URL of the uploaded asset.
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "ai-learning/" + folder,
                "resource_type", "auto"
        ));
        return (String) result.get("secure_url");
    }

    /**
     * Upload raw bytes (e.g. images extracted from documents) to Cloudinary.
     */
    public String uploadBytes(byte[] data, String folder, String fileName) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                "folder", "ai-learning/" + folder,
                "resource_type", "auto",
                "public_id", fileName
        ));
        return (String) result.get("secure_url");
    }

    /**
     * Delete a file from Cloudinary by its public ID.
     */
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
