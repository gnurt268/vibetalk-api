package com.gnxrt.vibetalkapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "vibetalk/avatars",
                        "width", 300,
                        "height", 300,
                        "crop", "fill",
                        "gravity", "face"
                ));
        return (String) uploadResult.get("secure_url");
    }

    public String uploadImageFromBase64(String base64Image) throws IOException {
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                "data:image/png;base64," + base64Image,
                ObjectUtils.asMap(
                        "folder", "vibetalk/avatars",
                        "width", 300,
                        "height", 300,
                        "crop", "fill",
                        "gravity", "face"
                ));
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Upload ảnh chat (không crop, giữ nguyên kích thước gốc, giới hạn max 1920px)
     */
    public Map<String, String> uploadChatImage(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "vibetalk/chat-images",
                        "transformation", new com.cloudinary.Transformation()
                                .width(1920).height(1920).crop("limit"),
                        "resource_type", "image"
                ));
        Map<String, String> result = new java.util.HashMap<>();
        result.put("url", (String) uploadResult.get("secure_url"));
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", String.valueOf(file.getSize()));
        result.put("fileType", file.getContentType());
        return result;
    }

    /**
     * Upload file chat (PDF, DOC, ZIP, etc.) dùng resource_type=raw
     */
    public Map<String, String> uploadChatFile(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "vibetalk/chat-files",
                        "resource_type", "raw"
                ));
        Map<String, String> result = new java.util.HashMap<>();
        result.put("url", (String) uploadResult.get("secure_url"));
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", String.valueOf(file.getSize()));
        result.put("fileType", file.getContentType());
        return result;
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        String[] parts = imageUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("upload")) {
                StringBuilder publicId = new StringBuilder();
                for (int j = i + 2; j < parts.length; j++) {
                    if (j > i + 2) publicId.append("/");
                    String part = parts[j];
                    if (j == parts.length - 1) {
                        int dotIndex = part.lastIndexOf('.');
                        if (dotIndex > 0) {
                            part = part.substring(0, dotIndex);
                        }
                    }
                    publicId.append(part);
                }
                return publicId.toString();
            }
        }
        return null;
    }
}