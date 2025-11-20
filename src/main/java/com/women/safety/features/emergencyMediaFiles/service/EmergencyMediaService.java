package com.women.safety.features.emergencyMediaFiles.service;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.emergencyMediaFiles.dto.EnhancedSOSRequestDTO;
import com.women.safety.features.emergencyMediaFiles.dto.MediaSummaryDTO;
import com.women.safety.features.emergencyMediaFiles.dto.MediaUploadResponseDTO;
import com.women.safety.features.emergencyMediaFiles.model.EmergencyMedia;
import com.women.safety.features.emergencyMediaFiles.repository.EmergencyMediaRepository;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertResponseDTO;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import com.women.safety.features.emergencySOS.repository.EmergencyAlertRepository;
import com.women.safety.features.emergencySOS.repository.EmergencyContactRepository;
import com.women.safety.features.emergencySOS.utils.TwilioSmsService;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import com.women.safety.features.liveLocationTracking.repository.LocationTrackingRepository;
import com.women.safety.features.liveLocationTracking.service.OpenStreetMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmergencyMediaService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyMediaService.class);

    private final EmergencyMediaRepository mediaRepository;
    private final EmergencyAlertRepository alertRepository;
    private final EmergencyContactRepository contactRepository;
    private final AuthUserRepository userRepository;
    private final LocationTrackingRepository locationRepository;
    private final FileStorageService fileStorageService;
    private final TwilioSmsService twilioSmsService;
    private final OpenStreetMapService openStreetMapService;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public EmergencyMediaService(EmergencyMediaRepository mediaRepository,
                                 EmergencyAlertRepository alertRepository,
                                 EmergencyContactRepository contactRepository,
                                 AuthUserRepository userRepository,
                                 LocationTrackingRepository locationRepository,
                                 FileStorageService fileStorageService,
                                 TwilioSmsService twilioSmsService,
                                 OpenStreetMapService openStreetMapService) {
        this.mediaRepository = mediaRepository;
        this.alertRepository = alertRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.fileStorageService = fileStorageService;
        this.twilioSmsService = twilioSmsService;
        this.openStreetMapService = openStreetMapService;

        // Initialize file storage
        fileStorageService.init();
    }

    // ==================== Enhanced SOS Workflow ====================

    /**
     * STEP 1: Trigger Fast Initial SOS
     * Creates alert and sends FIRST SMS immediately (NO media)
     */
    @Transactional
    public EmergencyAlertResponseDTO triggerFastSOS(String userEmail, EnhancedSOSRequestDTO request) {
        logger.info("FAST SOS TRIGGERED by user: {}", userEmail);

        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user has emergency contacts
        List<EmergencyContact> contacts = contactRepository.findByUserOrderByIsPrimaryDescCreatedAtAsc(user);
        if (contacts.isEmpty()) {
            throw new IllegalArgumentException("No emergency contacts found. Please add contacts first.");
        }

        // Create emergency alert
        EmergencyAlert alert = new EmergencyAlert();
        alert.setUser(user);
        alert.setAlertMessage(request.getAlertMessage() != null ?
                request.getAlertMessage() : "EMERGENCY! I need immediate help!");
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setLocationAddress(request.getLocationAddress());
        alert.setAlertStatus(EmergencyAlert.AlertStatus.ACTIVE);

        alert = alertRepository.save(alert);

        // Get/resolve address if not provided
        String address = alert.getLocationAddress();
        if (address == null || address.trim().isEmpty()) {
            try {
                OpenStreetMapService.GeocodeResult geocode = openStreetMapService.getAddressFromCoordinates(
                        request.getLatitude(), request.getLongitude());
                if (geocode != null) {
                    address = geocode.getFormattedAddress();
                    alert.setLocationAddress(address);
                    alertRepository.save(alert);
                }
            } catch (Exception e) {
                logger.warn("Failed to geocode address: {}", e.getMessage());
                address = String.format("Location: %.4f, %.4f", request.getLatitude(), request.getLongitude());
            }
        }

        // Store location
        if (request.getLatitude() != null && request.getLongitude() != null) {
            LocationTracking location = new LocationTracking();
            location.setUser(user);
            location.setAlert(alert);
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setAddress(address);
            location.setBatteryLevel(request.getBatteryLevel());
            location.setIsCharging(request.getIsCharging());
            location.setTrackingType(LocationTracking.TrackingType.EMERGENCY);
            locationRepository.save(location);
        }

        // ⚡ SEND FIRST SMS IMMEDIATELY (FASTEST - NO MEDIA)
        sendInitialEmergencySMS(user, alert, contacts, request.getTriggerType());

        logger.info("Fast SOS alert created: ID={}, Initial SMS sent", alert.getId());

        return new EmergencyAlertResponseDTO(alert,
                "Emergency alert sent! Contacts notified immediately. Media upload can follow.");
    }

    /**
     * Send initial fast SMS (NO media links)
     */
    @Async
    protected void sendInitialEmergencySMS(AuthUser user, EmergencyAlert alert,
                                           List<EmergencyContact> contacts, String triggerType) {
        String userName = user.getFullName();
        String mapUrl = openStreetMapService.generateMapUrl(alert.getLatitude(), alert.getLongitude());

        // Find nearest police and hospital
        OpenStreetMapService.NearbyPlace police = openStreetMapService.findNearestPoliceStation(
                alert.getLatitude(), alert.getLongitude());
        OpenStreetMapService.NearbyPlace hospital = openStreetMapService.findNearestHospital(
                alert.getLatitude(), alert.getLongitude());

        StringBuilder message = new StringBuilder();
        message.append("EMERGENCY ALERT\n\n");
        message.append("From: ").append(userName).append("\n");

        if (triggerType != null) {
            message.append("Trigger: ").append(triggerType).append("\n");
        }

        message.append("\nLOCATION:\n");
        message.append(alert.getLocationAddress() != null ? alert.getLocationAddress() : "Unknown").append("\n");
        message.append("Live Map: ").append(mapUrl).append("\n\n");

        if (police != null) {
            message.append("Nearest Police: ").append(police.getName())
                    .append(" (").append(police.getDistanceText()).append(")\n");
        }

        if (hospital != null) {
            message.append("Nearest Hospital: ").append(hospital.getName())
                    .append(" (").append(hospital.getDistanceText()).append(")\n");
        }

        message.append("\nCheck on ").append(userName).append(" IMMEDIATELY!");

        String smsText = message.toString();
        int successCount = 0;

        for (EmergencyContact contact : contacts) {
            try {
                boolean sent = twilioSmsService.sendSms(contact.getPhoneNumber(), smsText);
                if (sent) successCount++;
                Thread.sleep(1000); // Rate limiting
            } catch (Exception e) {
                logger.error("Failed to send initial SMS to {}: {}", contact.getContactName(), e.getMessage());
            }
        }

        alert.setContactsNotifiedCount(successCount);
        alertRepository.save(alert);

        logger.info("Initial emergency SMS sent to {} contacts", successCount);
    }

    // ==================== Media Upload (STEP 2) ====================

    /**
     * Upload audio recording
     */
    @Transactional
    public MediaUploadResponseDTO uploadAudio(String userEmail, Long alertId, MultipartFile file,
                                              Integer durationSeconds, Double latitude, Double longitude) {
        logger.info("Uploading audio for alert: {}", alertId);

        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        // Validate ownership
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to this alert");
        }

        // Validate file
        if (!fileStorageService.isValidAudioFile(file)) {
            throw new IllegalArgumentException("Invalid audio file format. Accepted: .aac, .m4a, .mp3");
        }

        // Store file
        FileStorageService.StorageResult result = fileStorageService.storeAudio(file, alertId, user.getId());

        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to store audio file: " + result.getErrorMessage());
        }

        // Create media record
        EmergencyMedia media = new EmergencyMedia(alert, user, EmergencyMedia.MediaType.AUDIO,
                result.getFileName(), result.getRelativePath());
        media.setFileSize(result.getFileSize());
        media.setFileExtension(result.getFileExtension());
        media.setMimeType(result.getMimeType());
        media.setDurationSeconds(durationSeconds);
        media.setLatitude(latitude);
        media.setLongitude(longitude);
        media.setRecordedAt(LocalDateTime.now());
        media.setUploadStatus(EmergencyMedia.UploadStatus.COMPLETED);

        media = mediaRepository.save(media);

        logger.info("Audio uploaded successfully: {}", media.getFileName());

        // Check if all expected media is uploaded, then send follow-up SMS
        checkAndSendFollowUpSMS(alert);

        return new MediaUploadResponseDTO(media, "Audio uploaded successfully", baseUrl);
    }

    /**
     * Upload photo
     */
    @Transactional
    public MediaUploadResponseDTO uploadPhoto(String userEmail, Long alertId, MultipartFile file,
                                              Double latitude, Double longitude) {
        logger.info("Uploading photo for alert: {}", alertId);

        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        if (!fileStorageService.isValidPhotoFile(file)) {
            throw new IllegalArgumentException("Invalid photo format. Accepted: .jpg, .png");
        }

        FileStorageService.StorageResult result = fileStorageService.storePhoto(file, alertId, user.getId());

        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to store photo: " + result.getErrorMessage());
        }

        EmergencyMedia media = new EmergencyMedia(alert, user, EmergencyMedia.MediaType.PHOTO,
                result.getFileName(), result.getRelativePath());
        media.setFileSize(result.getFileSize());
        media.setFileExtension(result.getFileExtension());
        media.setMimeType(result.getMimeType());
        media.setLatitude(latitude);
        media.setLongitude(longitude);
        media.setRecordedAt(LocalDateTime.now());
        media.setUploadStatus(EmergencyMedia.UploadStatus.COMPLETED);

        media = mediaRepository.save(media);

        logger.info("Photo uploaded successfully: {}", media.getFileName());

        checkAndSendFollowUpSMS(alert);

        return new MediaUploadResponseDTO(media, "Photo uploaded successfully", baseUrl);
    }

    /**
     * Upload video
     */
    @Transactional
    public MediaUploadResponseDTO uploadVideo(String userEmail, Long alertId, MultipartFile file,
                                              Integer durationSeconds, Double latitude, Double longitude) {
        logger.info("Uploading video for alert: {}", alertId);

        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        if (!fileStorageService.isValidVideoFile(file)) {
            throw new IllegalArgumentException("Invalid video format. Accepted: .mp4, .mov");
        }

        FileStorageService.StorageResult result = fileStorageService.storeVideo(file, alertId, user.getId());

        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to store video: " + result.getErrorMessage());
        }

        EmergencyMedia media = new EmergencyMedia(alert, user, EmergencyMedia.MediaType.VIDEO,
                result.getFileName(), result.getRelativePath());
        media.setFileSize(result.getFileSize());
        media.setFileExtension(result.getFileExtension());
        media.setMimeType(result.getMimeType());
        media.setDurationSeconds(durationSeconds);
        media.setLatitude(latitude);
        media.setLongitude(longitude);
        media.setRecordedAt(LocalDateTime.now());
        media.setUploadStatus(EmergencyMedia.UploadStatus.COMPLETED);

        media = mediaRepository.save(media);

        logger.info("Video uploaded successfully: {}", media.getFileName());

        checkAndSendFollowUpSMS(alert);

        return new MediaUploadResponseDTO(media, "Video uploaded successfully", baseUrl);
    }

    // ==================== Follow-Up SMS (STEP 3) ====================

    /**
     * Check if media upload is complete and send follow-up SMS
     */
    @Async
    protected void checkAndSendFollowUpSMS(EmergencyAlert alert) {
        try {
            // Wait a bit for other uploads to complete
            Thread.sleep(2000);

            long audioCount = mediaRepository.countByAlertAndType(alert, EmergencyMedia.MediaType.AUDIO);
            long photoCount = mediaRepository.countByAlertAndType(alert, EmergencyMedia.MediaType.PHOTO);
            long videoCount = mediaRepository.countByAlertAndType(alert, EmergencyMedia.MediaType.VIDEO);
            long totalMedia = audioCount + photoCount + videoCount;

            // Only send if we have media
            if (totalMedia > 0) {
                sendFollowUpSMS(alert, audioCount, photoCount, videoCount);
            }
        } catch (Exception e) {
            logger.error("Error sending follow-up SMS: {}", e.getMessage());
        }
    }

    /**
     * Send follow-up SMS with media evidence notification
     */
    @Async
    protected void sendFollowUpSMS(EmergencyAlert alert, long audioCount, long photoCount, long videoCount) {
        List<EmergencyContact> contacts = contactRepository.findByUserOrderByIsPrimaryDescCreatedAtAsc(alert.getUser());

        String mediaViewUrl = baseUrl + "/api/emergency/media/view-all/" + alert.getId();

        StringBuilder message = new StringBuilder();
        message.append("EMERGENCY EVIDENCE RECEIVED\n\n");
        message.append("From: ").append(alert.getUser().getFullName()).append("\n");
        message.append("Alert ID: ").append(alert.getId()).append("\n\n");
        message.append("Evidence uploaded:\n");

        if (audioCount > 0) {
            message.append(audioCount).append(" audio recording(s)\n");
        }
        if (photoCount > 0) {
            message.append(photoCount).append(" photo(s)\n");
        }
        if (videoCount > 0) {
            message.append(videoCount).append(" video(s)\n");
        }

        message.append("\nView evidence: ").append(mediaViewUrl);
        message.append("\n\nThis evidence may be critical!");

        String smsText = message.toString();

        for (EmergencyContact contact : contacts) {
            try {
                twilioSmsService.sendSms(contact.getPhoneNumber(), smsText);
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Failed to send follow-up SMS to {}: {}", contact.getContactName(), e.getMessage());
            }
        }

        logger.info("Follow-up SMS sent with media evidence notification");
    }

    // ==================== Media Retrieval ====================

    /**
     * Get all media for an alert
     */
    public MediaSummaryDTO getAlertMedia(String userEmail, Long alertId) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        // Allow access if user is owner or if shared with contacts (future feature)
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access");
        }

        List<EmergencyMedia> mediaList = mediaRepository.findByAlertOrderByUploadedAtDesc(alert);

        MediaSummaryDTO summary = new MediaSummaryDTO();
        summary.setAlertId(alertId);
        summary.setTotalMediaCount(mediaList.size());
        summary.setAudioCount((int) mediaList.stream().filter(m -> m.getMediaType() == EmergencyMedia.MediaType.AUDIO).count());
        summary.setPhotoCount((int) mediaList.stream().filter(m -> m.getMediaType() == EmergencyMedia.MediaType.PHOTO).count());
        summary.setVideoCount((int) mediaList.stream().filter(m -> m.getMediaType() == EmergencyMedia.MediaType.VIDEO).count());
        summary.setCompletedCount((int) mediaList.stream().filter(EmergencyMedia::isUploadComplete).count());
        summary.setPendingCount(summary.getTotalMediaCount() - summary.getCompletedCount());
        summary.setMessage("Media summary retrieved successfully");

        List<MediaSummaryDTO.MediaItemDTO> items = mediaList.stream().map(media -> {
            MediaSummaryDTO.MediaItemDTO item = new MediaSummaryDTO.MediaItemDTO();
            item.setId(media.getId());
            item.setType(media.getMediaType().name());
            item.setFileName(media.getFileName());
            item.setFileSize(media.getFormattedFileSize());
            item.setDurationSeconds(media.getDurationSeconds());
            item.setStatus(media.getUploadStatus().name());
            item.setViewUrl(media.getFileUrl(baseUrl));
            return item;
        }).collect(Collectors.toList());

        summary.setMedia(items);

        return summary;
    }

    /**
     * Get media file bytes for viewing/downloading
     */
    public byte[] getMediaFile(Long mediaId) {
        EmergencyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        return fileStorageService.loadFile(media.getFilePath());
    }

    /**
     * Delete media file
     */
    @Transactional
    public void deleteMedia(String userEmail, Long mediaId) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (!media.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        // Delete file from storage
        fileStorageService.deleteFile(media.getFilePath());

        // Delete database record
        mediaRepository.delete(media);

        logger.info("Media deleted: ID={}", mediaId);
    }
}
