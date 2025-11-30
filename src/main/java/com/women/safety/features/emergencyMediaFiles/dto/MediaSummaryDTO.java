package com.women.safety.features.emergencyMediaFiles.dto;

import lombok.Data;

import java.util.List;

@Data
public class MediaSummaryDTO {

    private Long alertId;
    private Integer totalMediaCount;
    private Integer audioCount;
    private Integer photoCount;
    private Integer videoCount;
    private Integer completedCount;
    private Integer pendingCount;
    private String message;
    private List<MediaItemDTO> media;

    @Data
    public static class MediaItemDTO {
        private Long id;
        private String type;
        private String fileName;
        private String fileSize;
        private Integer durationSeconds;
        private String status;
        private String viewUrl;
        private String thumbnailUrl;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Integer getTotalMediaCount() {
        return totalMediaCount;
    }

    public void setTotalMediaCount(Integer totalMediaCount) {
        this.totalMediaCount = totalMediaCount;
    }

    public Integer getAudioCount() {
        return audioCount;
    }

    public void setAudioCount(Integer audioCount) {
        this.audioCount = audioCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MediaItemDTO> getMedia() {
        return media;
    }

    public void setMedia(List<MediaItemDTO> media) {
        this.media = media;
    }
}
