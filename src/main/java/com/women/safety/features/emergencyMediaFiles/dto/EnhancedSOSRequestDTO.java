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

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerDetails() {
        return triggerDetails;
    }

    public void setTriggerDetails(String triggerDetails) {
        this.triggerDetails = triggerDetails;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Boolean getCharging() {
        return isCharging;
    }

    public void setCharging(Boolean charging) {
        isCharging = charging;
    }

    public Boolean getCaptureAudio() {
        return captureAudio;
    }

    public void setCaptureAudio(Boolean captureAudio) {
        this.captureAudio = captureAudio;
    }

    public Boolean getCapturePhoto() {
        return capturePhoto;
    }

    public void setCapturePhoto(Boolean capturePhoto) {
        this.capturePhoto = capturePhoto;
    }

    public Boolean getCaptureVideo() {
        return captureVideo;
    }

    public void setCaptureVideo(Boolean captureVideo) {
        this.captureVideo = captureVideo;
    }

    public Integer getAudioDurationSeconds() {
        return audioDurationSeconds;
    }

    public void setAudioDurationSeconds(Integer audioDurationSeconds) {
        this.audioDurationSeconds = audioDurationSeconds;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }
}
