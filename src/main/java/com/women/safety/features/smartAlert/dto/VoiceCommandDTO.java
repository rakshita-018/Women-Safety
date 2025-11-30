package com.women.safety.features.smartAlert.dto;

import com.women.safety.features.smartAlert.model.VoiceCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VoiceCommandDTO {

    @NotBlank(message = "Command text is required")
    private String commandText;

    private String audioUrl;
    private VoiceCommand.CommandType commandType;
    private Double confidenceScore;
    private Double latitude;
    private Double longitude;

    public VoiceCommandDTO() {}

    public VoiceCommandDTO(String commandText, VoiceCommand.CommandType commandType) {
        this.commandText = commandText;
        this.commandType = commandType;
    }

    public @NotBlank(message = "Command text is required") String getCommandText() {
        return commandText;
    }

    public void setCommandText(@NotBlank(message = "Command text is required") String commandText) {
        this.commandText = commandText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public VoiceCommand.CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(VoiceCommand.CommandType commandType) {
        this.commandType = commandType;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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
}
