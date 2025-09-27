package com.women.safety.features.emergencySOS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmergencyContactDTO {

    private Long id;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String relationship;

    private Boolean isPrimary = false;

    public EmergencyContactDTO() {}

    public EmergencyContactDTO(String contactName, String phoneNumber, String relationship, Boolean isPrimary) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }
}
