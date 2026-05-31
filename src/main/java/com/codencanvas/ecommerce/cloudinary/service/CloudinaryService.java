package com.codencanvas.ecommerce.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codencanvas.ecommerce.common.exception.AppException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Allowed image types
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = { "image/jpeg", "image/png", "image/webp" };

    public UploadResult upload(MultipartFile file, String folder) {
        validateFile(file);

        try {
            String publicId = folder + "/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "resource_type", "image",
                            "fetch_format", "auto",
                            "quality", "auto"));

            String url = (String) result.get("secure_url");
            String resultId = (String) result.get("public_id");

            log.info("Image uploaded: {}", resultId);
            return new UploadResult(url, resultId);

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new AppException("error.upload.failed", 500);
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank())
            return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("error.image.empty", 400);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException("error.image.size", 400);
        }

        String contentType = file.getContentType();
        if (!Arrays.asList(ALLOWED_TYPES).contains(contentType)) {
            throw new AppException("error.image.type", 400);
        }

    }

    // ── Result Record ────────────────────────────────────────────
    public record UploadResult(String url, String publicId) {
    }

}