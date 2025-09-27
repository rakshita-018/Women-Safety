package com.women.safety.features.emergencySOS.controller;

import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertRequestDTO;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertResponseDTO;
import com.women.safety.features.emergencySOS.dto.EmergencyContactDTO;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import com.women.safety.features.emergencySOS.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {
    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    // Emergency Contacts
    @PostMapping("/contacts")
    public ResponseEntity<Map<String, Object>> addEmergencyContact(
            @Valid @RequestBody EmergencyContactDTO contactDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails ){
        EmergencyContact contact = emergencyService.addEmergencyContact(userDetails.getUsername(), contactDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Emergency contact added successfully",
                "contact" , contact
        ));
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<EmergencyContact>> getEmergencyContacts(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<EmergencyContact> contacts = emergencyService.getUserEmergencyContacts(userDetails.getUsername());
        return ResponseEntity.ok(contacts);
    }

    @PutMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> updateEmergencyContact(
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactDTO contactDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EmergencyContact contact = emergencyService.updateEmergencyContact(
                userDetails.getUsername(), contactId, contactDTO);

        return ResponseEntity.ok(Map.of(
                "message", "Emergency contact updated successfully",
                "contact", contact
        ));
    }

    @DeleteMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, String>> deleteEmergencyContact(
            @PathVariable Long contactId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        emergencyService.deleteEmergencyContact(userDetails.getUsername(), contactId);

        return ResponseEntity.ok(Map.of("message", "Emergency contact deleted successfully"));
    }

    //Emergency alert
    @PostMapping("/alerts")
    public ResponseEntity<EmergencyAlertResponseDTO> triggerEmergencyAlert(
            @RequestBody EmergencyAlertRequestDTO alertRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        EmergencyAlertResponseDTO response = emergencyService.triggerEmergencyAlert(
                userDetails.getUsername(), alertRequest
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<EmergencyAlert>> getEmergencyAlerts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<EmergencyAlert> alerts = emergencyService.getUserEmergencyAlerts(userDetails.getUsername());
        return ResponseEntity.ok(alerts);
    }

    // Quick Emergency Alert (using default message and current location)
    @PostMapping("/alert/quick")
    public ResponseEntity<EmergencyAlertResponseDTO> quickEmergencyAlert(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String locationAddress,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EmergencyAlertRequestDTO quickAlert = new EmergencyAlertRequestDTO(
                "EMERGENCY! I need immediate help. Please check on me.",
                latitude,
                longitude,
                locationAddress
        );

        EmergencyAlertResponseDTO response = emergencyService.triggerEmergencyAlert(
                userDetails.getUsername(), quickAlert);

        return ResponseEntity.ok(response);
    }

    // Test SMS functionality (for testing purposes)
    @PostMapping("/test-sms")
    public ResponseEntity<Map<String, String>> testSms(
            @RequestParam String phoneNumber,
            @RequestParam(required = false, defaultValue = "Test message from Women Safety App") String message,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // This is a test endpoint - in production, you might want to restrict this
        try {
            boolean sent = emergencyService.testSmsService(phoneNumber, message);
            if (sent) {
                return ResponseEntity.ok(Map.of("message", "Test SMS sent successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to send test SMS"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

//    @GetMapping("/alerts/active")
//    public ResponseEntity<List<EmergencyAlert>> getActiveEmergencyAlerts(
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        List<EmergencyAlert> alerts = emergencyService.getActiveEmergencyAlerts(userDetails.getUsername());
//        return ResponseEntity.ok(alerts);
//    }
//
//    @PutMapping("/alerts/{alertId}/resolve")
//    public ResponseEntity<Map<String, Object>> resolveEmergencyAlert(
//            @PathVariable Long alertId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        EmergencyAlert alert = emergencyService.resolveEmergencyAlert(userDetails.getUsername(), alertId);
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Emergency alert resolved successfully",
//                "alert", alert
//        ));
//    }
}
