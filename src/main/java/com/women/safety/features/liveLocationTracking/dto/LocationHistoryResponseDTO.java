package com.women.safety.features.liveLocationTracking.dto;

import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LocationHistoryResponseDTO {

    private List<LocationPoint> locations;
    private Integer totalPoints;
    private Double totalDistanceTraveled; // in meters
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String message;

    public LocationHistoryResponseDTO(List<LocationTracking> trackings, String message) {
        this.locations = trackings.stream()
                .map(LocationPoint::new)
                .collect(Collectors.toList());
        this.totalPoints = trackings.size();
        this.totalDistanceTraveled = calculateTotalDistance(trackings);

        if (!trackings.isEmpty()) {
            this.startTime = trackings.get(trackings.size() - 1).getTimestamp();
            this.endTime = trackings.get(0).getTimestamp();
        }
        this.message = message;
    }

    private Double calculateTotalDistance(List<LocationTracking> trackings) {
        if (trackings.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < trackings.size() - 1; i++) {
            totalDistance += trackings.get(i).distanceFrom(trackings.get(i + 1));
        }
        return totalDistance;
    }

    @Data
    public static class LocationPoint {
        private Long id;
        private Double latitude;
        private Double longitude;
        private Double altitude;
        private Double accuracy;
        private Double speed;
        private String address;
        private LocalDateTime timestamp;
        private Integer batteryLevel;
        private Boolean isCharging;

        public LocationPoint(LocationTracking tracking) {
            this.id = tracking.getId();
            this.latitude = tracking.getLatitude();
            this.longitude = tracking.getLongitude();
            this.altitude = tracking.getAltitude();
            this.accuracy = tracking.getAccuracy();
            this.speed = tracking.getSpeed();
            this.address = tracking.getAddress();
            this.timestamp = tracking.getTimestamp();
            this.batteryLevel = tracking.getBatteryLevel();
            this.isCharging = tracking.getIsCharging();
        }
    }
}
