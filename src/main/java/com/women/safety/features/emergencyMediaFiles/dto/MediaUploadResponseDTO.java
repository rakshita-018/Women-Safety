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
}

