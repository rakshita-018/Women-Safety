package com.women.safety.features.emergencyMediaFiles.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * File Storage Service - Local Filesystem
 * Stores emergency media files (audio, photos, videos)
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload.dir:uploads/emergency-media}")
    private String uploadDir;

    @Value("${file.max.size:10485760}") // 10MB default
    private long maxFileSize;

    private static final String AUDIO_DIR = "audio";
    private static final String PHOTO_DIR = "photos";
    private static final String VIDEO_DIR = "videos";
    private static final String THUMBNAIL_DIR = "thumbnails";

    /**
     * Initialize storage directories
     */
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, AUDIO_DIR));
            Files.createDirectories(Paths.get(uploadDir, PHOTO_DIR));
            Files.createDirectories(Paths.get(uploadDir, VIDEO_DIR));
            Files.createDirectories(Paths.get(uploadDir, THUMBNAIL_DIR));
            logger.info("File storage directories initialized at: {}", uploadDir);
        } catch (IOException e) {
            logger.error("Failed to create storage directories: {}", e.getMessage());
            throw new RuntimeException("Could not create upload directories!", e);
        }
    }

    /**
     * Store audio file
     */
    public StorageResult storeAudio(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, AUDIO_DIR, alertId, userId, "audio");
    }

    /**
     * Store photo file
     */
    public StorageResult storePhoto(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, PHOTO_DIR, alertId, userId, "photo");
    }

    /**
     * Store video file
     */
    public StorageResult storeVideo(MultipartFile file, Long alertId, Long userId) {
        return storeFile(file, VIDEO_DIR, alertId, userId, "video");
    }

    /**
     * Generic file storage method
     */
    private StorageResult storeFile(MultipartFile file, String subDir, Long alertId, Long userId, String type) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            // Check file size
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

            String newFileName = String.format(
                    "alert_%d_user_%d_%s_%s_%s%s",
                    alertId, userId, type, timestamp, uniqueId, fileExtension
            );

            // Create full path
            Path targetLocation = Paths.get(uploadDir, subDir, newFileName);

            // Ensure directory exists
            Files.createDirectories(targetLocation.getParent());

            // Copy file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("{} file stored successfully: {}", type, newFileName);

            // Return result
            StorageResult result = new StorageResult();
            result.setSuccess(true);
            result.setFileName(newFileName);
            result.setFilePath(targetLocation.toString());
            result.setRelativePath(subDir + "/" + newFileName);
            result.setFileSize(file.getSize());
            result.setFileExtension(fileExtension);
            result.setMimeType(file.getContentType());
            result.setOriginalFileName(originalFilename);

            return result;

        } catch (IOException e) {
            logger.error("Failed to store {} file: {}", type, e.getMessage());

            StorageResult result = new StorageResult();
            result.setSuccess(false);
            result.setErrorMessage("Failed to store file: " + e.getMessage());
            return result;
        }
    }

    /**
     * Load file as byte array
     */
    public byte[] loadFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("Failed to load file: {}", e.getMessage());
            throw new RuntimeException("Could not read file: " + relativePath, e);
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String relativePath) {
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.exists(filePath);
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
            logger.info("File deleted: {}", relativePath);
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Get upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Validate audio file
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

    /**
     * Validate photo file
     */
    public boolean isValidPhotoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }

    /**
     * Validate video file
     */
    public boolean isValidVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("video/mp4") ||
                        contentType.equals("video/quicktime") ||
                        contentType.equals("video/mpeg")
        );
    }

    /**
     * Storage result class
     */
    public static class StorageResult {
        private boolean success;
        private String fileName;
        private String filePath;
        private String relativePath;
        private Long fileSize;
        private String fileExtension;
        private String mimeType;
        private String originalFileName;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getRelativePath() { return relativePath; }
        public void setRelativePath(String relativePath) { this.relativePath = relativePath; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getFileExtension() { return fileExtension; }
        public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
