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

    public Boolean getShakeDetectionEnabled() {
        return shakeDetectionEnabled;
    }

    public void setShakeDetectionEnabled(Boolean shakeDetectionEnabled) {
        this.shakeDetectionEnabled = shakeDetectionEnabled;
    }

    public @Min(value = 0, message = "Sensitivity must be between 0 and 1") @Max(value = 1, message = "Sensitivity must be between 0 and 1") Double getShakeSensitivity() {
        return shakeSensitivity;
    }

    public void setShakeSensitivity(@Min(value = 0, message = "Sensitivity must be between 0 and 1") @Max(value = 1, message = "Sensitivity must be between 0 and 1") Double shakeSensitivity) {
        this.shakeSensitivity = shakeSensitivity;
    }

    public @Min(value = 1, message = "Duration must be at least 1 second") Integer getShakeDurationSeconds() {
        return shakeDurationSeconds;
    }

    public void setShakeDurationSeconds(@Min(value = 1, message = "Duration must be at least 1 second") Integer shakeDurationSeconds) {
        this.shakeDurationSeconds = shakeDurationSeconds;
    }

    public Boolean getRunningDetectionEnabled() {
        return runningDetectionEnabled;
    }

    public void setRunningDetectionEnabled(Boolean runningDetectionEnabled) {
        this.runningDetectionEnabled = runningDetectionEnabled;
    }

    public Integer getRunningDurationSeconds() {
        return runningDurationSeconds;
    }

    public void setRunningDurationSeconds(Integer runningDurationSeconds) {
        this.runningDurationSeconds = runningDurationSeconds;
    }

    public Boolean getFallDetectionEnabled() {
        return fallDetectionEnabled;
    }

    public void setFallDetectionEnabled(Boolean fallDetectionEnabled) {
        this.fallDetectionEnabled = fallDetectionEnabled;
    }

    public Boolean getImpactDetectionEnabled() {
        return impactDetectionEnabled;
    }

    public void setImpactDetectionEnabled(Boolean impactDetectionEnabled) {
        this.impactDetectionEnabled = impactDetectionEnabled;
    }

    public Boolean getVoiceActivationEnabled() {
        return voiceActivationEnabled;
    }

    public void setVoiceActivationEnabled(Boolean voiceActivationEnabled) {
        this.voiceActivationEnabled = voiceActivationEnabled;
    }

    public String getVoiceKeywords() {
        return voiceKeywords;
    }

    public void setVoiceKeywords(String voiceKeywords) {
        this.voiceKeywords = voiceKeywords;
    }

    public Boolean getScreamDetectionEnabled() {
        return screamDetectionEnabled;
    }

    public void setScreamDetectionEnabled(Boolean screamDetectionEnabled) {
        this.screamDetectionEnabled = screamDetectionEnabled;
    }

    public Boolean getAutoTriggerEnabled() {
        return autoTriggerEnabled;
    }

    public void setAutoTriggerEnabled(Boolean autoTriggerEnabled) {
        this.autoTriggerEnabled = autoTriggerEnabled;
    }

    public Integer getConfirmationDelaySeconds() {
        return confirmationDelaySeconds;
    }

    public void setConfirmationDelaySeconds(Integer confirmationDelaySeconds) {
        this.confirmationDelaySeconds = confirmationDelaySeconds;
    }

    public Boolean getAutoEnableAtNight() {
        return autoEnableAtNight;
    }

    public void setAutoEnableAtNight(Boolean autoEnableAtNight) {
        this.autoEnableAtNight = autoEnableAtNight;
    }

    public Boolean getSilentMode() {
        return silentMode;
    }

    public void setSilentMode(Boolean silentMode) {
        this.silentMode = silentMode;
    }

    public Integer getNightStartHour() {
        return nightStartHour;
    }

    public void setNightStartHour(Integer nightStartHour) {
        this.nightStartHour = nightStartHour;
    }

    public Integer getNightEndHour() {
        return nightEndHour;
    }

    public void setNightEndHour(Integer nightEndHour) {
        this.nightEndHour = nightEndHour;
    }

    public Boolean getAutoEnableInDangerZones() {
        return autoEnableInDangerZones;
    }

    public void setAutoEnableInDangerZones(Boolean autoEnableInDangerZones) {
        this.autoEnableInDangerZones = autoEnableInDangerZones;
    }

}
