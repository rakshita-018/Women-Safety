package com.women.safety.features.emergencyMediaFiles.dto;

import lombok.Data;

@Data
public class EnhancedSOSRequestDTO {

    private String alertMessage;
    private Double latitude;
    private Double longitude;
    private String locationAddress;

    // Trigger information
    private String triggerType; // SHAKE, VOICE, BUTTON, AUTO
    private String triggerDetails; // Additional context

    // Device information
    private Integer batteryLevel;
    private Boolean isCharging;

    // Media capture settings
    private Boolean captureAudio;
    private Boolean capturePhoto;
    private Boolean captureVideo;
    private Integer audioDurationSeconds;
    private Integer photoCount;
    private Integer videoDurationSeconds;

    public EnhancedSOSRequestDTO() {
        // Defaults
        this.captureAudio = true;
        this.capturePhoto = true;
        this.captureVideo = false;
        this.audioDurationSeconds = 10;
        this.photoCount = 3;
        this.videoDurationSeconds = 5;
    }
}
