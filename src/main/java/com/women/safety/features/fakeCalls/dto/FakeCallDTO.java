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

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public @NotBlank(message = "Caller name is required") String getCallerName() {
            return callerName;
        }

        public void setCallerName(@NotBlank(message = "Caller name is required") String callerName) {
            this.callerName = callerName;
        }

        public @NotBlank(message = "Caller phone is required") String getCallerPhone() {
            return callerPhone;
        }

        public void setCallerPhone(@NotBlank(message = "Caller phone is required") String callerPhone) {
            this.callerPhone = callerPhone;
        }

        public String getCallerPhotoUrl() {
            return callerPhotoUrl;
        }

        public void setCallerPhotoUrl(String callerPhotoUrl) {
            this.callerPhotoUrl = callerPhotoUrl;
        }

        public String getRingtoneName() {
            return ringtoneName;
        }

        public void setRingtoneName(String ringtoneName) {
            this.ringtoneName = ringtoneName;
        }

        public Boolean getVibrateEnabled() {
            return vibrateEnabled;
        }

        public void setVibrateEnabled(Boolean vibrateEnabled) {
            this.vibrateEnabled = vibrateEnabled;
        }

        public Integer getAutoAnswerDelaySeconds() {
            return autoAnswerDelaySeconds;
        }

        public void setAutoAnswerDelaySeconds(Integer autoAnswerDelaySeconds) {
            this.autoAnswerDelaySeconds = autoAnswerDelaySeconds;
        }

        public @Min(value = 10, message = "Call duration must be at least 10 seconds") Integer getCallDurationSeconds() {
            return callDurationSeconds;
        }

        public void setCallDurationSeconds(@Min(value = 10, message = "Call duration must be at least 10 seconds") Integer callDurationSeconds) {
            this.callDurationSeconds = callDurationSeconds;
        }

        public FakeCall.CallType getCallType() {
            return callType;
        }

        public void setCallType(FakeCall.CallType callType) {
            this.callType = callType;
        }

        public Boolean getPreset() {
            return isPreset;
        }

        public void setPreset(Boolean preset) {
            isPreset = preset;
        }

        public String getPresetName() {
            return presetName;
        }

        public void setPresetName(String presetName) {
            this.presetName = presetName;
        }
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

        public Long getPresetId() {
            return presetId;
        }

        public void setPresetId(Long presetId) {
            this.presetId = presetId;
        }

        public String getCallerName() {
            return callerName;
        }

        public void setCallerName(String callerName) {
            this.callerName = callerName;
        }

        public String getCallerPhone() {
            return callerPhone;
        }

        public void setCallerPhone(String callerPhone) {
            this.callerPhone = callerPhone;
        }

        public String getCallerPhotoUrl() {
            return callerPhotoUrl;
        }

        public void setCallerPhotoUrl(String callerPhotoUrl) {
            this.callerPhotoUrl = callerPhotoUrl;
        }

        public FakeCall.CallType getCallType() {
            return callType;
        }

        public void setCallType(FakeCall.CallType callType) {
            this.callType = callType;
        }

        public Integer getAutoAnswerDelaySeconds() {
            return autoAnswerDelaySeconds;
        }

        public void setAutoAnswerDelaySeconds(Integer autoAnswerDelaySeconds) {
            this.autoAnswerDelaySeconds = autoAnswerDelaySeconds;
        }

        public Integer getCallDurationSeconds() {
            return callDurationSeconds;
        }

        public void setCallDurationSeconds(Integer callDurationSeconds) {
            this.callDurationSeconds = callDurationSeconds;
        }

        public FakeCallLog.TriggerMethod getTriggerMethod() {
            return triggerMethod;
        }

        public void setTriggerMethod(FakeCallLog.TriggerMethod triggerMethod) {
            this.triggerMethod = triggerMethod;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getLocationAddress() {
            return locationAddress;
        }

        public void setLocationAddress(String locationAddress) {
            this.locationAddress = locationAddress;
        }

        public Integer getBatteryLevel() {
            return batteryLevel;
        }

        public void setBatteryLevel(Integer batteryLevel) {
            this.batteryLevel = batteryLevel;
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

        public Long getCallLogId() {
            return callLogId;
        }

        public void setCallLogId(Long callLogId) {
            this.callLogId = callLogId;
        }

        public Long getPresetId() {
            return presetId;
        }

        public void setPresetId(Long presetId) {
            this.presetId = presetId;
        }

        public String getCallerName() {
            return callerName;
        }

        public void setCallerName(String callerName) {
            this.callerName = callerName;
        }

        public String getCallerPhone() {
            return callerPhone;
        }

        public void setCallerPhone(String callerPhone) {
            this.callerPhone = callerPhone;
        }

        public String getCallerPhotoUrl() {
            return callerPhotoUrl;
        }

        public void setCallerPhotoUrl(String callerPhotoUrl) {
            this.callerPhotoUrl = callerPhotoUrl;
        }

        public Integer getAutoAnswerDelaySeconds() {
            return autoAnswerDelaySeconds;
        }

        public void setAutoAnswerDelaySeconds(Integer autoAnswerDelaySeconds) {
            this.autoAnswerDelaySeconds = autoAnswerDelaySeconds;
        }

        public FakeCall.CallType getCallType() {
            return callType;
        }

        public void setCallType(FakeCall.CallType callType) {
            this.callType = callType;
        }

        public Integer getCallDurationSeconds() {
            return callDurationSeconds;
        }

        public void setCallDurationSeconds(Integer callDurationSeconds) {
            this.callDurationSeconds = callDurationSeconds;
        }

        public String getRingtoneName() {
            return ringtoneName;
        }

        public void setRingtoneName(String ringtoneName) {
            this.ringtoneName = ringtoneName;
        }

        public Boolean getVibrateEnabled() {
            return vibrateEnabled;
        }

        public void setVibrateEnabled(Boolean vibrateEnabled) {
            this.vibrateEnabled = vibrateEnabled;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
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

        public Long getTotalCallsTriggered() {
            return totalCallsTriggered;
        }

        public void setTotalCallsTriggered(Long totalCallsTriggered) {
            this.totalCallsTriggered = totalCallsTriggered;
        }

        public Long getCallsThisMonth() {
            return callsThisMonth;
        }

        public void setCallsThisMonth(Long callsThisMonth) {
            this.callsThisMonth = callsThisMonth;
        }

        public Long getCallsThisWeek() {
            return callsThisWeek;
        }

        public void setCallsThisWeek(Long callsThisWeek) {
            this.callsThisWeek = callsThisWeek;
        }

        public String getMostUsedPresetName() {
            return mostUsedPresetName;
        }

        public void setMostUsedPresetName(String mostUsedPresetName) {
            this.mostUsedPresetName = mostUsedPresetName;
        }

        public Integer getMostUsedPresetId() {
            return mostUsedPresetId;
        }

        public void setMostUsedPresetId(Integer mostUsedPresetId) {
            this.mostUsedPresetId = mostUsedPresetId;
        }

        public Integer getAverageDurationSeconds() {
            return averageDurationSeconds;
        }

        public void setAverageDurationSeconds(Integer averageDurationSeconds) {
            this.averageDurationSeconds = averageDurationSeconds;
        }

        public String getMostCommonTriggerMethod() {
            return mostCommonTriggerMethod;
        }

        public void setMostCommonTriggerMethod(String mostCommonTriggerMethod) {
            this.mostCommonTriggerMethod = mostCommonTriggerMethod;
        }
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

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getxPosition() {
            return xPosition;
        }

        public void setxPosition(Integer xPosition) {
            this.xPosition = xPosition;
        }

        public Integer getyPosition() {
            return yPosition;
        }

        public void setyPosition(Integer yPosition) {
            this.yPosition = yPosition;
        }

        public Integer getOpacity() {
            return opacity;
        }

        public void setOpacity(Integer opacity) {
            this.opacity = opacity;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Long getDefaultPresetId() {
            return defaultPresetId;
        }

        public void setDefaultPresetId(Long defaultPresetId) {
            this.defaultPresetId = defaultPresetId;
        }
    }



}
