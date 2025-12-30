package com.women.safety.features.emergencyMediaFiles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Cloudflare R2 Storage Service
 *
 * Handles file upload/download/delete operations with Cloudflare R2
 * Uses AWS S3 SDK (R2 is S3-compatible)
 */
@Service
public class CloudflareR2StorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareR2StorageService.class);

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl;

    @Value("${file.max.size:10485760}")
    private long maxFileSize;

    private static final String AUDIO_DIR = "audio";
    private static final String PHOTO_DIR = "photos";
    private static final String VIDEO_DIR = "videos";

    public CloudflareR2StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Store audio file in R2
     */
    public FileStorageService.StorageResult storeAudio(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, AUDIO_DIR, alertId, userId, "audio");
    }

    /**
     * Store photo file in R2
     */
    public FileStorageService.StorageResult storePhoto(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, PHOTO_DIR, alertId, userId, "photo");
    }

    /**
     * Store video file in R2
     */
    public FileStorageService.StorageResult storeVideo(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, VIDEO_DIR, alertId, userId, "video");
    }

    /**
     * Generic file storage method for R2
     */
    private FileStorageService.StorageResult storeFile(
            MultipartFile file, String subDir, Long alertId, Long userId, String type) {

        FileStorageService.StorageResult result = new FileStorageService.StorageResult();

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException(String.format(
                        "File size exceeds maximum allowed size of %d MB",
                        maxFileSize / (1024 * 1024)
                ));
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            String fileName = String.format(
                    "alert_%d_user_%d_%s_%s_%s%s",
                    alertId, userId, type, timestamp, uniqueId, fileExtension
            );

            // Build S3 key (path in bucket)
            String s3Key = subDir + "/" + fileName;

            // Prepare metadata
            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            // Upload to R2
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(contentType)
                        .contentLength(file.getSize())
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            }

            logger.info("File uploaded to R2: {} ({})", s3Key, formatFileSize(file.getSize()));

            // Build result
            result.setSuccess(true);
            result.setFileName(fileName);
            result.setFilePath(s3Key); // Store S3 key as path
            result.setRelativePath(s3Key);
            result.setFileSize(file.getSize());
            result.setFileExtension(fileExtension);
            result.setMimeType(contentType);
            result.setOriginalFileName(originalFilename);

            return result;

        } catch (IOException e) {
            logger.error("Failed to upload {} to R2: {}", type, e.getMessage());
            result.setSuccess(false);
            result.setErrorMessage("Failed to upload file: " + e.getMessage());
            return result;
        } catch (S3Exception e) {
            logger.error("R2 error uploading {}: {}", type, e.awsErrorDetails().errorMessage());
            result.setSuccess(false);
            result.setErrorMessage("Cloud storage error: " + e.awsErrorDetails().errorMessage());
            return result;
        }
    }

    /**
     * Load file from R2 as byte array
     */
    public byte[] loadFile(String s3Key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] fileBytes = s3Client.getObject(getRequest).readAllBytes();

            logger.info("File retrieved from R2: {} ({})", s3Key, formatFileSize(fileBytes.length));

            return fileBytes;

        } catch (IOException e) {
            logger.error("Failed to read file from R2: {}", e.getMessage());
            throw new RuntimeException("Could not read file: " + s3Key, e);
        } catch (S3Exception e) {
            logger.error("R2 error retrieving file: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Cloud storage error: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Delete file from R2
     */
    public boolean deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            logger.info("File deleted from R2: {}", s3Key);
            return true;

        } catch (S3Exception e) {
            logger.error("Failed to delete file from R2: {}", e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * Check if file exists in R2
     */
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking file existence: {}", e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * Get public URL for file
     * If publicUrl is configured, use it; otherwise generate presigned URL
     */
    public String getFileUrl(String s3Key) {
        if (publicUrl != null && !publicUrl.isEmpty()) {
            // Use public R2 domain (if configured)
            return publicUrl + "/" + s3Key;
        } else {
            // For now, return API endpoint URL
            // In production, you should configure R2 public domain or use presigned URLs
            return "/api/emergency/media/view/" + s3Key;
        }
    }

    // Helper methods

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Validate file types (same as local storage)
     */
    public boolean isValidAudioFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("audio/aac") ||
                        contentType.equals("audio/m4a") ||
                        contentType.equals("audio/mp3") ||
                        contentType.equals("audio/mpeg") ||
                        contentType.equals("audio/mp4")
        );
    }

    public boolean isValidPhotoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }

    public boolean isValidVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("video/mp4") ||
                        contentType.equals("video/quicktime") ||
                        contentType.equals("video/mpeg")
        );
    }
}
