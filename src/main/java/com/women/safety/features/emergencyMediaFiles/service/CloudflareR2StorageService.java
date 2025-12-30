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
 * Cloudflare R2 Storage Service - FIXED VERSION
 * Now stores public URLs in database and generates correct media links
 */
@Service
public class CloudflareR2StorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareR2StorageService.class);

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl; // e.g., https://pub-0bf92da275904aee9de4bf37768c544b.r2.dev

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
     * Generic file storage method for R2 - FIXED to return public URL
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

            logger.info("✅ File uploaded to R2: {} ({})", s3Key, formatFileSize(file.getSize()));

            // 🔥 FIX: Build PUBLIC URL for database storage
            String publicFileUrl = buildPublicUrl(s3Key);

            // Build result with PUBLIC URL
            result.setSuccess(true);
            result.setFileName(fileName);
            result.setFilePath(publicFileUrl); // Store PUBLIC URL, not S3 key
            result.setRelativePath(s3Key); // Keep S3 key for internal use
            result.setFileSize(file.getSize());
            result.setFileExtension(fileExtension);
            result.setMimeType(contentType);
            result.setOriginalFileName(originalFilename);

            logger.info("📍 Public URL generated: {}", publicFileUrl);

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
     * 🔥 NEW: Build public URL from S3 key
     */
    private String buildPublicUrl(String s3Key) {
        if (publicUrl == null || publicUrl.isEmpty()) {
            logger.warn("⚠️ Public URL not configured! Using S3 key as fallback.");
            return s3Key;
        }

        // Ensure no double slashes
        String baseUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
        return baseUrl + "/" + s3Key;
    }

    /**
     * Load file from R2 as byte array
     * 🔥 FIXED: Extract S3 key from public URL if needed
     */
    public byte[] loadFile(String filePathOrUrl) {
        try {
            // Extract S3 key from public URL if it's a full URL
            String s3Key = extractS3KeyFromUrl(filePathOrUrl);

            logger.info("📥 Loading file from R2: {}", s3Key);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] fileBytes = s3Client.getObject(getRequest).readAllBytes();

            logger.info("✅ File retrieved from R2: {} ({})", s3Key, formatFileSize(fileBytes.length));

            return fileBytes;

        } catch (IOException e) {
            logger.error("Failed to read file from R2: {}", e.getMessage());
            throw new RuntimeException("Could not read file: " + filePathOrUrl, e);
        } catch (S3Exception e) {
            logger.error("R2 error retrieving file: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Cloud storage error: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * 🔥 NEW: Extract S3 key from public URL or return as-is if already a key
     */
    private String extractS3KeyFromUrl(String filePathOrUrl) {
        if (filePathOrUrl == null || filePathOrUrl.isEmpty()) {
            throw new IllegalArgumentException("File path/URL cannot be empty");
        }

        // If it's a full URL starting with http/https, extract the path
        if (filePathOrUrl.startsWith("http://") || filePathOrUrl.startsWith("https://")) {
            try {
                // Extract everything after the domain
                int domainEnd = filePathOrUrl.indexOf("/", 8); // Skip "https://"
                if (domainEnd != -1) {
                    return filePathOrUrl.substring(domainEnd + 1);
                }
            } catch (Exception e) {
                logger.warn("Failed to extract S3 key from URL, using as-is: {}", filePathOrUrl);
            }
        }

        // Already an S3 key (e.g., "audio/file.mp3")
        return filePathOrUrl;
    }

    /**
     * Delete file from R2
     * 🔥 FIXED: Handle both URLs and S3 keys
     */
    public boolean deleteFile(String filePathOrUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(filePathOrUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            logger.info("🗑️ File deleted from R2: {}", s3Key);
            return true;

        } catch (S3Exception e) {
            logger.error("Failed to delete file from R2: {}", e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * Check if file exists in R2
     * 🔥 FIXED: Handle both URLs and S3 keys
     */
    public boolean fileExists(String filePathOrUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(filePathOrUrl);

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
     * 🔥 UPDATED: Now always returns public URL
     */
    public String getFileUrl(String filePathOrUrl) {
        // If already a full URL, return as-is
        if (filePathOrUrl != null && (filePathOrUrl.startsWith("http://") || filePathOrUrl.startsWith("https://"))) {
            return filePathOrUrl;
        }

        // Build public URL from S3 key
        return buildPublicUrl(filePathOrUrl);
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

    // Validation methods
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