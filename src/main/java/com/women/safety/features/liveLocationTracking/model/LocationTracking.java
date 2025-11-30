package com.women.safety.features.liveLocationTracking.model;

import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_tracking", indexes = {
        @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp"),
        @Index(name = "idx_alert_timestamp", columnList = "alert_id, timestamp"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
public class LocationTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private EmergencyAlert alert;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "accuracy")
    private Double accuracy; // Accuracy in meters

    @Column(name = "speed")
    private Double speed; // Speed in m/s

    @Column(name = "bearing")
    private Double bearing; // Direction in degrees

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "battery_level")
    private Integer batteryLevel; // Battery percentage 0-100

    @Column(name = "is_charging")
    private Boolean isCharging;

    @Column(name = "is_mock_location")
    private Boolean isMockLocation; // Detect fake GPS

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type")
    private TrackingType trackingType = TrackingType.MANUAL;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TrackingType {
        MANUAL,          // User manually shared location
        AUTO_TRACKING,   // Continuous tracking during alert
        SCHEDULED,       // Scheduled location sharing
        EMERGENCY        // Emergency alert triggered
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (isMockLocation == null) {
            isMockLocation = false;
        }
    }

    public LocationTracking() {}

    public LocationTracking(AuthUser user, Double latitude, Double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
    }

    // Calculate distance from another location (in meters)
    public double distanceFrom(LocationTracking other) {
        return calculateDistance(
                this.latitude, this.longitude,
                other.getLatitude(), other.getLongitude()
        );
    }

    // Haversine formula for distance calculation
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
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

    public EmergencyAlert getAlert() {
        return alert;
    }

    public void setAlert(EmergencyAlert alert) {
        this.alert = alert;
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

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Boolean getCharging() {
        return isCharging;
    }

    public void setCharging(Boolean charging) {
        isCharging = charging;
    }

    public Boolean getMockLocation() {
        return isMockLocation;
    }

    public void setMockLocation(Boolean mockLocation) {
        isMockLocation = mockLocation;
    }

    public TrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(TrackingType trackingType) {
        this.trackingType = trackingType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
