package com.women.safety.features.liveLocationTracking.controller;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.repository.EmergencyAlertRepository;
import com.women.safety.features.liveLocationTracking.dto.LocationUpdateDTO;
import com.women.safety.features.liveLocationTracking.service.LiveLocationStreamingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Real-Time Location Streaming Controller
 *
 * SSE Endpoints for live location tracking during emergencies
 */
@RestController
@RequestMapping("/api/location/live")
public class LiveLocationStreamingController {

    private static final Logger logger = LoggerFactory.getLogger(LiveLocationStreamingController.class);

    private final LiveLocationStreamingService streamingService;
    private final EmergencyAlertRepository alertRepository;
    private final AuthUserRepository userRepository;

    public LiveLocationStreamingController(LiveLocationStreamingService streamingService,
                                           EmergencyAlertRepository alertRepository,
                                           AuthUserRepository userRepository) {
        this.streamingService = streamingService;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }

    /**
     * Subscribe to live location stream for an alert
     *
     * GET /api/location/live/stream/{alertId}
     *
     * Emergency contacts use this to watch real-time location
     * Returns SSE stream that sends location updates every 1-3 seconds
     */
    @GetMapping(value = "/stream/{alertId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLocation(@PathVariable Long alertId) {
        logger.info("📡 SSE stream requested for alert: {}", alertId);
        return streamingService.subscribe(alertId);
    }

    /**
     * Send live location update during active SOS
     *
     * POST /api/location/live/update/{alertId}
     *
     * Mobile app calls this every 1-3 seconds during emergency
     * Broadcasts to all connected subscribers via SSE
     */
    @PostMapping("/update/{alertId}")
    public ResponseEntity<Map<String, Object>> sendLiveLocationUpdate(
            @PathVariable Long alertId,
            @Valid @RequestBody LocationUpdateDTO locationDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthUser user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        // Verify user owns this alert
        if (!alert.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Unauthorized access to alert"
            ));
        }

        // Broadcast to all subscribers
        streamingService.broadcastLocationUpdate(alertId, locationDTO, user);

        // Check battery and send warning if needed
        if (locationDTO.getBatteryLevel() != null && locationDTO.getBatteryLevel() <= 20) {
            streamingService.broadcastBatteryWarning(alertId, locationDTO.getBatteryLevel());
        }

        int subscriberCount = streamingService.getSubscriberCount(alertId);

        logger.info("✅ Live location broadcasted to {} subscribers", subscriberCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Location broadcasted to " + subscriberCount + " subscriber(s)",
                "alertId", alertId,
                "subscriberCount", subscriberCount,
                "latitude", locationDTO.getLatitude(),
                "longitude", locationDTO.getLongitude(),
                "batteryLevel", locationDTO.getBatteryLevel() != null ? locationDTO.getBatteryLevel() : 0
        ));
    }

    /**
     * Update alert status and notify all subscribers
     *
     * PUT /api/location/live/status/{alertId}
     */
    @PutMapping("/status/{alertId}")
    public ResponseEntity<Map<String, Object>> updateAlertStatus(
            @PathVariable Long alertId,
            @RequestParam String status,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthUser user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        // Update alert status
        EmergencyAlert.AlertStatus newStatus;
        try {
            newStatus = EmergencyAlert.AlertStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid status: " + status
            ));
        }

        alert.setAlertStatus(newStatus);
        if (newStatus == EmergencyAlert.AlertStatus.RESOLVED ||
                newStatus == EmergencyAlert.AlertStatus.CANCELLED) {
            alert.setResolvedAt(java.time.LocalDateTime.now());
        }
        alertRepository.save(alert);

        // Broadcast status update to all subscribers
        String broadcastMessage = message != null ? message : "Alert status changed to: " + status;
        streamingService.broadcastAlertStatusUpdate(alertId, status, broadcastMessage);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert status updated",
                "alertId", alertId,
                "status", status,
                "subscribersNotified", streamingService.getSubscriberCount(alertId)
        ));
    }

    /**
     * Get subscriber count for an alert
     *
     * GET /api/location/live/subscribers/{alertId}
     */
    @GetMapping("/subscribers/{alertId}")
    public ResponseEntity<Map<String, Object>> getSubscriberCount(@PathVariable Long alertId) {
        int count = streamingService.getSubscriberCount(alertId);

        return ResponseEntity.ok(Map.of(
                "alertId", alertId,
                "subscriberCount", count,
                "isBeingWatched", count > 0
        ));
    }

    /**
     * Manually close all connections for an alert
     *
     * POST /api/location/live/close/{alertId}
     */
    @PostMapping("/close/{alertId}")
    public ResponseEntity<Map<String, Object>> closeConnections(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthUser user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        streamingService.closeAllConnections(alertId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All live tracking connections closed for alert " + alertId
        ));
    }

    /**
     * Test SSE endpoint - for development/testing
     *
     * GET /api/location/live/test
     */
    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSSE() {
        SseEmitter emitter = new SseEmitter(60000L);

        new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Map<String, Object> data = Map.of(
                            "message", "Test message " + i,
                            "timestamp", System.currentTimeMillis()
                    );
                    emitter.send(SseEmitter.event()
                            .name("test")
                            .data(data));
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
