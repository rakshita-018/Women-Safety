package com.women.safety.features.fakeCalls.service;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.fakeCalls.dto.FakeCallDTO;
import com.women.safety.features.fakeCalls.model.FakeCall;
import com.women.safety.features.fakeCalls.model.FakeCallLog;
import com.women.safety.features.fakeCalls.repository.FakeCallLogRepository;
import com.women.safety.features.fakeCalls.repository.FakeCallRepository;
import com.women.safety.features.liveLocationTracking.service.OpenStreetMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Fake Call Service
 *
 * Manages fake call presets and triggering for user safety
 */
@Service
public class FakeCallService {

    private static final Logger logger = LoggerFactory.getLogger(FakeCallService.class);
    private static final int MAX_PRESETS_PER_USER = 10;

    private final FakeCallRepository fakeCallRepository;
    private final FakeCallLogRepository callLogRepository;
    private final AuthUserRepository userRepository;
    private final OpenStreetMapService openStreetMapService;


    public FakeCallService(FakeCallRepository fakeCallRepository,
                           FakeCallLogRepository callLogRepository,
                           AuthUserRepository userRepository,
                           OpenStreetMapService openStreetMapService) {
        this.fakeCallRepository = fakeCallRepository;
        this.callLogRepository = callLogRepository;
        this.userRepository = userRepository;
        this.openStreetMapService = openStreetMapService;
    }

    // ==================== Preset Management ====================

    /**
     * Create a new fake call preset
     */
    @Transactional
    public FakeCall createPreset(String userEmail, FakeCallDTO.FakeCallPresetDTO presetDTO) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check preset limit
        long presetCount = fakeCallRepository.countPresetsForUser(user);
        if (presetCount >= MAX_PRESETS_PER_USER) {
            throw new IllegalArgumentException("Maximum preset limit reached (" + MAX_PRESETS_PER_USER + ")");
        }

        FakeCall fakeCall = new FakeCall(user, presetDTO.getCallerName(), presetDTO.getCallerPhone());
        fakeCall.setCallerPhotoUrl(presetDTO.getCallerPhotoUrl());
        fakeCall.setRingtoneName(presetDTO.getRingtoneName());
        fakeCall.setVibrateEnabled(presetDTO.getVibrateEnabled() != null ? presetDTO.getVibrateEnabled() : true);
        fakeCall.setAutoAnswerDelaySeconds(presetDTO.getAutoAnswerDelaySeconds());
        fakeCall.setCallDurationSeconds(presetDTO.getCallDurationSeconds() != null ?
                presetDTO.getCallDurationSeconds() : 120);
        fakeCall.setCallType(presetDTO.getCallType() != null ?
                presetDTO.getCallType() : FakeCall.CallType.VOICE_CALL);
        fakeCall.setIsPreset(true);
        fakeCall.setPresetName(presetDTO.getPresetName());

        fakeCall = fakeCallRepository.save(fakeCall);

        logger.info("Fake call preset created: {} for user: {}", presetDTO.getPresetName(), userEmail);

