package com.women.safety.features.fakeCalls.dto;

import com.women.safety.features.fakeCalls.model.FakeCall;
import com.women.safety.features.fakeCalls.model.FakeCallLog;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTOs for Fake Call Feature
 */
public class FakeCallDTO {

    /**
     * Create/Update Fake Call Preset
     */
    @Data
    public static class FakeCallPresetDTO {
        private Long id;

        @NotBlank(message = "Caller name is required")
        private String callerName;

        @NotBlank(message = "Caller phone is required")
        private String callerPhone;

        private String callerPhotoUrl;
        private String ringtoneName;
        private Boolean vibrateEnabled;
        private Integer autoAnswerDelaySeconds;

        @Min(value = 10, message = "Call duration must be at least 10 seconds")
        private Integer callDurationSeconds;

        private FakeCall.CallType callType;
        private Boolean isPreset;
        private String presetName;

        public FakeCallPresetDTO() {}
    }

    /**
     * Trigger Fake Call Request
     */
    @Data
    public static class TriggerFakeCallDTO {
        private Long presetId; // Use existing preset (optional)
        private String callerName;
        private String callerPhone;
        private String callerPhotoUrl;
        private FakeCall.CallType callType;
        private Integer autoAnswerDelaySeconds;
        private Integer callDurationSeconds;
        private FakeCallLog.TriggerMethod triggerMethod;

        // Location (optional)
        private Double latitude;
        private Double longitude;
        private String locationAddress;
        private Integer batteryLevel;

        public TriggerFakeCallDTO() {
            // Defaults
            this.callType = FakeCall.CallType.VOICE_CALL;
            this.autoAnswerDelaySeconds = null; // Manual answer
            this.callDurationSeconds = 120; // 2 minutes
            this.triggerMethod = FakeCallLog.TriggerMethod.IN_APP_BUTTON;
        }
    }

    /**
     * Fake Call Response
     */
    @Data
    public static class FakeCallResponseDTO {
        private Long callLogId;
        private Long presetId;
        private String callerName;
        private String callerPhone;
        private String callerPhotoUrl;
        private FakeCall.CallType callType;
        private Integer autoAnswerDelaySeconds;
        private Integer callDurationSeconds;
        private String ringtoneName;
        private Boolean vibrateEnabled;
        private String message;

        // Instructions for frontend
        private String instruction; // e.g., "Show incoming call screen"

        public FakeCallResponseDTO() {}

        public FakeCallResponseDTO(FakeCall fakeCall, Long callLogId, String message) {
            this.callLogId = callLogId;
            this.presetId = fakeCall.getId();
            this.callerName = fakeCall.getCallerName();
            this.callerPhone = fakeCall.getCallerPhone();
            this.callerPhotoUrl = fakeCall.getCallerPhotoUrl();
            this.callType = fakeCall.getCallType();
            this.autoAnswerDelaySeconds = fakeCall.getAutoAnswerDelaySeconds();
            this.callDurationSeconds = fakeCall.getCallDurationSeconds();
            this.ringtoneName = fakeCall.getRingtoneName();
            this.vibrateEnabled = fakeCall.getVibrateEnabled();
            this.message = message;
            this.instruction = "SHOW_INCOMING_CALL";
        }
    }

    /**
     * End Fake Call Request
     */
    @Data
    public static class EndFakeCallDTO {
        private Long callLogId;
        private Boolean wasAnswered;
        private Boolean wasDeclined;
        private Integer actualDurationSeconds;
        private String notes;

        public EndFakeCallDTO() {}
    }

    /**
     * Fake Call Statistics
     */
    @Data
    public static class FakeCallStatsDTO {
        private Long totalCallsTriggered;
        private Long callsThisWeek;
        private Long callsThisMonth;
        private Integer mostUsedPresetId;
        private String mostUsedPresetName;
        private Integer averageDurationSeconds;
        private String mostCommonTriggerMethod;

        public FakeCallStatsDTO() {}
    }

    /**
     * Floating Button Config
     */
    @Data
    public static class FloatingButtonConfigDTO {
        private Boolean enabled;
        private Integer xPosition; // Screen X coordinate
        private Integer yPosition; // Screen Y coordinate
        private Integer opacity; // 0-100
        private Integer size; // In DP
        private String color; // Hex color code
        private Long defaultPresetId; // Which preset to use when tapped

        public FloatingButtonConfigDTO() {
            // Defaults for invisible button
            this.enabled = true;
            this.xPosition = 50;
            this.yPosition = 50;
            this.opacity = 10; // Nearly invisible
            this.size = 50;
            this.color = "#000000";
        }
    }
}
