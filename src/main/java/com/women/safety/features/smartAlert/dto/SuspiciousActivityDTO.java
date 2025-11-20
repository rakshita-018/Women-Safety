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
}
