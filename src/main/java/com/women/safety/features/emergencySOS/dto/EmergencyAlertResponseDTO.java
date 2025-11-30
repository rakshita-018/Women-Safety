package com.women.safety.features.emergencySOS.dto;

import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmergencyAlertResponseDTO {

    private Long id;
    private String alertMessage;
    private Double latitude;
    private Double longitude;
    private String locationAddress;
    private EmergencyAlert.AlertStatus alertStatus;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer contactsNotifiedCount;
    private String message;

    public EmergencyAlertResponseDTO() {}

    public EmergencyAlertResponseDTO(EmergencyAlert alert, String message) {
        this.id = alert.getId();
        this.alertMessage = alert.getAlertMessage();
        this.latitude = alert.getLatitude();
        this.longitude = alert.getLongitude();
        this.locationAddress = alert.getLocationAddress();
        this.alertStatus = alert.getAlertStatus();
        this.createdAt = alert.getCreatedAt();
        this.resolvedAt = alert.getResolvedAt();
        this.contactsNotifiedCount = alert.getContactsNotifiedCount();
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public EmergencyAlert.AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public Integer getContactsNotifiedCount() {
        return contactsNotifiedCount;
    }

    public String getMessage() {
        return message;
    }
}
