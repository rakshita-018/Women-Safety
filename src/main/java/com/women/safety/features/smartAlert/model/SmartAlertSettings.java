package com.women.safety.features.smartAlert.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "smart_alert_settings")
@Data
public class SmartAlertSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AuthUser user;

    // Motion Detection Settings
    @Column(name = "shake_detection_enabled")
    private Boolean shakeDetectionEnabled = true;

    @Column(name = "shake_sensitivity")
    private Double shakeSensitivity = 0.7; // 0.0 to 1.0

    @Column(name = "shake_duration_seconds")
    private Integer shakeDurationSeconds = 3; // Shake for 3 seconds to trigger

    @Column(name = "running_detection_enabled")
    private Boolean runningDetectionEnabled = true;

    @Column(name = "running_duration_seconds")
    private Integer runningDurationSeconds = 10; // Run for 10 seconds to trigger

    @Column(name = "fall_detection_enabled")
    private Boolean fallDetectionEnabled = true;

    @Column(name = "impact_detection_enabled")
    private Boolean impactDetectionEnabled = true;

    // Voice Detection Settings
    @Column(name = "voice_activation_enabled")
    private Boolean voiceActivationEnabled = true;

    @Column(name = "voice_keywords", columnDefinition = "TEXT")
    private String voiceKeywords = "help,emergency,police,danger,assault,attack"; // Comma-separated

    @Column(name = "scream_detection_enabled")
    private Boolean screamDetectionEnabled = false; // Battery intensive

    // Alert Behavior
    @Column(name = "auto_trigger_enabled")
    private Boolean autoTriggerEnabled = false; // Auto-send alert or wait for confirmation

    @Column(name = "confirmation_delay_seconds")
    private Integer confirmationDelaySeconds = 10; // Show "Cancel" button for 10 seconds

    @Column(name = "silent_mode")
    private Boolean silentMode = false; // No sound/vibration for stealth

    // Scheduling
    @Column(name = "auto_enable_at_night")
    private Boolean autoEnableAtNight = false;

    @Column(name = "night_start_hour")
    private Integer nightStartHour = 22; // 10 PM

    @Column(name = "night_end_hour")
    private Integer nightEndHour = 6; // 6 AM

    @Column(name = "auto_enable_in_danger_zones")
    private Boolean autoEnableInDangerZones = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public SmartAlertSettings() {}

    public SmartAlertSettings(AuthUser user) {
        this.user = user;
    }
}
