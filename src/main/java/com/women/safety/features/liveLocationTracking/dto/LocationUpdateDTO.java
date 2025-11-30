package com.women.safety.features.liveLocationTracking.dto;

import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDTO {
    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    private Double altitude;
    private Double accuracy;
    private Double speed;
    private Double bearing;
    private String address;

    @Min(value = 0, message = "Battery level must be between 0 and 100")
    @Max(value = 100, message = "Battery level must be between 0 and 100")
    private Integer batteryLevel;

    private Boolean isCharging;
    private Boolean isMockLocation;

    private Long alertId; // Optional: associate with emergency alert

    private LocationTracking.TrackingType trackingType;

    public LocationUpdateDTO() {}

    public LocationUpdateDTO(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public @NotNull(message = "Latitude is required") @Min(value = -90, message = "Latitude must be between -90 and 90") @Max(value = 90, message = "Latitude must be between -90 and 90") Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@NotNull(message = "Latitude is required") @Min(value = -90, message = "Latitude must be between -90 and 90") @Max(value = 90, message = "Latitude must be between -90 and 90") Double latitude) {
        this.latitude = latitude;
    }

    public @NotNull(message = "Longitude is required") @Min(value = -180, message = "Longitude must be between -180 and 180") @Max(value = 180, message = "Longitude must be between -180 and 180") Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@NotNull(message = "Longitude is required") @Min(value = -180, message = "Longitude must be between -180 and 180") @Max(value = 180, message = "Longitude must be between -180 and 180") Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public @Min(value = 0, message = "Battery level must be between 0 and 100") @Max(value = 100, message = "Battery level must be between 0 and 100") Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(@Min(value = 0, message = "Battery level must be between 0 and 100") @Max(value = 100, message = "Battery level must be between 0 and 100") Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public LocationTracking.TrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(LocationTracking.TrackingType trackingType) {
        this.trackingType = trackingType;
    }
}