        return fakeCall;
    }

    /**
     * Update existing preset
     */
    @Transactional
    public FakeCall updatePreset(String userEmail, Long presetId, FakeCallDTO.FakeCallPresetDTO presetDTO) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCall fakeCall = fakeCallRepository.findById(presetId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        if (!fakeCall.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        // Update fields
        if (presetDTO.getCallerName() != null) {
            fakeCall.setCallerName(presetDTO.getCallerName());
        }
        if (presetDTO.getCallerPhone() != null) {
            fakeCall.setCallerPhone(presetDTO.getCallerPhone());
        }
        if (presetDTO.getCallerPhotoUrl() != null) {
            fakeCall.setCallerPhotoUrl(presetDTO.getCallerPhotoUrl());
        }
        if (presetDTO.getRingtoneName() != null) {
            fakeCall.setRingtoneName(presetDTO.getRingtoneName());
        }
        if (presetDTO.getVibrateEnabled() != null) {
            fakeCall.setVibrateEnabled(presetDTO.getVibrateEnabled());
        }
        if (presetDTO.getAutoAnswerDelaySeconds() != null) {
            fakeCall.setAutoAnswerDelaySeconds(presetDTO.getAutoAnswerDelaySeconds());
        }
        if (presetDTO.getCallDurationSeconds() != null) {
            fakeCall.setCallDurationSeconds(presetDTO.getCallDurationSeconds());
        }
        if (presetDTO.getCallType() != null) {
            fakeCall.setCallType(presetDTO.getCallType());
        }
        if (presetDTO.getPresetName() != null) {
            fakeCall.setPresetName(presetDTO.getPresetName());
        }

        fakeCall = fakeCallRepository.save(fakeCall);

        logger.info("Fake call preset updated: {} for user: {}", presetId, userEmail);

        return fakeCall;
    }

    /**
     * Delete preset
     */
    @Transactional
    public void deletePreset(String userEmail, Long presetId) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCall fakeCall = fakeCallRepository.findById(presetId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        if (!fakeCall.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        callLogRepository.deleteByFakeCall(fakeCall);
        fakeCallRepository.delete(fakeCall);



        logger.info("Fake call preset deleted: {} for user: {}", presetId, userEmail);
    }

    /**
     * Get all presets for user
     */
    public List<FakeCall> getUserPresets(String userEmail) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return fakeCallRepository.findByUserAndIsPresetTrueOrderByTriggerCountDesc(user);
    }

    /**
     * Get single preset
     */
    public FakeCall getPreset(String userEmail, Long presetId) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCall fakeCall = fakeCallRepository.findById(presetId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        if (!fakeCall.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        return fakeCall;
    }

    // ==================== Trigger Fake Call ====================

    /**
     * Trigger a fake call (main method)
     */
    @Transactional
    public FakeCallDTO.FakeCallResponseDTO triggerFakeCall(String userEmail,
                                                           FakeCallDTO.TriggerFakeCallDTO triggerDTO) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCall fakeCall;

        // Use preset or create one-time fake call
        if (triggerDTO.getPresetId() != null) {
            fakeCall = fakeCallRepository.findById(triggerDTO.getPresetId())
                    .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

            if (!fakeCall.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized");
            }

            // Record usage
            fakeCall.recordTrigger();
            fakeCallRepository.save(fakeCall);

        } else {
            // Create temporary fake call (not saved as preset)
            fakeCall = new FakeCall(user,
                    triggerDTO.getCallerName() != null ? triggerDTO.getCallerName() : "Unknown Caller",
                    triggerDTO.getCallerPhone() != null ? triggerDTO.getCallerPhone() : "+1234567890");
            fakeCall.setCallerPhotoUrl(triggerDTO.getCallerPhotoUrl());
            fakeCall.setCallType(triggerDTO.getCallType());
            fakeCall.setAutoAnswerDelaySeconds(triggerDTO.getAutoAnswerDelaySeconds());
            fakeCall.setCallDurationSeconds(triggerDTO.getCallDurationSeconds());
            fakeCall.setIsPreset(false);
            fakeCall = fakeCallRepository.save(fakeCall);
        }

        // Create call log
        FakeCallLog callLog = new FakeCallLog(user, fakeCall,
                triggerDTO.getTriggerMethod() != null ?
                        triggerDTO.getTriggerMethod() : FakeCallLog.TriggerMethod.IN_APP_BUTTON);
        callLog.setLatitude(triggerDTO.getLatitude());
        callLog.setLongitude(triggerDTO.getLongitude());
        callLog.setLocationAddress(triggerDTO.getLocationAddress());
        callLog.setBatteryLevel(triggerDTO.getBatteryLevel());

        // Resolve location if provided
        if (triggerDTO.getLatitude() != null && triggerDTO.getLongitude() != null &&
                (triggerDTO.getLocationAddress() == null || triggerDTO.getLocationAddress().trim().isEmpty())) {
            try {
                OpenStreetMapService.GeocodeResult geocode = openStreetMapService.getAddressFromCoordinates(
                        triggerDTO.getLatitude(), triggerDTO.getLongitude());
                if (geocode != null) {
                    callLog.setLocationAddress(geocode.getFormattedAddress());
                }
            } catch (Exception e) {
                logger.warn("Failed to geocode location for fake call: {}", e.getMessage());
            }
        }

        callLog = callLogRepository.save(callLog);

        logger.info("Fake call triggered for user: {} via {}",
                userEmail, triggerDTO.getTriggerMethod());

        // Return response with call details
        FakeCallDTO.FakeCallResponseDTO response = new FakeCallDTO.FakeCallResponseDTO(
                fakeCall, callLog.getId(), "Fake call initiated successfully");

        return response;
    }

    /**
     * Quick trigger using most recent preset
     */
    @Transactional
    public FakeCallDTO.FakeCallResponseDTO quickTrigger(String userEmail,
                                                        FakeCallLog.TriggerMethod triggerMethod) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Find most recently used preset
        Optional<FakeCall> recentPreset = fakeCallRepository.findMostRecentlyUsedPreset(user);

        if (recentPreset.isEmpty()) {
            // No presets exist, use default
            FakeCallDTO.TriggerFakeCallDTO defaultTrigger = new FakeCallDTO.TriggerFakeCallDTO();
            defaultTrigger.setCallerName("Mom");
            defaultTrigger.setCallerPhone("+1234567890");
            defaultTrigger.setTriggerMethod(triggerMethod);
            return triggerFakeCall(userEmail, defaultTrigger);
        }

        FakeCallDTO.TriggerFakeCallDTO triggerDTO = new FakeCallDTO.TriggerFakeCallDTO();
        triggerDTO.setPresetId(recentPreset.get().getId());
        triggerDTO.setTriggerMethod(triggerMethod);

        return triggerFakeCall(userEmail, triggerDTO);
    }

    /**
     * End fake call
     */
    @Transactional
    public FakeCallLog endFakeCall(String userEmail, FakeCallDTO.EndFakeCallDTO endDTO) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCallLog callLog = callLogRepository.findById(endDTO.getCallLogId())
                .orElseThrow(() -> new IllegalArgumentException("Call log not found"));

        if (!callLog.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        callLog.endCall();
        callLog.setWasAnswered(endDTO.getWasAnswered() != null ? endDTO.getWasAnswered() : false);
        callLog.setWasDeclined(endDTO.getWasDeclined() != null ? endDTO.getWasDeclined() : false);
        callLog.setNotes(endDTO.getNotes());

        // Override duration if provided
        if (endDTO.getActualDurationSeconds() != null) {
            callLog.setDurationSeconds(endDTO.getActualDurationSeconds());
        }

        callLog = callLogRepository.save(callLog);

        logger.info("Fake call ended: {} (duration: {}s)", callLog.getId(), callLog.getDurationSeconds());

        return callLog;
    }

    // ==================== History & Statistics ====================

    /**
     * Get call history
     */
    public List<FakeCallLog> getCallHistory(String userEmail, Integer recentDays) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (recentDays != null && recentDays > 0) {
            LocalDateTime fromDate = LocalDateTime.now().minusDays(recentDays);
            return callLogRepository.findRecentLogs(user, fromDate);
        }

        return callLogRepository.findByUserOrderByCallStartedAtDesc(user);
    }

    /**
     * Get statistics
     */
    public FakeCallDTO.FakeCallStatsDTO getStatistics(String userEmail) {
        AuthUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FakeCallDTO.FakeCallStatsDTO stats = new FakeCallDTO.FakeCallStatsDTO();

        stats.setTotalCallsTriggered(callLogRepository.countByUser(user));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        stats.setCallsThisWeek(callLogRepository.countRecentCalls(user, oneWeekAgo));

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        stats.setCallsThisMonth(callLogRepository.countRecentCalls(user, oneMonthAgo));

        // Most used preset
        Optional<FakeCall> mostUsed = fakeCallRepository.findMostUsedPreset(user);
        if (mostUsed.isPresent()) {
            stats.setMostUsedPresetId(mostUsed.get().getId().intValue());
            stats.setMostUsedPresetName(mostUsed.get().getPresetName());
        }

        // Calculate average duration
        List<FakeCallLog> allLogs = callLogRepository.findByUserOrderByCallStartedAtDesc(user);
        if (!allLogs.isEmpty()) {
            int totalDuration = allLogs.stream()
                    .filter(log -> log.getDurationSeconds() != null)
                    .mapToInt(FakeCallLog::getDurationSeconds)
                    .sum();
            stats.setAverageDurationSeconds(totalDuration / allLogs.size());
        }

        return stats;
    }
}
