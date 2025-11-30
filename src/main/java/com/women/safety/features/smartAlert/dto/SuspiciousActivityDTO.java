package com.women.safety.features.smartAlert.dto;

import com.women.safety.features.smartAlert.model.SuspiciousActivityLog;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuspiciousActivityDTO {

    @NotNull(message = "Activity type is required")
    private SuspiciousActivityLog.ActivityType activityType;

    @Min(value = 0, message = "Intensity level must be between 0 and 1")
    @Max(value = 1, message = "Intensity level must be between 0 and 1")
    private Double intensityLevel;

    @Min(value = 0, message = "Confidence score must be between 0 and 1")
    @Max(value = 1, message = "Confidence score must be between 0 and 1")
    private Double confidenceScore;

    private Double latitude;
    private Double longitude;
    private String deviceMotionData; // JSON string
    private String notes;

    public SuspiciousActivityDTO() {}

    public SuspiciousActivityDTO(SuspiciousActivityLog.ActivityType activityType, Double intensityLevel) {
        this.activityType = activityType;
        this.intensityLevel = intensityLevel;
    }

    public @NotNull(message = "Activity type is required") SuspiciousActivityLog.ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(@NotNull(message = "Activity type is required") SuspiciousActivityLog.ActivityType activityType) {
        this.activityType = activityType;
    }

    public @Min(value = 0, message = "Intensity level must be between 0 and 1") @Max(value = 1, message = "Intensity level must be between 0 and 1") Double getIntensityLevel() {
        return intensityLevel;
    }

    public void setIntensityLevel(@Min(value = 0, message = "Intensity level must be between 0 and 1") @Max(value = 1, message = "Intensity level must be between 0 and 1") Double intensityLevel) {
        this.intensityLevel = intensityLevel;
    }

    public @Min(value = 0, message = "Confidence score must be between 0 and 1") @Max(value = 1, message = "Confidence score must be between 0 and 1") Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(@Min(value = 0, message = "Confidence score must be between 0 and 1") @Max(value = 1, message = "Confidence score must be between 0 and 1") Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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

    public String getDeviceMotionData() {
        return deviceMotionData;
    }

    public void setDeviceMotionData(String deviceMotionData) {
        this.deviceMotionData = deviceMotionData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
