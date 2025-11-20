package com.women.safety.features.smartAlert.controller;

import com.women.safety.features.authentication.security.CustomUserDetails;

import com.women.safety.features.smartAlert.dto.SmartAlertAnalysisDTO;
import com.women.safety.features.smartAlert.dto.SmartAlertSettingsDTO;
import com.women.safety.features.smartAlert.dto.SuspiciousActivityDTO;
import com.women.safety.features.smartAlert.dto.VoiceCommandDTO;
import com.women.safety.features.smartAlert.model.SmartAlertSettings;
import com.women.safety.features.smartAlert.model.SuspiciousActivityLog;
import com.women.safety.features.smartAlert.model.VoiceCommand;
import com.women.safety.features.smartAlert.service.SmartAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart-alert")
public class SmartAlertController {

    private final SmartAlertService smartAlertService;

    public SmartAlertController(SmartAlertService smartAlertService) {
        this.smartAlertService = smartAlertService;
    }

    // ==================== Suspicious Activity Detection ====================

    @PostMapping("/activity")
    public ResponseEntity<Map<String, Object>> logSuspiciousActivity(
            @Valid @RequestBody SuspiciousActivityDTO activityDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SmartAlertAnalysisDTO analysis = smartAlertService.logSuspiciousActivity(
                userDetails.getUsername(), activityDTO);

        String message = analysis.getShouldTriggerAlert() ?
                "Suspicious activity detected. Alert may be triggered." :
                "Activity logged for monitoring.";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "analysis", analysis
        ));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<SuspiciousActivityLog>> getUserActivityLog(
            @RequestParam(required = false) Integer recentMinutes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<SuspiciousActivityLog> activities = smartAlertService.getUserActivityLog(
                userDetails.getUsername(), recentMinutes);

        return ResponseEntity.ok(activities);
    }

    @PutMapping("/activity/{activityId}/false-positive")
    public ResponseEntity<Map<String, Object>> markAsFalsePositive(
            @PathVariable Long activityId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SuspiciousActivityLog activity = smartAlertService.markAsFalsePositive(
                userDetails.getUsername(), activityId);

        return ResponseEntity.ok(Map.of(
                "message", "Activity marked as false positive",
                "activity", activity
        ));
    }

    // ==================== Voice Commands ====================

    @PostMapping("/voice-command")
    public ResponseEntity<Map<String, Object>> processVoiceCommand(
            @Valid @RequestBody VoiceCommandDTO commandDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SmartAlertAnalysisDTO analysis = smartAlertService.processVoiceCommand(
                userDetails.getUsername(), commandDTO);

        String message;
        if (analysis.getShouldTriggerAlert()) {
            message = "Emergency voice command detected! Alert triggered.";
        } else if (analysis.getRiskLevel().equals("LOW")) {
            message = "Voice command processed successfully.";
        } else {
            message = "Voice command logged for analysis.";
        }

        return ResponseEntity.ok(Map.of(
                "message", message,
                "analysis", analysis
        ));
    }

    @GetMapping("/voice-command")
    public ResponseEntity<List<VoiceCommand>> getUserVoiceCommands(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<VoiceCommand> commands = smartAlertService.getUserVoiceCommands(userDetails.getUsername());

        return ResponseEntity.ok(commands);
    }

    // ==================== Settings ====================

    @GetMapping("/settings")
    public ResponseEntity<SmartAlertSettings> getSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SmartAlertSettings settings = smartAlertService.getSettings(userDetails.getUsername());

        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(
            @Valid @RequestBody SmartAlertSettingsDTO settingsDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SmartAlertSettings settings = smartAlertService.updateSettings(
                userDetails.getUsername(), settingsDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Smart alert settings updated successfully",
                "settings", settings
        ));
    }

    // ==================== Quick Actions ====================

    @PostMapping("/quick/shake")
    public ResponseEntity<Map<String, Object>> reportPhoneShake(
            @RequestParam Double intensity,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SuspiciousActivityDTO activityDTO = new SuspiciousActivityDTO(
                SuspiciousActivityLog.ActivityType.PHONE_SHAKE, intensity);
        activityDTO.setLatitude(latitude);
        activityDTO.setLongitude(longitude);

        SmartAlertAnalysisDTO analysis = smartAlertService.logSuspiciousActivity(
                userDetails.getUsername(), activityDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Phone shake detected and analyzed",
                "analysis", analysis
        ));
    }

    @PostMapping("/quick/running")
    public ResponseEntity<Map<String, Object>> reportRapidMovement(
            @RequestParam Double speed,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        double intensity = Math.min(speed / 10.0, 1.0); // Normalize speed to 0-1

        SuspiciousActivityDTO activityDTO = new SuspiciousActivityDTO(
                SuspiciousActivityLog.ActivityType.RAPID_MOVEMENT, intensity);
        activityDTO.setLatitude(latitude);
        activityDTO.setLongitude(longitude);

        SmartAlertAnalysisDTO analysis = smartAlertService.logSuspiciousActivity(
                userDetails.getUsername(), activityDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Rapid movement detected and analyzed",
                "analysis", analysis
        ));
    }

    @PostMapping("/quick/fall")
    public ResponseEntity<Map<String, Object>> reportFall(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SuspiciousActivityDTO activityDTO = new SuspiciousActivityDTO(
                SuspiciousActivityLog.ActivityType.FALL_DETECTED, 0.9);
        activityDTO.setLatitude(latitude);
        activityDTO.setLongitude(longitude);

        SmartAlertAnalysisDTO analysis = smartAlertService.logSuspiciousActivity(
                userDetails.getUsername(), activityDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Fall detected! Analyzing situation...",
                "analysis", analysis
        ));
    }

    @PostMapping("/quick/voice-help")
    public ResponseEntity<Map<String, Object>> voiceHelp(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        VoiceCommandDTO commandDTO = new VoiceCommandDTO("Help!", VoiceCommand.CommandType.EMERGENCY_HELP);
        commandDTO.setLatitude(latitude);
        commandDTO.setLongitude(longitude);

        SmartAlertAnalysisDTO analysis = smartAlertService.processVoiceCommand(
                userDetails.getUsername(), commandDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Emergency help command received! Triggering alert...",
                "analysis", analysis
        ));
    }
}