package com.women.safety.features.emergencyMediaFiles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * File Storage Service - Supports both LOCAL and R2 (Cloudflare) storage
 *
 * Switch between modes using storage.mode property:
 * - LOCAL: Store files on server filesystem (development)
 * - R2: Store files on Cloudflare R2 (production)
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${storage.mode:LOCAL}")
    private String storageMode;

    @Value("${file.upload.dir:uploads/emergency-media}")
    private String uploadDir;

    @Value("${file.max.size:10485760}")
    private long maxFileSize;

    private static final String AUDIO_DIR = "audio";
    private static final String PHOTO_DIR = "photos";
    private static final String VIDEO_DIR = "videos";

    @Autowired(required = false)
    private CloudflareR2StorageService r2StorageService;

    /**
     * Initialize storage directories (LOCAL mode only)
     */
    public void init() {
        if ("LOCAL".equalsIgnoreCase(storageMode)) {
            try {
                Files.createDirectories(Paths.get(uploadDir, AUDIO_DIR));
                Files.createDirectories(Paths.get(uploadDir, PHOTO_DIR));
                Files.createDirectories(Paths.get(uploadDir, VIDEO_DIR));
                logger.info("LOCAL file storage initialized at: {}", uploadDir);
            } catch (IOException e) {
                logger.error("Failed to create storage directories: {}", e.getMessage());
                throw new RuntimeException("Could not create upload directories!", e);
            }
        } else if ("R2".equalsIgnoreCase(storageMode)) {
            logger.info("R2 cloud storage mode enabled (Cloudflare R2)");
        }
    }

    /**
     * Store audio file
     */
    public StorageResult storeAudio(MultipartFile file, Long alertId, Long userId) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.storeAudio(file, alertId, userId);
        }
        return storeFileLocally(file, AUDIO_DIR, alertId, userId, "audio");
    }

    /**
     * Store photo file
     */
    public StorageResult storePhoto(MultipartFile file, Long alertId, Long userId) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.storePhoto(file, alertId, userId);
        }
        return storeFileLocally(file, PHOTO_DIR, alertId, userId, "photo");
    }

    /**
     * Store video file
     */
    public StorageResult storeVideo(MultipartFile file, Long alertId, Long userId) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.storeVideo(file, alertId, userId);
        }
        return storeFileLocally(file, VIDEO_DIR, alertId, userId, "video");
    }

    /**
     * Load file as byte array
     */
    public byte[] loadFile(String path) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.loadFile(path);
        }
        return loadFileLocally(path);
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String path) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.deleteFile(path);
        }
        return deleteFileLocally(path);
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String path) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.fileExists(path);
        }
        Path filePath = Paths.get(uploadDir, path);
        return Files.exists(filePath);
    }

    // ==================== LOCAL STORAGE METHODS ====================

    private StorageResult storeFileLocally(MultipartFile file, String subDir, Long alertId, Long userId, String type) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException(String.format(
                        "File size exceeds maximum allowed size of %d MB",
                        maxFileSize / (1024 * 1024)
                ));
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            String newFileName = String.format(
                    "alert_%d_user_%d_%s_%s_%s%s",
                    alertId, userId, type, timestamp, uniqueId, fileExtension
            );

            Path targetLocation = Paths.get(uploadDir, subDir, newFileName);
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("{} file stored locally: {}", type, newFileName);

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
            logger.error("Failed to store {} file locally: {}", type, e.getMessage());
            StorageResult result = new StorageResult();
            result.setSuccess(false);
            result.setErrorMessage("Failed to store file: " + e.getMessage());
            return result;
        }
    }

    private byte[] loadFileLocally(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("Failed to load file locally: {}", e.getMessage());
            throw new RuntimeException("Could not read file: " + relativePath, e);
        }
    }

    private boolean deleteFileLocally(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
            logger.info("File deleted locally: {}", relativePath);
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete file locally: {}", e.getMessage());
            return false;
        }
    }

    // ==================== VALIDATION METHODS ====================

    public boolean isValidAudioFile(MultipartFile file) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.isValidAudioFile(file);
        }
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
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.isValidPhotoFile(file);
        }
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }

    public boolean isValidVideoFile(MultipartFile file) {
        if ("R2".equalsIgnoreCase(storageMode)) {
            return r2StorageService.isValidVideoFile(file);
        }
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("video/mp4") ||
                        contentType.equals("video/quicktime") ||
                        contentType.equals("video/mpeg")
        );
    }

    // ==================== HELPER METHODS ====================

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public String getUploadDir() {
        return uploadDir;
    }

    // ==================== STORAGE RESULT CLASS ====================

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