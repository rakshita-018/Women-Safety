package com.women.safety.features.liveLocationTracking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledLocationSharingDTO {

    private Long id;

    private String sessionName;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @Min(value = 10, message = "Update interval must be at least 10 seconds")
    private Integer updateIntervalSeconds;

    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationAddress;
    private LocalDateTime expectedArrivalTime;

    private Boolean notifyContactsOnStart;
    private Boolean notifyContactsOnArrival;
    private Boolean notifyContactsOnDelay;
    private Boolean autoAlertIfNotArrived;

    private String notes;

    public ScheduledLocationSharingDTO() {}

    public ScheduledLocationSharingDTO(LocalDateTime startTime, Integer durationMinutes) {
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.endTime = startTime.plusMinutes(durationMinutes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public @Min(value = 1, message = "Duration must be at least 1 minute") Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(@Min(value = 1, message = "Duration must be at least 1 minute") Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public @Min(value = 10, message = "Update interval must be at least 10 seconds") Integer getUpdateIntervalSeconds() {
        return updateIntervalSeconds;
    }

    public void setUpdateIntervalSeconds(@Min(value = 10, message = "Update interval must be at least 10 seconds") Integer updateIntervalSeconds) {
        this.updateIntervalSeconds = updateIntervalSeconds;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
