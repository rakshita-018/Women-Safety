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
}
