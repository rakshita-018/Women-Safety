package com.women.safety.features.smartAlert.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspicious_activity_log", indexes = {
        @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp"),
        @Index(name = "idx_activity_type", columnList = "activity_type")
})
@Data
public class SuspiciousActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "intensity_level")
    private Double intensityLevel; // 0.0 to 1.0

    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 to 1.0

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "device_motion_data", columnDefinition = "TEXT")
    private String deviceMotionData; // JSON data from accelerometer

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "alert_triggered")
    private Boolean alertTriggered = false;

    @Column(name = "false_positive")
    private Boolean falsePositive = false; // User can mark as false alarm

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ActivityType {
        PHONE_SHAKE,        // Vigorous shaking detected
        RAPID_MOVEMENT,     // Running detected
        FALL_DETECTED,      // Possible fall
        ERRATIC_MOVEMENT,   // Unusual movement patterns
        SUDDEN_STOP,        // Sudden stop after movement
        VOICE_DISTRESS,     // "Help" keyword detected
        SCREAM_DETECTED,    // High-pitched sound detected
        IMPACT_DETECTED,    // Strong impact/hit detected
        UNUSUAL_LOCATION,   // In dangerous area at unusual time
        DEVICE_THROWN       // Device thrown/dropped
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public SuspiciousActivityLog() {}

    public SuspiciousActivityLog(AuthUser user, ActivityType activityType, Double intensityLevel) {
        this.user = user;
        this.activityType = activityType;
        this.intensityLevel = intensityLevel;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Double getIntensityLevel() {
        return intensityLevel;
    }

    public void setIntensityLevel(Double intensityLevel) {
        this.intensityLevel = intensityLevel;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getAlertTriggered() {
        return alertTriggered;
    }

    public void setAlertTriggered(Boolean alertTriggered) {
        this.alertTriggered = alertTriggered;
    }

    public Boolean getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(Boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
