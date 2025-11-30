package com.women.safety.features.fakeCalls.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Fake Call Entity
 *
 * Stores fake call configurations for realistic simulation
 */
@Entity
@Table(name = "fake_calls")
@Data
public class FakeCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "caller_name", nullable = false)
    private String callerName; // e.g., "Mom", "John", "Boss"

    @Column(name = "caller_phone", nullable = false)
    private String callerPhone; // Displayed phone number

    @Column(name = "caller_photo_url")
    private String callerPhotoUrl; // Profile picture URL (optional)

    @Column(name = "ringtone_name")
    private String ringtoneName; // Which ringtone to play

    @Column(name = "vibrate_enabled")
    private Boolean vibrateEnabled = true;

    @Column(name = "auto_answer_delay_seconds")
    private Integer autoAnswerDelaySeconds; // Auto answer after X seconds (null = manual)

    @Column(name = "call_duration_seconds")
    private Integer callDurationSeconds = 120; // How long fake call should last (default 2 min)

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type")
    private CallType callType = CallType.VOICE_CALL; // VOICE_CALL, VIDEO_CALL

    @Column(name = "is_preset")
    private Boolean isPreset = false; // True if this is a saved preset

    @Column(name = "preset_name")
    private String presetName; // e.g., "Emergency Mom Call", "Boss Meeting"

    @Column(name = "trigger_count")
    private Integer triggerCount = 0; // How many times this fake call was used

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CallType {
        VOICE_CALL,
        VIDEO_CALL
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (triggerCount == null) {
            triggerCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public FakeCall() {}

    public FakeCall(AuthUser user, String callerName, String callerPhone) {
        this.user = user;
        this.callerName = callerName;
        this.callerPhone = callerPhone;
    }

    // Increment trigger count when used
    public void recordTrigger() {
        this.triggerCount++;
        this.lastTriggeredAt = LocalDateTime.now();
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

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerPhone() {
        return callerPhone;
    }

    public void setCallerPhone(String callerPhone) {
        this.callerPhone = callerPhone;
    }

    public String getCallerPhotoUrl() {
        return callerPhotoUrl;
    }

    public void setCallerPhotoUrl(String callerPhotoUrl) {
        this.callerPhotoUrl = callerPhotoUrl;
    }

    public String getRingtoneName() {
        return ringtoneName;
    }

    public void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
    }

    public Boolean getVibrateEnabled() {
        return vibrateEnabled;
    }

    public void setVibrateEnabled(Boolean vibrateEnabled) {
        this.vibrateEnabled = vibrateEnabled;
    }

    public Integer getAutoAnswerDelaySeconds() {
        return autoAnswerDelaySeconds;
    }

    public void setAutoAnswerDelaySeconds(Integer autoAnswerDelaySeconds) {
        this.autoAnswerDelaySeconds = autoAnswerDelaySeconds;
    }

    public Integer getCallDurationSeconds() {
        return callDurationSeconds;
    }

    public void setCallDurationSeconds(Integer callDurationSeconds) {
        this.callDurationSeconds = callDurationSeconds;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public Boolean getPreset() {
        return isPreset;
    }

    public void setPreset(Boolean preset) {
        isPreset = preset;
    }

    public Integer getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(Integer triggerCount) {
        this.triggerCount = triggerCount;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}