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

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
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

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}
