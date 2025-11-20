package com.women.safety.features.liveLocationTracking.controller;

import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.liveLocationTracking.dto.LocationHistoryResponseDTO;
import com.women.safety.features.liveLocationTracking.dto.LocationUpdateDTO;
import com.women.safety.features.liveLocationTracking.dto.ScheduledLocationSharingDTO;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import com.women.safety.features.liveLocationTracking.model.ScheduledLocationSharing;
import com.women.safety.features.liveLocationTracking.service.LocationTrackingService;
import com.women.safety.features.liveLocationTracking.service.OpenStreetMapService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
public class LocationTrackingController {

    private final LocationTrackingService locationTrackingService;
    private final OpenStreetMapService openStreetMapService;

    public LocationTrackingController(LocationTrackingService locationTrackingService,
                                      OpenStreetMapService openStreetMapService) {
        this.locationTrackingService = locationTrackingService;
        this.openStreetMapService = openStreetMapService;
    }

    // ==================== Location Tracking ====================

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @Valid @RequestBody LocationUpdateDTO locationDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LocationTracking location = locationTrackingService.updateLocation(userDetails.getUsername(), locationDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Location updated successfully",
                "location", location
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<LocationHistoryResponseDTO> getLocationHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LocationHistoryResponseDTO history = locationTrackingService.getLocationHistory(
                userDetails.getUsername(), startDate, endDate);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Optional<LocationTracking> location = locationTrackingService.getLatestLocation(userDetails.getUsername());

        if (location.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Latest location retrieved successfully",
                    "location", location.get()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No location data found"
            ));
        }
    }

    @GetMapping("/by-alert/{alertId}")
    public ResponseEntity<List<LocationTracking>> getLocationsByAlert(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<LocationTracking> locations = locationTrackingService.getLocationsByAlert(alertId);

        return ResponseEntity.ok(locations);
    }

    // ==================== Scheduled Location Sharing ====================

    @PostMapping("/scheduled-sharing")
    public ResponseEntity<Map<String, Object>> createScheduledSharing(
            @Valid @RequestBody ScheduledLocationSharingDTO sharingDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ScheduledLocationSharing sharing = locationTrackingService.createScheduledSharing(
                userDetails.getUsername(), sharingDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Scheduled location sharing created successfully",
                "sharing", sharing
        ));
    }

    @GetMapping("/scheduled-sharing")
    public ResponseEntity<List<ScheduledLocationSharing>> getUserScheduledSharing(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ScheduledLocationSharing> sharingSessions = locationTrackingService.getUserScheduledSharing(
                userDetails.getUsername());

        return ResponseEntity.ok(sharingSessions);
    }

    @PutMapping("/scheduled-sharing/{sharingId}/arrived")
    public ResponseEntity<Map<String, Object>> markArrived(
            @PathVariable Long sharingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ScheduledLocationSharing sharing = locationTrackingService.markArrived(
                userDetails.getUsername(), sharingId);

        return ResponseEntity.ok(Map.of(
                "message", "Arrival confirmed successfully. Contacts have been notified.",
                "sharing", sharing
        ));
    }

    @PutMapping("/scheduled-sharing/{sharingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelScheduledSharing(
            @PathVariable Long sharingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ScheduledLocationSharing sharing = locationTrackingService.cancelScheduledSharing(
                userDetails.getUsername(), sharingId);

        return ResponseEntity.ok(Map.of(
                "message", "Scheduled location sharing cancelled successfully",
                "sharing", sharing
        ));
    }

    // ==================== OpenStreetMap Integration (100% FREE) ====================

    @GetMapping("/geocode/address")
    public ResponseEntity<Map<String, Object>> getCoordinatesFromAddress(
            @RequestParam String address) {

        OpenStreetMapService.GeocodeResult result = openStreetMapService.getCoordinatesFromAddress(address);

        if (result != null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "result", result
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Address not found"
            ));
        }
    }

    @GetMapping("/geocode/reverse")
    public ResponseEntity<Map<String, Object>> getAddressFromCoordinates(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        OpenStreetMapService.GeocodeResult result = openStreetMapService.getAddressFromCoordinates(latitude, longitude);

        if (result != null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "result", result
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Could not reverse geocode coordinates"
            ));
        }
    }

    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam Double originLat,
            @RequestParam Double originLng,
            @RequestParam Double destLat,
            @RequestParam Double destLng) {

        OpenStreetMapService.DistanceResult result = openStreetMapService.getDistanceInfo(
                originLat, originLng, destLat, destLng);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "distance", result.getDistanceText(),
                "distanceMeters", result.getDistanceMeters(),
                "walkingTime", result.getWalkingTimeText(),
                "drivingTime", result.getDrivingTimeText()
        ));
    }

    @GetMapping("/nearby/police")
    public ResponseEntity<Map<String, Object>> findNearestPoliceStation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OpenStreetMapService.NearbyPlace police = openStreetMapService.findNearestPoliceStation(latitude, longitude);

        if (police != null) {
            OpenStreetMapService.DistanceResult distance = openStreetMapService.getDistanceInfo(
                    latitude, longitude, police.getLatitude(), police.getLongitude());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "policeStation", police,
                    "distance", distance.getDistanceText(),
                    "walkingTime", distance.getWalkingTimeText(),
                    "drivingTime", distance.getDrivingTimeText(),
                    "mapUrl", openStreetMapService.generateMapUrl(police.getLatitude(), police.getLongitude()),
                    "directionsUrl", openStreetMapService.generateDirectionsUrl(
                            latitude, longitude, police.getLatitude(), police.getLongitude())
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No police station found nearby (within 5km)"
            ));
        }
    }

    @GetMapping("/nearby/hospital")
    public ResponseEntity<Map<String, Object>> findNearestHospital(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OpenStreetMapService.NearbyPlace hospital = openStreetMapService.findNearestHospital(latitude, longitude);

        if (hospital != null) {
            OpenStreetMapService.DistanceResult distance = openStreetMapService.getDistanceInfo(
                    latitude, longitude, hospital.getLatitude(), hospital.getLongitude());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hospital", hospital,
                    "distance", distance.getDistanceText(),
                    "walkingTime", distance.getWalkingTimeText(),
                    "drivingTime", distance.getDrivingTimeText(),
                    "mapUrl", openStreetMapService.generateMapUrl(hospital.getLatitude(), hospital.getLongitude()),
                    "directionsUrl", openStreetMapService.generateDirectionsUrl(
                            latitude, longitude, hospital.getLatitude(), hospital.getLongitude())
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No hospital found nearby (within 5km)"
            ));
        }
    }

    @GetMapping("/nearby/places")
    public ResponseEntity<Map<String, Object>> findNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<OpenStreetMapService.NearbyPlace> places = openStreetMapService.findNearbyPlaces(
                latitude, longitude, type, radius);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", places.size(),
                "places", places
        ));
    }
}

