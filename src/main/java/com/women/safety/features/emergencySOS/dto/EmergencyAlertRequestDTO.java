package com.women.safety.features.emergencySOS.dto;

import lombok.Data;

@Data
public class EmergencyAlertRequestDTO {

    private String alertMessage;
    private Double latitude;
    private Double longitude;
    private String locationAddress;

    public EmergencyAlertRequestDTO() {}

    public EmergencyAlertRequestDTO(String alertMessage, Double latitude, Double longitude, String locationAddress) {
        this.alertMessage = alertMessage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
    }
}
