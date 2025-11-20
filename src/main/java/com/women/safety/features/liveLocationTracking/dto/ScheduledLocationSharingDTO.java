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
}
