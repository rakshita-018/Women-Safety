package com.women.safety.features.emergencySOS.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alert")
@Data
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;
    private Double latitude;
    private Double longitude;

    @Column(name = "location_address", columnDefinition = "TEXT")
    private String locationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status")
    private AlertStatus alertStatus = AlertStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @Column(name = "contacts_notified_count")
    private Integer contactsNotifiedCount = 0;

    public enum AlertStatus {
        ACTIVE, RESOLVED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public EmergencyAlert() {}

    public EmergencyAlert(AuthUser user, String alertMessage, Double latitude, Double longitude, String locationAddress) {
        this.user = user;
        this.alertMessage = alertMessage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
    }
}
