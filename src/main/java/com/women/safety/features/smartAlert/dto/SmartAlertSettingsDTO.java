package com.women.safety.features.smartAlert.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SmartAlertSettingsDTO {

    // Motion Detection
    private Boolean shakeDetectionEnabled;

    @Min(value = 0, message = "Sensitivity must be between 0 and 1")
    @Max(value = 1, message = "Sensitivity must be between 0 and 1")
    private Double shakeSensitivity;

    @Min(value = 1, message = "Duration must be at least 1 second")
    private Integer shakeDurationSeconds;

    private Boolean runningDetectionEnabled;
    private Integer runningDurationSeconds;
    private Boolean fallDetectionEnabled;
    private Boolean impactDetectionEnabled;

    // Voice Detection
    private Boolean voiceActivationEnabled;
    private String voiceKeywords; // Comma-separated
    private Boolean screamDetectionEnabled;

    // Alert Behavior
    private Boolean autoTriggerEnabled;
    private Integer confirmationDelaySeconds;
    private Boolean silentMode;

    // Scheduling
    private Boolean autoEnableAtNight;
    private Integer nightStartHour;
    private Integer nightEndHour;
    private Boolean autoEnableInDangerZones;

    public SmartAlertSettingsDTO() {}
}
