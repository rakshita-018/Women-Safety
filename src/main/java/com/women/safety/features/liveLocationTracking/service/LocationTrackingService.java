package com.women.safety.features.liveLocationTracking.service;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import com.women.safety.features.emergencySOS.repository.EmergencyAlertRepository;
import com.women.safety.features.emergencySOS.repository.EmergencyContactRepository;
import com.women.safety.features.emergencySOS.utils.TwilioSmsService;
import com.women.safety.features.liveLocationTracking.dto.LocationHistoryResponseDTO;
import com.women.safety.features.liveLocationTracking.dto.LocationUpdateDTO;
import com.women.safety.features.liveLocationTracking.dto.ScheduledLocationSharingDTO;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import com.women.safety.features.liveLocationTracking.model.ScheduledLocationSharing;
import com.women.safety.features.liveLocationTracking.repository.LocationTrackingRepository;
import com.women.safety.features.liveLocationTracking.repository.ScheduledLocationSharingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocationTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(LocationTrackingService.class);
    private static final int MAX_LOCATION_HISTORY_DAYS = 30; // Keep location data for 30 days

    private final LocationTrackingRepository locationTrackingRepository;
    private final ScheduledLocationSharingRepository scheduledSharingRepository;
    private final AuthUserRepository authUserRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final TwilioSmsService twilioSmsService;
    private final OpenStreetMapService openStreetMapService;

    public LocationTrackingService(LocationTrackingRepository locationTrackingRepository,
                                   ScheduledLocationSharingRepository scheduledSharingRepository,
                                   AuthUserRepository authUserRepository,
                                   EmergencyAlertRepository emergencyAlertRepository,
                                   EmergencyContactRepository emergencyContactRepository,
                                   TwilioSmsService twilioSmsService,
                                   OpenStreetMapService openStreetMapService) {
        this.locationTrackingRepository = locationTrackingRepository;
        this.scheduledSharingRepository = scheduledSharingRepository;
        this.authUserRepository = authUserRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.twilioSmsService = twilioSmsService;
        this.openStreetMapService = openStreetMapService;
    }

    // ==================== Location Updates ====================

    @Transactional
    public LocationTracking updateLocation(String userEmail, LocationUpdateDTO locationDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
        location.setTrackingType(locationDTO.getTrackingType() != null ?
                locationDTO.getTrackingType() : LocationTracking.TrackingType.MANUAL);

        // Get address from Google Maps if not provided
        if (locationDTO.getAddress() == null || locationDTO.getAddress().trim().isEmpty()) {
            try {
                OpenStreetMapService.GeocodeResult geocodeResult = openStreetMapService.getAddressFromCoordinates(
                        locationDTO.getLatitude(), locationDTO.getLongitude());
                if (geocodeResult != null) {
                    location.setAddress(geocodeResult.getFormattedAddress());
                    logger.info("✅ Address resolved via OpenStreetMap (FREE): {}", geocodeResult.getFormattedAddress());
                } else {
                    // Fallback if geocoding fails
                    location.setAddress(String.format("Location: %.4f, %.4f",
                            locationDTO.getLatitude(), locationDTO.getLongitude()));
                    logger.warn("⚠️ Could not geocode, using coordinates as address");
                }
            } catch (Exception e) {
                logger.error("❌ Error getting address from OpenStreetMap: {}", e.getMessage());
                // Fallback to coordinates
                location.setAddress(String.format("Location: %.4f, %.4f",
                        locationDTO.getLatitude(), locationDTO.getLongitude()));
            }
        } else {
            location.setAddress(locationDTO.getAddress());
        }

        // Associate with alert if provided
        if (locationDTO.getAlertId() != null) {
            EmergencyAlert alert = emergencyAlertRepository.findById(locationDTO.getAlertId())
                    .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
            location.setAlert(alert);
            location.setTrackingType(LocationTracking.TrackingType.EMERGENCY);
        }

        location = locationTrackingRepository.save(location);

        // Update scheduled sharing sessions
        updateActiveSharingSessions(user, location);

        logger.info("Location updated for user: {}", userEmail);
        return location;
    }

    public LocationHistoryResponseDTO getLocationHistory(String userEmail, LocalDateTime startDate, LocalDateTime endDate) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<LocationTracking> locations = locationTrackingRepository.findLocationHistory(user, startDate, endDate);

        return new LocationHistoryResponseDTO(locations, "Location history retrieved successfully");
    }

    public Optional<LocationTracking> getLatestLocation(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return locationTrackingRepository.findLatestLocation(user);
    }

    public List<LocationTracking> getLocationsByAlert(Long alertId) {
        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        return locationTrackingRepository.findByAlertOrderByTimestampDesc(alert);
    }

    // ==================== Scheduled Location Sharing ====================

    @Transactional
    public ScheduledLocationSharing createScheduledSharing(String userEmail, ScheduledLocationSharingDTO sharingDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Calculate end time if duration is provided
        LocalDateTime endTime = sharingDTO.getEndTime();
        if (endTime == null && sharingDTO.getDurationMinutes() != null) {
            endTime = sharingDTO.getStartTime().plusMinutes(sharingDTO.getDurationMinutes());
        }

        if (endTime == null) {
            throw new IllegalArgumentException("Either endTime or durationMinutes must be provided");
        }

        ScheduledLocationSharing sharing = new ScheduledLocationSharing(user, sharingDTO.getStartTime(), endTime);
        sharing.setSessionName(sharingDTO.getSessionName());
        sharing.setDurationMinutes(sharingDTO.getDurationMinutes());
        sharing.setUpdateIntervalSeconds(sharingDTO.getUpdateIntervalSeconds() != null ?
                sharingDTO.getUpdateIntervalSeconds() : 30);
        sharing.setDestinationLatitude(sharingDTO.getDestinationLatitude());
        sharing.setDestinationLongitude(sharingDTO.getDestinationLongitude());
        sharing.setDestinationAddress(sharingDTO.getDestinationAddress());
        sharing.setExpectedArrivalTime(sharingDTO.getExpectedArrivalTime());
        sharing.setNotifyContactsOnStart(sharingDTO.getNotifyContactsOnStart());
        sharing.setNotifyContactsOnArrival(sharingDTO.getNotifyContactsOnArrival());
        sharing.setNotifyContactsOnDelay(sharingDTO.getNotifyContactsOnDelay());
        sharing.setAutoAlertIfNotArrived(sharingDTO.getAutoAlertIfNotArrived());
        sharing.setNotes(sharingDTO.getNotes());

        return scheduledSharingRepository.save(sharing);
    }

    public List<ScheduledLocationSharing> getUserScheduledSharing(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return scheduledSharingRepository.findByUserOrderByStartTimeDesc(user);
    }

    @Transactional
    public ScheduledLocationSharing markArrived(String userEmail, Long sharingId) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ScheduledLocationSharing sharing = scheduledSharingRepository.findById(sharingId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled sharing not found"));

        if (!sharing.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        sharing.markArrived();
        sharing = scheduledSharingRepository.save(sharing);

        // Notify contacts about safe arrival
        if (sharing.getNotifyContactsOnArrival()) {
            notifyContactsAboutArrival(user, sharing);
        }

        return sharing;
    }

    @Transactional
    public ScheduledLocationSharing cancelScheduledSharing(String userEmail, Long sharingId) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ScheduledLocationSharing sharing = scheduledSharingRepository.findById(sharingId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled sharing not found"));

        if (!sharing.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        sharing.setStatus(ScheduledLocationSharing.SharingStatus.CANCELLED);
        return scheduledSharingRepository.save(sharing);
    }

    // ==================== Background Jobs ====================

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledSessions() {
        LocalDateTime now = LocalDateTime.now();

        // Activate scheduled sessions
        List<ScheduledLocationSharing> toActivate = scheduledSharingRepository.findSessionsToActivate(now);
        for (ScheduledLocationSharing sharing : toActivate) {
            sharing.setStatus(ScheduledLocationSharing.SharingStatus.ACTIVE);
            scheduledSharingRepository.save(sharing);

            if (sharing.getNotifyContactsOnStart()) {
                notifyContactsAboutSharingStart(sharing);
            }
        }

        // Expire active sessions
        List<ScheduledLocationSharing> toExpire = scheduledSharingRepository.findExpiredActiveSessions(now);
        for (ScheduledLocationSharing sharing : toExpire) {
            sharing.setStatus(ScheduledLocationSharing.SharingStatus.EXPIRED);
            scheduledSharingRepository.save(sharing);
        }

        // Check for delayed arrivals
        List<ScheduledLocationSharing> delayed = scheduledSharingRepository.findDelayedSessions(now.minusMinutes(15));
        for (ScheduledLocationSharing sharing : delayed) {
            if (sharing.getNotifyContactsOnDelay()) {
                notifyContactsAboutDelay(sharing);
            }

            if (sharing.getAutoAlertIfNotArrived()) {
                // Trigger automatic emergency alert
                triggerAutoAlert(sharing);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldLocationData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(MAX_LOCATION_HISTORY_DAYS);
        try {
            locationTrackingRepository.deleteOldLocations(cutoffDate);
            logger.info("Cleaned up location data older than {} days", MAX_LOCATION_HISTORY_DAYS);
        } catch (Exception e) {
            logger.error("Error cleaning up old location data: {}", e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private void updateActiveSharingSessions(AuthUser user, LocationTracking location) {
        List<ScheduledLocationSharing> activeSessions = scheduledSharingRepository.findActiveSharing(user);

        for (ScheduledLocationSharing session : activeSessions) {
            session.recordLocationUpdate();
            scheduledSharingRepository.save(session);

            // Send location update to contacts (implement based on your needs)
            // This could send SMS updates at intervals
        }
    }

    @Async
    protected void notifyContactsAboutSharingStart(ScheduledLocationSharing sharing) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserOrderByCreatedAtAsc(sharing.getUser());

        String message = String.format("📍 LOCATION SHARING STARTED\n\n" +
                        "%s has started sharing their location with you.\n" +
                        "Session: %s\n" +
                        "Duration: %d minutes\n" +
                        "Expected end: %s\n\n" +
                        "You will receive updates about their location.",
                sharing.getUser().getFullName(),
                sharing.getSessionName() != null ? sharing.getSessionName() : "Location Sharing",
                sharing.getDurationMinutes(),
                sharing.getEndTime());

        for (EmergencyContact contact : contacts) {
            twilioSmsService.sendSms(contact.getPhoneNumber(), message);
        }
    }

    @Async
    protected void notifyContactsAboutArrival(AuthUser user, ScheduledLocationSharing sharing) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserOrderByCreatedAtAsc(user);

        String message = String.format("✅ SAFE ARRIVAL CONFIRMED\n\n" +
                        "%s has arrived safely at their destination.\n" +
                        "Session: %s\n" +
                        "Arrival time: %s",
                user.getFullName(),
                sharing.getSessionName() != null ? sharing.getSessionName() : "Location Sharing",
                sharing.getActualArrivalTime());

        for (EmergencyContact contact : contacts) {
            twilioSmsService.sendSms(contact.getPhoneNumber(), message);
        }
    }

    @Async
    protected void notifyContactsAboutDelay(ScheduledLocationSharing sharing) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserOrderByCreatedAtAsc(sharing.getUser());

        String message = String.format("⚠️ DELAYED ARRIVAL ALERT\n\n" +
                        "%s has not arrived at their expected destination.\n" +
                        "Expected arrival: %s\n" +
                        "Session: %s\n\n" +
                        "Please check on them.",
                sharing.getUser().getFullName(),
                sharing.getExpectedArrivalTime(),
                sharing.getSessionName() != null ? sharing.getSessionName() : "Location Sharing");

        for (EmergencyContact contact : contacts) {
            twilioSmsService.sendSms(contact.getPhoneNumber(), message);
        }
    }

    private void triggerAutoAlert(ScheduledLocationSharing sharing) {
        sharing.setStatus(ScheduledLocationSharing.SharingStatus.ALERTED);
        scheduledSharingRepository.save(sharing);

        // Create emergency alert
        EmergencyAlert alert = new EmergencyAlert();
        alert.setUser(sharing.getUser());
        alert.setAlertMessage("AUTO ALERT: User did not arrive at expected destination on time");

        // Get latest location
        Optional<LocationTracking> latestLocation = locationTrackingRepository.findLatestLocation(sharing.getUser());
        if (latestLocation.isPresent()) {
            alert.setLatitude(latestLocation.get().getLatitude());
            alert.setLongitude(latestLocation.get().getLongitude());
            alert.setLocationAddress(latestLocation.get().getAddress());
        }

        emergencyAlertRepository.save(alert);
        logger.warn("Auto alert triggered for delayed arrival: {}", sharing.getUser().getEmail());
    }
}

