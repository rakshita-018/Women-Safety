package com.women.safety.features.smartAlert.service;


import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertRequestDTO;
import com.women.safety.features.emergencySOS.service.EmergencyService;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import com.women.safety.features.liveLocationTracking.repository.LocationTrackingRepository;
import com.women.safety.features.smartAlert.dto.SmartAlertAnalysisDTO;
import com.women.safety.features.smartAlert.dto.SmartAlertSettingsDTO;
import com.women.safety.features.smartAlert.dto.SuspiciousActivityDTO;
import com.women.safety.features.smartAlert.dto.VoiceCommandDTO;
import com.women.safety.features.smartAlert.model.SmartAlertSettings;
import com.women.safety.features.smartAlert.model.SuspiciousActivityLog;
import com.women.safety.features.smartAlert.model.VoiceCommand;
import com.women.safety.features.smartAlert.repository.SmartAlertSettingsRepository;
import com.women.safety.features.smartAlert.repository.SuspiciousActivityLogRepository;
import com.women.safety.features.smartAlert.repository.VoiceCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class SmartAlertService {

    private static final Logger logger = LoggerFactory.getLogger(SmartAlertService.class);

    // Thresholds for alert triggering
    private static final double HIGH_RISK_THRESHOLD = 0.7;
    private static final double CRITICAL_RISK_THRESHOLD = 0.85;
    private static final int RECENT_ACTIVITY_WINDOW_MINUTES = 5;

    private final SuspiciousActivityLogRepository activityLogRepository;
    private final VoiceCommandRepository voiceCommandRepository;
    private final SmartAlertSettingsRepository settingsRepository;
    private final AuthUserRepository authUserRepository;
    private final LocationTrackingRepository locationTrackingRepository;
    private final EmergencyService emergencyService;

    public SmartAlertService(SuspiciousActivityLogRepository activityLogRepository,
                             VoiceCommandRepository voiceCommandRepository,
                             SmartAlertSettingsRepository settingsRepository,
                             AuthUserRepository authUserRepository,
                             LocationTrackingRepository locationTrackingRepository,
                             EmergencyService emergencyService) {
        this.activityLogRepository = activityLogRepository;
        this.voiceCommandRepository = voiceCommandRepository;
        this.settingsRepository = settingsRepository;
        this.authUserRepository = authUserRepository;
        this.locationTrackingRepository = locationTrackingRepository;
        this.emergencyService = emergencyService;
    }

    // ==================== Suspicious Activity Detection ====================

    @Transactional
    public SmartAlertAnalysisDTO logSuspiciousActivity(String userEmail, SuspiciousActivityDTO activityDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get user settings
        SmartAlertSettings settings = getOrCreateSettings(user);

        // Check if this type of detection is enabled
        if (!isDetectionEnabled(settings, activityDTO.getActivityType())) {
            return new SmartAlertAnalysisDTO(false, 0.0, "DETECTION_DISABLED");
        }

        // Create activity log
        SuspiciousActivityLog activityLog = new SuspiciousActivityLog(
                user,
                activityDTO.getActivityType(),
                activityDTO.getIntensityLevel()
        );
        activityLog.setConfidenceScore(activityDTO.getConfidenceScore());
        activityLog.setLatitude(activityDTO.getLatitude());
        activityLog.setLongitude(activityDTO.getLongitude());
        activityLog.setDeviceMotionData(activityDTO.getDeviceMotionData());
        activityLog.setNotes(activityDTO.getNotes());

        activityLog = activityLogRepository.save(activityLog);

        // Analyze risk and decide if alert should be triggered
        SmartAlertAnalysisDTO analysis = analyzeRiskLevel(user, activityLog, settings);

        // Auto-trigger alert if enabled and risk is high
        if (settings.getAutoTriggerEnabled() && analysis.getShouldTriggerAlert()) {
            triggerSmartAlert(user, activityLog, analysis);
            activityLog.setAlertTriggered(true);
            activityLogRepository.save(activityLog);
        }

        logger.info("Suspicious activity logged: {} - Risk: {}",
                activityDTO.getActivityType(), analysis.getRiskLevel());

        return analysis;
    }

    @Transactional
    public SmartAlertAnalysisDTO processVoiceCommand(String userEmail, VoiceCommandDTO commandDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SmartAlertSettings settings = getOrCreateSettings(user);

        if (!settings.getVoiceActivationEnabled()) {
            return new SmartAlertAnalysisDTO(false, 0.0, "VOICE_DETECTION_DISABLED");
        }

        // Determine command type if not provided
        VoiceCommand.CommandType commandType = commandDTO.getCommandType();
        if (commandType == null) {
            commandType = detectCommandType(commandDTO.getCommandText(), settings);
        }

        // Create voice command record
        VoiceCommand voiceCommand = new VoiceCommand(user, commandDTO.getCommandText(), commandType);
        voiceCommand.setAudioUrl(commandDTO.getAudioUrl());
        voiceCommand.setConfidenceScore(commandDTO.getConfidenceScore());
        voiceCommand.setLatitude(commandDTO.getLatitude());
        voiceCommand.setLongitude(commandDTO.getLongitude());

        voiceCommand = voiceCommandRepository.save(voiceCommand);

        // Handle emergency commands
        SmartAlertAnalysisDTO analysis = new SmartAlertAnalysisDTO();

        if (commandType == VoiceCommand.CommandType.EMERGENCY_HELP) {
            analysis.setShouldTriggerAlert(true);
            analysis.setOverallRiskScore(0.95);
            analysis.setRiskLevel("CRITICAL");
            analysis.setRecommendation("Emergency help keyword detected. Alert will be triggered.");
            analysis.setRequiresImmediateAction(true);

            if (settings.getAutoTriggerEnabled()) {
                triggerVoiceActivatedAlert(user, voiceCommand);
                voiceCommand.setAlertTriggered(true);
                voiceCommandRepository.save(voiceCommand);
            }
        } else if (commandType == VoiceCommand.CommandType.CANCEL_ALERT) {
            analysis.setShouldTriggerAlert(false);
            analysis.setRiskLevel("LOW");
            analysis.setRecommendation("Alert cancellation confirmed.");
            // TODO: Cancel any pending alerts
        } else if (commandType == VoiceCommand.CommandType.SAFE_CHECK_IN) {
            analysis.setShouldTriggerAlert(false);
            analysis.setRiskLevel("LOW");
            analysis.setRecommendation("Safe check-in confirmed.");
        }

        logger.info("Voice command processed: {} - Type: {}",
                commandDTO.getCommandText(), commandType);

        return analysis;
    }

    public List<SuspiciousActivityLog> getUserActivityLog(String userEmail, Integer recentMinutes) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (recentMinutes != null && recentMinutes > 0) {
            LocalDateTime fromDate = LocalDateTime.now().minusMinutes(recentMinutes);
            return activityLogRepository.findRecentActivities(user, fromDate);
        }

        return activityLogRepository.findByUserOrderByTimestampDesc(user);
    }

    public List<VoiceCommand> getUserVoiceCommands(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return voiceCommandRepository.findByUserOrderByTimestampDesc(user);
    }

    @Transactional
    public SuspiciousActivityLog markAsFalsePositive(String userEmail, Long activityId) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SuspiciousActivityLog activity = activityLogRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!activity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        activity.setFalsePositive(true);
        return activityLogRepository.save(activity);
    }

    // ==================== Smart Alert Settings ====================

    @Transactional
    public SmartAlertSettings updateSettings(String userEmail, SmartAlertSettingsDTO settingsDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SmartAlertSettings settings = getOrCreateSettings(user);

        // Update motion detection settings
        if (settingsDTO.getShakeDetectionEnabled() != null) {
            settings.setShakeDetectionEnabled(settingsDTO.getShakeDetectionEnabled());
        }
        if (settingsDTO.getShakeSensitivity() != null) {
            settings.setShakeSensitivity(settingsDTO.getShakeSensitivity());
        }
        if (settingsDTO.getShakeDurationSeconds() != null) {
            settings.setShakeDurationSeconds(settingsDTO.getShakeDurationSeconds());
        }
        if (settingsDTO.getRunningDetectionEnabled() != null) {
            settings.setRunningDetectionEnabled(settingsDTO.getRunningDetectionEnabled());
        }
        if (settingsDTO.getRunningDurationSeconds() != null) {
            settings.setRunningDurationSeconds(settingsDTO.getRunningDurationSeconds());
        }
        if (settingsDTO.getFallDetectionEnabled() != null) {
            settings.setFallDetectionEnabled(settingsDTO.getFallDetectionEnabled());
        }
        if (settingsDTO.getImpactDetectionEnabled() != null) {
            settings.setImpactDetectionEnabled(settingsDTO.getImpactDetectionEnabled());
        }

        // Update voice detection settings
        if (settingsDTO.getVoiceActivationEnabled() != null) {
            settings.setVoiceActivationEnabled(settingsDTO.getVoiceActivationEnabled());
        }
        if (settingsDTO.getVoiceKeywords() != null) {
            settings.setVoiceKeywords(settingsDTO.getVoiceKeywords());
        }
        if (settingsDTO.getScreamDetectionEnabled() != null) {
            settings.setScreamDetectionEnabled(settingsDTO.getScreamDetectionEnabled());
        }

        // Update alert behavior
        if (settingsDTO.getAutoTriggerEnabled() != null) {
            settings.setAutoTriggerEnabled(settingsDTO.getAutoTriggerEnabled());
        }
        if (settingsDTO.getConfirmationDelaySeconds() != null) {
            settings.setConfirmationDelaySeconds(settingsDTO.getConfirmationDelaySeconds());
        }
        if (settingsDTO.getSilentMode() != null) {
            settings.setSilentMode(settingsDTO.getSilentMode());
        }

        // Update scheduling
        if (settingsDTO.getAutoEnableAtNight() != null) {
            settings.setAutoEnableAtNight(settingsDTO.getAutoEnableAtNight());
        }
        if (settingsDTO.getNightStartHour() != null) {
            settings.setNightStartHour(settingsDTO.getNightStartHour());
        }
        if (settingsDTO.getNightEndHour() != null) {
            settings.setNightEndHour(settingsDTO.getNightEndHour());
        }
        if (settingsDTO.getAutoEnableInDangerZones() != null) {
            settings.setAutoEnableInDangerZones(settingsDTO.getAutoEnableInDangerZones());
        }

        return settingsRepository.save(settings);
    }

    public SmartAlertSettings getSettings(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return getOrCreateSettings(user);
    }

    // ==================== Helper Methods ====================

    private SmartAlertSettings getOrCreateSettings(AuthUser user) {
        return settingsRepository.findByUser(user)
                .orElseGet(() -> {
                    SmartAlertSettings newSettings = new SmartAlertSettings(user);
                    return settingsRepository.save(newSettings);
                });
    }

    private boolean isDetectionEnabled(SmartAlertSettings settings, SuspiciousActivityLog.ActivityType activityType) {
        switch (activityType) {
            case PHONE_SHAKE:
                return settings.getShakeDetectionEnabled();
            case RAPID_MOVEMENT:
                return settings.getRunningDetectionEnabled();
            case FALL_DETECTED:
                return settings.getFallDetectionEnabled();
            case IMPACT_DETECTED:
                return settings.getImpactDetectionEnabled();
            case VOICE_DISTRESS:
                return settings.getVoiceActivationEnabled();
            case SCREAM_DETECTED:
                return settings.getScreamDetectionEnabled();
            default:
                return true; // Enable by default for other types
        }
    }

    private SmartAlertAnalysisDTO analyzeRiskLevel(AuthUser user, SuspiciousActivityLog currentActivity, SmartAlertSettings settings) {
        SmartAlertAnalysisDTO analysis = new SmartAlertAnalysisDTO();

        // Get recent activities for pattern analysis
        LocalDateTime recentWindow = LocalDateTime.now().minusMinutes(RECENT_ACTIVITY_WINDOW_MINUTES);
        List<SuspiciousActivityLog> recentActivities = activityLogRepository.findRecentActivities(user, recentWindow);

        analysis.setRecentActivityCount(recentActivities.size());

        // Calculate risk score based on multiple factors
        double riskScore = currentActivity.getIntensityLevel() != null ? currentActivity.getIntensityLevel() : 0.5;

        // Increase risk for multiple recent activities
        if (recentActivities.size() > 3) {
            riskScore += 0.2;
        }

        // Increase risk for high-intensity activities
        if (currentActivity.getIntensityLevel() != null && currentActivity.getIntensityLevel() > 0.8) {
            riskScore += 0.15;
        }

        // Specific activity type adjustments
        switch (currentActivity.getActivityType()) {
            case PHONE_SHAKE:
            case IMPACT_DETECTED:
            case SCREAM_DETECTED:
                riskScore += 0.25;
                break;
            case FALL_DETECTED:
            case DEVICE_THROWN:
                riskScore += 0.3;
                break;
            case VOICE_DISTRESS:
                riskScore += 0.35;
                break;
            default:
                break;
        }

        // Check time of day (night = higher risk)
        if (settings.getAutoEnableAtNight() && isNightTime(settings)) {
            riskScore += 0.1;
        }

        // Cap at 1.0
        riskScore = Math.min(riskScore, 1.0);

        analysis.setOverallRiskScore(riskScore);

        // Determine risk level and recommendation
        if (riskScore >= CRITICAL_RISK_THRESHOLD) {
            analysis.setRiskLevel("CRITICAL");
            analysis.setShouldTriggerAlert(true);
            analysis.setRequiresImmediateAction(true);
            analysis.setRecommendation("Extremely high risk detected. Immediate alert recommended.");
        } else if (riskScore >= HIGH_RISK_THRESHOLD) {
            analysis.setRiskLevel("HIGH");
            analysis.setShouldTriggerAlert(true);
            analysis.setRequiresImmediateAction(false);
            analysis.setRecommendation("High risk detected. Alert recommended with confirmation delay.");
        } else if (riskScore >= 0.4) {
            analysis.setRiskLevel("MEDIUM");
            analysis.setShouldTriggerAlert(false);
            analysis.setRecommendation("Moderate risk. Monitor situation closely.");
        } else {
            analysis.setRiskLevel("LOW");
            analysis.setShouldTriggerAlert(false);
            analysis.setRecommendation("Low risk. Continue monitoring.");
        }

        // Add detected activities to response
        Map<String, Object> detectedActivities = new HashMap<>();
        detectedActivities.put("currentActivity", currentActivity.getActivityType());
        detectedActivities.put("recentCount", recentActivities.size());
        detectedActivities.put("intensity", currentActivity.getIntensityLevel());
        analysis.setDetectedActivities(detectedActivities);

        return analysis;
    }

    private VoiceCommand.CommandType detectCommandType(String commandText, SmartAlertSettings settings) {
        String lowerCommand = commandText.toLowerCase().trim();

        // Check emergency keywords
        String[] keywords = settings.getVoiceKeywords().toLowerCase().split(",");
        for (String keyword : keywords) {
            if (lowerCommand.contains(keyword.trim())) {
                return VoiceCommand.CommandType.EMERGENCY_HELP;
            }
        }

        // Check cancel keywords
        if (lowerCommand.contains("cancel") || lowerCommand.contains("false alarm") ||
                lowerCommand.contains("stop") || lowerCommand.contains("mistake")) {
            return VoiceCommand.CommandType.CANCEL_ALERT;
        }

        // Check safe check-in keywords
        if (lowerCommand.contains("safe") || lowerCommand.contains("okay") ||
                lowerCommand.contains("all good") || lowerCommand.contains("fine")) {
            return VoiceCommand.CommandType.SAFE_CHECK_IN;
        }

        // Check confirmation keywords
        if (lowerCommand.contains("yes") || lowerCommand.contains("confirm") ||
                lowerCommand.contains("send")) {
            return VoiceCommand.CommandType.CONFIRM_ALERT;
        }

        return VoiceCommand.CommandType.CUSTOM;
    }

    private boolean isNightTime(SmartAlertSettings settings) {
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();

        int nightStart = settings.getNightStartHour() != null ? settings.getNightStartHour() : 22;
        int nightEnd = settings.getNightEndHour() != null ? settings.getNightEndHour() : 6;

        if (nightStart > nightEnd) {
            // Night spans midnight (e.g., 22:00 to 06:00)
            return currentHour >= nightStart || currentHour < nightEnd;
        } else {
            return currentHour >= nightStart && currentHour < nightEnd;
        }
    }

    @Async
    protected void triggerSmartAlert(AuthUser user, SuspiciousActivityLog activity, SmartAlertAnalysisDTO analysis) {
        try {
            // Get latest location
            Optional<LocationTracking> latestLocation = locationTrackingRepository.findLatestLocation(user);

            String alertMessage = String.format("🤖 SMART ALERT TRIGGERED\n\n" +
                            "Activity Detected: %s\n" +
                            "Risk Level: %s (%.0f%%)\n" +
                            "Intensity: %.0f%%\n\n" +
                            "This alert was automatically triggered by suspicious activity detection.",
                    activity.getActivityType(),
                    analysis.getRiskLevel(),
                    analysis.getOverallRiskScore() * 100,
                    (activity.getIntensityLevel() != null ? activity.getIntensityLevel() * 100 : 0));

            EmergencyAlertRequestDTO alertRequest = new EmergencyAlertRequestDTO();
            alertRequest.setAlertMessage(alertMessage);

            if (latestLocation.isPresent()) {
                alertRequest.setLatitude(latestLocation.get().getLatitude());
                alertRequest.setLongitude(latestLocation.get().getLongitude());
                alertRequest.setLocationAddress(latestLocation.get().getAddress());
            } else if (activity.getLatitude() != null && activity.getLongitude() != null) {
                alertRequest.setLatitude(activity.getLatitude());
                alertRequest.setLongitude(activity.getLongitude());
            }

            emergencyService.triggerEmergencyAlert(user.getEmail(), alertRequest);

            logger.info("Smart alert triggered for user: {} - Activity: {}",
                    user.getEmail(), activity.getActivityType());
        } catch (Exception e) {
            logger.error("Error triggering smart alert: {}", e.getMessage());
        }
    }

    @Async
    protected void triggerVoiceActivatedAlert(AuthUser user, VoiceCommand voiceCommand) {
        try {
            Optional<LocationTracking> latestLocation = locationTrackingRepository.findLatestLocation(user);

            String alertMessage = String.format("🎤 VOICE-ACTIVATED EMERGENCY ALERT\n\n" +
                            "Voice Command: \"%s\"\n" +
                            "Time: %s\n\n" +
                            "This alert was triggered by voice command detection.",
                    voiceCommand.getCommandText(),
                    voiceCommand.getTimestamp());

            EmergencyAlertRequestDTO alertRequest = new EmergencyAlertRequestDTO();
            alertRequest.setAlertMessage(alertMessage);

            if (latestLocation.isPresent()) {
                alertRequest.setLatitude(latestLocation.get().getLatitude());
                alertRequest.setLongitude(latestLocation.get().getLongitude());
                alertRequest.setLocationAddress(latestLocation.get().getAddress());
            } else if (voiceCommand.getLatitude() != null && voiceCommand.getLongitude() != null) {
                alertRequest.setLatitude(voiceCommand.getLatitude());
                alertRequest.setLongitude(voiceCommand.getLongitude());
            }

            emergencyService.triggerEmergencyAlert(user.getEmail(), alertRequest);

            logger.info("Voice-activated alert triggered for user: {} - Command: {}",
                    user.getEmail(), voiceCommand.getCommandText());
        } catch (Exception e) {
            logger.error("Error triggering voice-activated alert: {}", e.getMessage());
        }
    }
}
