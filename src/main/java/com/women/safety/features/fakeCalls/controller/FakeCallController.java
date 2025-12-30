package com.women.safety.features.fakeCalls.controller;


import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.fakeCalls.dto.FakeCallDTO;
import com.women.safety.features.fakeCalls.model.FakeCall;
import com.women.safety.features.fakeCalls.model.FakeCallLog;
import com.women.safety.features.fakeCalls.service.FakeCallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fake-call")
public class FakeCallController {

    private final FakeCallService fakeCallService;

    public FakeCallController(FakeCallService fakeCallService) {
        this.fakeCallService = fakeCallService;
    }

    // ==================== Preset Management ====================


    @PostMapping("/presets")
    public ResponseEntity<Map<String, Object>> createPreset(
            @Valid @RequestBody FakeCallDTO.FakeCallPresetDTO presetDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCall preset = fakeCallService.createPreset(userDetails.getUsername(), presetDTO);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fake call preset created successfully",
                "preset", preset
        ));
    }

    /**
     * Get all presets for user
     * GET /api/fake-call/presets
     */
    @GetMapping("/presets")
    public ResponseEntity<List<FakeCall>> getPresets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FakeCall> presets = fakeCallService.getUserPresets(userDetails.getUsername());

        return ResponseEntity.ok(presets);
    }

    /**
     * Get single preset
     * GET /api/fake-call/presets/{presetId}
     */
    @GetMapping("/presets/{presetId}")
    public ResponseEntity<FakeCall> getPreset(
            @PathVariable Long presetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCall preset = fakeCallService.getPreset(userDetails.getUsername(), presetId);

        return ResponseEntity.ok(preset);
    }

    /**
     * Update preset
     * PUT /api/fake-call/presets/{presetId}
     */
    @PutMapping("/presets/{presetId}")
    public ResponseEntity<Map<String, Object>> updatePreset(
            @PathVariable Long presetId,
            @Valid @RequestBody FakeCallDTO.FakeCallPresetDTO presetDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCall preset = fakeCallService.updatePreset(userDetails.getUsername(), presetId, presetDTO);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Preset updated successfully",
                "preset", preset
        ));
    }

    /**
     * Delete preset
     * DELETE /api/fake-call/presets/{presetId}
     */
    @DeleteMapping("/presets/{presetId}")
    public ResponseEntity<Map<String, String>> deletePreset(
            @PathVariable Long presetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        fakeCallService.deletePreset(userDetails.getUsername(), presetId);

        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Preset deleted successfully"
        ));
    }

    // ==================== Trigger Fake Call ====================

    /**
     * Trigger fake call (main endpoint)
     * POST /api/fake-call/trigger
     *
     * This is called by:
     * - In-app button
     * - Floating button
     * - Widget
     * - Scheduled trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<FakeCallDTO.FakeCallResponseDTO> triggerFakeCall(
            @Valid @RequestBody FakeCallDTO.TriggerFakeCallDTO triggerDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCallDTO.FakeCallResponseDTO response = fakeCallService.triggerFakeCall(
                userDetails.getUsername(), triggerDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Quick trigger (uses most recent preset)
     * POST /api/fake-call/quick-trigger
     *
     * Ultra-fast trigger for emergency situations
     */
    @PostMapping("/quick-trigger")
    public ResponseEntity<FakeCallDTO.FakeCallResponseDTO> quickTrigger(
            @RequestParam(required = false, defaultValue = "IN_APP_BUTTON") String triggerMethod,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCallLog.TriggerMethod method;
        try {
            method = FakeCallLog.TriggerMethod.valueOf(triggerMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            method = FakeCallLog.TriggerMethod.IN_APP_BUTTON;
        }

        FakeCallDTO.FakeCallResponseDTO response = fakeCallService.quickTrigger(
                userDetails.getUsername(), method);

        return ResponseEntity.ok(response);
    }

    /**
     * Trigger from floating button
     * POST /api/fake-call/trigger/floating-button
     */
    @PostMapping("/trigger/floating-button")
    public ResponseEntity<FakeCallDTO.FakeCallResponseDTO> triggerFromFloatingButton(
            @RequestBody(required = false) FakeCallDTO.TriggerFakeCallDTO triggerDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (triggerDTO == null) {
            triggerDTO = new FakeCallDTO.TriggerFakeCallDTO();
        }
        triggerDTO.setTriggerMethod(FakeCallLog.TriggerMethod.FLOATING_BUTTON);

        FakeCallDTO.FakeCallResponseDTO response = fakeCallService.triggerFakeCall(
                userDetails.getUsername(), triggerDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * End fake call
     * POST /api/fake-call/end
     */
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endFakeCall(
            @Valid @RequestBody FakeCallDTO.EndFakeCallDTO endDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCallLog callLog = fakeCallService.endFakeCall(userDetails.getUsername(), endDTO);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fake call ended",
                "callLog", callLog
        ));
    }

    // ==================== History & Statistics ====================

    /**
     * Get call history
     * GET /api/fake-call/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<FakeCallLog>> getCallHistory(
            @RequestParam(required = false) Integer recentDays,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FakeCallLog> history = fakeCallService.getCallHistory(
                userDetails.getUsername(), recentDays);

        return ResponseEntity.ok(history);
    }

    /**
     * Get statistics
     * GET /api/fake-call/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<FakeCallDTO.FakeCallStatsDTO> getStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FakeCallDTO.FakeCallStatsDTO stats = fakeCallService.getStatistics(userDetails.getUsername());

        return ResponseEntity.ok(stats);
    }

    // ==================== Utility Endpoints ====================

    /**
     * Test fake call system
     * GET /api/fake-call/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSystem() {
        return ResponseEntity.ok(Map.of(
                "status", "Fake call system is operational",
                "features", List.of(
                        "Preset management",
                        "Quick trigger",
                        "Floating button trigger",
                        "Call history tracking",
                        "Statistics"
                ),
                "maxPresetsPerUser", 10,
                "defaultCallDuration", "120 seconds"
        ));
    }

    /**
     * Get default presets (suggested configurations)
     * GET /api/fake-call/default-presets
     */
    @GetMapping("/default-presets")
    public ResponseEntity<List<Map<String, Object>>> getDefaultPresets() {
        List<Map<String, Object>> defaults = List.of(
                Map.of(
                        "name", "Mom - Emergency",
                        "callerName", "Mom",
                        "callerPhone", "+1234567890",
                        "callType", "VOICE_CALL",
                        "autoAnswerDelay", null,
                        "duration", 180
                ),
                Map.of(
                        "name", "Boss - Work Call",
                        "callerName", "Boss",
                        "callerPhone", "+1987654321",
                        "callType", "VOICE_CALL",
                        "autoAnswerDelay", 5,
                        "duration", 300
                ),
                Map.of(
                        "name", "Best Friend",
                        "callerName", "Sarah",
                        "callerPhone", "+1555123456",
                        "callType", "VOICE_CALL",
                        "autoAnswerDelay", null,
                        "duration", 120
                ),
                Map.of(
                        "name", "Video Call - Dad",
                        "callerName", "Dad",
                        "callerPhone", "+1444567890",
                        "callType", "VIDEO_CALL",
                        "autoAnswerDelay", null,
                        "duration", 240
                )
        );

        return ResponseEntity.ok(defaults);
    }
}
