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
}
