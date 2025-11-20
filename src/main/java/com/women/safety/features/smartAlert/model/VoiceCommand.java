package com.women.safety.features.smartAlert.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_command")
@Data
public class VoiceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "command_text", columnDefinition = "TEXT")
    private String commandText; // Transcribed text

    @Column(name = "audio_url")
    private String audioUrl; // S3 URL if audio is saved

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type")
    private CommandType commandType;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "alert_triggered")
    private Boolean alertTriggered = false;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum CommandType {
        EMERGENCY_HELP,     // "Help", "Emergency", "Police"
        CANCEL_ALERT,       // "Cancel", "False alarm", "I'm safe"
        CONFIRM_ALERT,      // "Yes", "Confirm", "Send alert"
        SAFE_CHECK_IN,      // "I'm safe", "All good"
        CUSTOM              // Other commands
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public VoiceCommand() {}

    public VoiceCommand(AuthUser user, String commandText, CommandType commandType) {
        this.user = user;
        this.commandText = commandText;
        this.commandType = commandType;
        this.timestamp = LocalDateTime.now();
    }
}
