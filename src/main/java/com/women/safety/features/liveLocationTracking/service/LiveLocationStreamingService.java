package com.women.safety.features.liveLocationTracking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.repository.EmergencyAlertRepository;
import com.women.safety.features.liveLocationTracking.dto.LocationUpdateDTO;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import com.women.safety.features.liveLocationTracking.repository.LocationTrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Real-Time Location Streaming Service using SSE (Server-Sent Events)
 *
 * Features:
 * - Live location updates every 1-3 seconds during active SOS
 * - Multiple subscribers per alert (all emergency contacts can watch)
 * - Automatic cleanup of dead connections
 * - Battery status monitoring
 */
@Service
public class LiveLocationStreamingService {

    private static final Logger logger = LoggerFactory.getLogger(LiveLocationStreamingService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    // Map: AlertId -> List of SSE Emitters (multiple contacts watching)
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> alertSubscribers = new ConcurrentHashMap<>();

    private final EmergencyAlertRepository alertRepository;
    private final LocationTrackingRepository locationRepository;
    private final AuthUserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OpenStreetMapService openStreetMapService;

    public LiveLocationStreamingService(EmergencyAlertRepository alertRepository,
                                        LocationTrackingRepository locationRepository,
                                        AuthUserRepository userRepository,
                                        ObjectMapper objectMapper,
                                        OpenStreetMapService openStreetMapService) {
        this.alertRepository = alertRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.openStreetMapService = openStreetMapService;
    }

    /**
     * Subscribe to live location updates for an alert
     * Used by emergency contacts to watch real-time location
     */
    public SseEmitter subscribe(Long alertId) {
        // Verify alert exists
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        logger.info("📡 New subscriber for alert ID: {}", alertId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Add to subscribers list
        alertSubscribers.computeIfAbsent(alertId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Handle completion/timeout
        emitter.onCompletion(() -> {
            logger.info("SSE connection completed for alert: {}", alertId);
            removeSubscriber(alertId, emitter);
        });

        emitter.onTimeout(() -> {
            logger.warn("SSE connection timeout for alert: {}", alertId);
            removeSubscriber(alertId, emitter);
        });

        emitter.onError((ex) -> {
            logger.error("SSE connection error for alert {}: {}", alertId, ex.getMessage());
            removeSubscriber(alertId, emitter);
        });

        // Send initial connection confirmation
        try {
            Map<String, Object> initialData = Map.of(
                    "type", "CONNECTION_ESTABLISHED",
                    "alertId", alertId,
                    "message", "Connected to live location stream",
                    "timestamp", LocalDateTime.now().toString()
            );
            emitter.send(SseEmitter.event()
                    .name("connection")
                    .data(objectMapper.writeValueAsString(initialData)));

            // Send latest location immediately if available
            sendLatestLocation(alertId, emitter);

        } catch (IOException e) {
            logger.error("Failed to send initial SSE message: {}", e.getMessage());
            removeSubscriber(alertId, emitter);
        }

        logger.info("📊 Total subscribers for alert {}: {}",
                alertId, alertSubscribers.getOrDefault(alertId, new CopyOnWriteArrayList<>()).size());

        return emitter;
    }

    /**
     * Broadcast location update to all subscribers of an alert
     * Called when user sends location update during active SOS
     */
    public void broadcastLocationUpdate(Long alertId, LocationUpdateDTO locationDTO, AuthUser user) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.get(alertId);

        if (subscribers == null || subscribers.isEmpty()) {
            logger.debug("No subscribers for alert: {}", alertId);
            return;
        }

        // Save location to database
        LocationTracking location = new LocationTracking();
        location.setUser(user);
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        location.setAltitude(locationDTO.getAltitude());
        location.setAccuracy(locationDTO.getAccuracy());
        location.setSpeed(locationDTO.getSpeed());
        location.setBearing(locationDTO.getBearing());
        location.setBatteryLevel(locationDTO.getBatteryLevel());
        location.setIsCharging(locationDTO.getIsCharging());
        location.setIsMockLocation(locationDTO.getIsMockLocation());
        location.setTrackingType(LocationTracking.TrackingType.EMERGENCY);
        location.setTimestamp(LocalDateTime.now());

        // Resolve address if not provided
        if (locationDTO.getAddress() == null || locationDTO.getAddress().trim().isEmpty()) {
            try {
                OpenStreetMapService.GeocodeResult geocode = openStreetMapService.getAddressFromCoordinates(
                        locationDTO.getLatitude(), locationDTO.getLongitude());
                if (geocode != null) {
                    location.setAddress(geocode.getFormattedAddress());
                }
            } catch (Exception e) {
                logger.warn("Failed to geocode during live stream: {}", e.getMessage());
            }
        } else {
            location.setAddress(locationDTO.getAddress());
        }

        // Associate with alert
        alertRepository.findById(alertId).ifPresent(location::setAlert);

        location = locationRepository.save(location);

        // Prepare broadcast data
        String mapUrl = openStreetMapService.generateMapUrl(
                location.getLatitude(),
                location.getLongitude()
        );

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("type", "LOCATION_UPDATE");
        locationData.put("alertId", alertId);
        locationData.put("locationId", location.getId());
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("altitude", location.getAltitude() != null ? location.getAltitude() : 0);
        locationData.put("accuracy", location.getAccuracy() != null ? location.getAccuracy() : 0);
        locationData.put("speed", location.getSpeed() != null ? location.getSpeed() : 0);
        locationData.put("bearing", location.getBearing() != null ? location.getBearing() : 0);
        locationData.put("address", location.getAddress() != null ? location.getAddress() : "Unknown");
        locationData.put("batteryLevel", location.getBatteryLevel() != null ? location.getBatteryLevel() : 0);
        locationData.put("isCharging", location.getIsCharging() != null ? location.getIsCharging() : false);
        locationData.put("isMockLocation", location.getIsMockLocation() != null ? location.getIsMockLocation() : false);
        locationData.put("timestamp", location.getTimestamp().toString());
        locationData.put("userName", user.getFullName());
        locationData.put("mapUrl", mapUrl);

        // Broadcast to all subscribers
        logger.info("📡 Broadcasting location to {} subscribers for alert: {}", subscribers.size(), alertId);

        subscribers.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("location")
                        .data(objectMapper.writeValueAsString(locationData)));
                return false; // Keep this emitter
            } catch (IOException e) {
                logger.warn("Failed to send to subscriber, removing: {}", e.getMessage());
                return true; // Remove this emitter
            }
        });
    }

    /**
     * Send alert status update (resolved, cancelled, etc.)
     */
    public void broadcastAlertStatusUpdate(Long alertId, String status, String message) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.get(alertId);

        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        Map<String, Object> statusData = Map.of(
                "type", "ALERT_STATUS_UPDATE",
                "alertId", alertId,
                "status", status,
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );

        logger.info("📢 Broadcasting status update to {} subscribers: {}", subscribers.size(), status);

        subscribers.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(objectMapper.writeValueAsString(statusData)));
                return false;
            } catch (IOException e) {
                logger.warn("Failed to send status update: {}", e.getMessage());
                return true;
            }
        });

        // If alert is resolved/cancelled, cleanup after delay
        if ("RESOLVED".equals(status) || "CANCELLED".equals(status)) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds
                    closeAllConnections(alertId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Send battery warning to subscribers
     */
    public void broadcastBatteryWarning(Long alertId, int batteryLevel) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.get(alertId);

        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        String warningMessage;
        if (batteryLevel <= 5) {
            warningMessage = "CRITICAL: Battery at " + batteryLevel + "%. Device may shut down soon!";
        } else if (batteryLevel <= 10) {
            warningMessage = "WARNING: Battery at " + batteryLevel + "%. Please charge device.";
        } else if (batteryLevel <= 20) {
            warningMessage = "LOW BATTERY: " + batteryLevel + "% remaining.";
        } else {
            return; // No warning needed
        }

        Map<String, Object> batteryData = Map.of(
                "type", "BATTERY_WARNING",
                "alertId", alertId,
                "batteryLevel", batteryLevel,
                "message", warningMessage,
                "timestamp", LocalDateTime.now().toString()
        );

        subscribers.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("battery")
                        .data(objectMapper.writeValueAsString(batteryData)));
            } catch (IOException e) {
                logger.warn("Failed to send battery warning: {}", e.getMessage());
            }
        });
    }

    /**
     * Get subscriber count for an alert
     */
    public int getSubscriberCount(Long alertId) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.get(alertId);
        return subscribers != null ? subscribers.size() : 0;
    }

    /**
     * Close all connections for an alert
     */
    public void closeAllConnections(Long alertId) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.remove(alertId);

        if (subscribers != null) {
            logger.info("🔌 Closing {} connections for alert: {}", subscribers.size(), alertId);
            subscribers.forEach(emitter -> {
                try {
                    Map<String, Object> closeData = Map.of(
                            "type", "CONNECTION_CLOSED",
                            "alertId", alertId,
                            "message", "Live tracking ended",
                            "timestamp", LocalDateTime.now().toString()
                    );
                    emitter.send(SseEmitter.event()
                            .name("close")
                            .data(objectMapper.writeValueAsString(closeData)));
                    emitter.complete();
                } catch (IOException e) {
                    logger.debug("Error closing emitter: {}", e.getMessage());
                }
            });
            subscribers.clear();
        }
    }

    // Private helper methods

    private void removeSubscriber(Long alertId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> subscribers = alertSubscribers.get(alertId);
        if (subscribers != null) {
            subscribers.remove(emitter);
            if (subscribers.isEmpty()) {
                alertSubscribers.remove(alertId);
                logger.info("🧹 Cleaned up empty subscriber list for alert: {}", alertId);
            }
        }
    }

    private void sendLatestLocation(Long alertId, SseEmitter emitter) {
        try {
            EmergencyAlert alert = alertRepository.findById(alertId).orElse(null);
            if (alert == null) return;

            locationRepository.findLatestLocationByAlert(alert).ifPresent(location -> {
                try {
                    Map<String, Object> locationData = Map.of(
                            "type", "LATEST_LOCATION",
                            "alertId", alertId,
                            "latitude", location.getLatitude(),
                            "longitude", location.getLongitude(),
                            "address", location.getAddress() != null ? location.getAddress() : "Unknown",
                            "batteryLevel", location.getBatteryLevel() != null ? location.getBatteryLevel() : 0,
                            "timestamp", location.getTimestamp().toString()
                    );
                    emitter.send(SseEmitter.event()
                            .name("latest")
                            .data(objectMapper.writeValueAsString(locationData)));
                } catch (IOException e) {
                    logger.error("Failed to send latest location: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error sending latest location: {}", e.getMessage());
        }
    }
}
