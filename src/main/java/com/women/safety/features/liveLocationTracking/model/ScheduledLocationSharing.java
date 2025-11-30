package com.women.safety.features.liveLocationTracking.model;

import com.women.safety.features.authentication.model.AuthUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_location_sharing")
@Data
public class ScheduledLocationSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "session_name")
    private String sessionName; // e.g., "First Date", "Uber Ride", "Night Walk"

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "update_interval_seconds")
    private Integer updateIntervalSeconds = 30; // Default 30 seconds

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SharingStatus status = SharingStatus.SCHEDULED;

    @Column(name = "destination_latitude")
    private Double destinationLatitude;

    @Column(name = "destination_longitude")
    private Double destinationLongitude;

    @Column(name = "destination_address", columnDefinition = "TEXT")
    private String destinationAddress;

    @Column(name = "expected_arrival_time")
    private LocalDateTime expectedArrivalTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Column(name = "notify_contacts_on_start")
    private Boolean notifyContactsOnStart = true;

    @Column(name = "notify_contacts_on_arrival")
    private Boolean notifyContactsOnArrival = true;

    @Column(name = "notify_contacts_on_delay")
    private Boolean notifyContactsOnDelay = true;

    @Column(name = "auto_alert_if_not_arrived")
    private Boolean autoAlertIfNotArrived = false;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "total_updates_sent")
    private Integer totalUpdatesSent = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Additional context (e.g., "Meeting John at cafe")

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SharingStatus {
        SCHEDULED,     // Not started yet
        ACTIVE,        // Currently sharing location
        COMPLETED,     // Successfully completed
        CANCELLED,     // User cancelled
        EXPIRED,       // Time expired
        ALERTED        // Auto-alert triggered
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (durationMinutes != null && endTime == null) {
            endTime = startTime.plusMinutes(durationMinutes);
        }

        if (totalUpdatesSent == null) {
            totalUpdatesSent = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ScheduledLocationSharing() {}

    public ScheduledLocationSharing(AuthUser user, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    // Check if session is currently active
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == SharingStatus.ACTIVE &&
                now.isAfter(startTime) &&
                now.isBefore(endTime);
    }

    // Check if session should be active now
    public boolean shouldBeActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == SharingStatus.SCHEDULED &&
                now.isAfter(startTime) &&
                now.isBefore(endTime);
    }

    // Check if session has expired
    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    // Check if user is delayed (not arrived at expected time)
    public boolean isDelayed() {
        if (expectedArrivalTime == null || actualArrivalTime != null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expectedArrivalTime.plusMinutes(15)); // 15 min grace period
    }

    // Increment update count
    public void recordLocationUpdate() {
        this.totalUpdatesSent++;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    // Mark as arrived
    public void markArrived() {
        this.actualArrivalTime = LocalDateTime.now();
        this.status = SharingStatus.COMPLETED;
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

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getUpdateIntervalSeconds() {
        return updateIntervalSeconds;
    }

    public void setUpdateIntervalSeconds(Integer updateIntervalSeconds) {
        this.updateIntervalSeconds = updateIntervalSeconds;
    }

    public SharingStatus getStatus() {
        return status;
    }

    public void setStatus(SharingStatus status) {
        this.status = status;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public LocalDateTime getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public LocalDateTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(LocalDateTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public Boolean getNotifyContactsOnStart() {
        return notifyContactsOnStart;
    }

    public void setNotifyContactsOnStart(Boolean notifyContactsOnStart) {
        this.notifyContactsOnStart = notifyContactsOnStart;
    }

    public Boolean getNotifyContactsOnArrival() {
        return notifyContactsOnArrival;
    }

    public void setNotifyContactsOnArrival(Boolean notifyContactsOnArrival) {
        this.notifyContactsOnArrival = notifyContactsOnArrival;
    }

    public Boolean getNotifyContactsOnDelay() {
        return notifyContactsOnDelay;
    }

    public void setNotifyContactsOnDelay(Boolean notifyContactsOnDelay) {
        this.notifyContactsOnDelay = notifyContactsOnDelay;
    }

    public Boolean getAutoAlertIfNotArrived() {
        return autoAlertIfNotArrived;
    }

    public void setAutoAlertIfNotArrived(Boolean autoAlertIfNotArrived) {
        this.autoAlertIfNotArrived = autoAlertIfNotArrived;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public Integer getTotalUpdatesSent() {
        return totalUpdatesSent;
    }

    public void setTotalUpdatesSent(Integer totalUpdatesSent) {
        this.totalUpdatesSent = totalUpdatesSent;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
