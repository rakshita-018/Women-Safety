package com.women.safety.features.fakeCalls.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Fake Call Log
 *
 * Tracks history of fake calls for analytics and safety records
 */
@Entity
@Table(name = "fake_call_logs")
@Data
public class FakeCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fake_call_id")
    private FakeCall fakeCall; // Reference to preset used (if any)

    @Column(name = "caller_name")
    private String callerName;

    @Column(name = "trigger_method")
    @Enumerated(EnumType.STRING)
    private TriggerMethod triggerMethod;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "call_started_at", nullable = false)
    private LocalDateTime callStartedAt;

    @Column(name = "call_ended_at")
    private LocalDateTime callEndedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "was_answered")
    private Boolean wasAnswered = false;

    @Column(name = "was_declined")
    private Boolean wasDeclined = false;

    @Column(name = "auto_ended")
    private Boolean autoEnded = false; // True if timer ended call automatically

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "notes")
    private String notes; // User can add notes about why they used fake call

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TriggerMethod {
        FLOATING_BUTTON,    // Invisible floating bubble
        IN_APP_BUTTON,      // Button inside app
        WIDGET,             // Home screen widget
        SHAKE_GESTURE,      // Shake phone to trigger
        VOICE_COMMAND,      // "Hey Siri, call mom" simulation
        SCHEDULED           // Pre-scheduled fake call
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (callStartedAt == null) {
            callStartedAt = LocalDateTime.now();
        }
    }

    public FakeCallLog() {}

    public FakeCallLog(AuthUser user, FakeCall fakeCall, TriggerMethod triggerMethod) {
        this.user = user;
        this.fakeCall = fakeCall;
        this.callerName = fakeCall != null ? fakeCall.getCallerName() : "Unknown";
        this.triggerMethod = triggerMethod;
        this.callStartedAt = LocalDateTime.now();
    }

    // Calculate duration when call ends
    public void endCall() {
        this.callEndedAt = LocalDateTime.now();
        if (this.callStartedAt != null && this.callEndedAt != null) {
            this.durationSeconds = (int) java.time.Duration.between(
                    this.callStartedAt, this.callEndedAt).getSeconds();
        }
    }
}