package com.women.safety.features.emergencyMediaFiles.model;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_media")
@Data
public class EmergencyMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private EmergencyAlert alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize; // in bytes

    @Column(name = "file_extension")
    private String fileExtension; // .jpg, .aac, .mp4

    @Column(name = "mime_type")
    private String mimeType; // image/jpeg, audio/aac, video/mp4

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // for audio/video only

    @Column(name = "thumbnail_path")
    private String thumbnailPath; // for video only

    @Column(name = "upload_status")
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus = UploadStatus.UPLOADING;

    @Column(name = "compression_applied")
    private Boolean compressionApplied = false;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum MediaType {
        AUDIO,      // .aac, .m4a, .mp3
        PHOTO,      // .jpg, .png
        VIDEO,      // .mp4, .mov
        SCREENSHOT  // .jpg, .png
    }

    public enum UploadStatus {
        UPLOADING,
        COMPLETED,
        FAILED,
        PROCESSING
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    public EmergencyMedia() {}

    public EmergencyMedia(EmergencyAlert alert, AuthUser user, MediaType mediaType,
                          String fileName, String filePath) {
        this.alert = alert;
        this.user = user;
        this.mediaType = mediaType;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    // Helper method to get file URL for sharing
    public String getFileUrl(String baseUrl) {
        return baseUrl + "/api/emergency/media/view/" + this.id;
    }

    // Helper method to check if upload is complete
    public boolean isUploadComplete() {
        return this.uploadStatus == UploadStatus.COMPLETED;
    }

    // Helper method to format file size
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmergencyAlert getAlert() {
        return alert;
    }

    public void setAlert(EmergencyAlert alert) {
        this.alert = alert;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Boolean getCompressionApplied() {
        return compressionApplied;
    }

    public void setCompressionApplied(Boolean compressionApplied) {
        this.compressionApplied = compressionApplied;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
