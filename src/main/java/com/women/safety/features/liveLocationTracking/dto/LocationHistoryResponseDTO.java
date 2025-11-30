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

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
    }

    public List<LocationPoint> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationPoint> locations) {
        this.locations = locations;
    }

    public Double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public void setTotalDistanceTraveled(Double totalDistanceTraveled) {
        this.totalDistanceTraveled = totalDistanceTraveled;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
