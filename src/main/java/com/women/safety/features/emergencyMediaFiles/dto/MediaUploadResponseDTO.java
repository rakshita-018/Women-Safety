package com.women.safety.features.emergencyMediaFiles.dto;

import com.women.safety.features.emergencyMediaFiles.model.EmergencyMedia;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MediaUploadResponseDTO {

    private Long mediaId;
    private Long alertId;
    private EmergencyMedia.MediaType mediaType;
    private String fileName;
    private String fileSize;
    private Integer durationSeconds;
    private EmergencyMedia.UploadStatus uploadStatus;
    private String message;
    private String viewUrl;
    private LocalDateTime uploadedAt;

    public MediaUploadResponseDTO() {}

    public MediaUploadResponseDTO(EmergencyMedia media, String message, String baseUrl) {
        this.mediaId = media.getId();
        this.alertId = media.getAlert().getId();
        this.mediaType = media.getMediaType();
        this.fileName = media.getFileName();
        this.fileSize = media.getFormattedFileSize();
        this.durationSeconds = media.getDurationSeconds();
        this.uploadStatus = media.getUploadStatus();
        this.message = message;
        this.viewUrl = media.getFileUrl(baseUrl);
        this.uploadedAt = media.getUploadedAt();
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public EmergencyMedia.MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(EmergencyMedia.MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public EmergencyMedia.UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(EmergencyMedia.UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

