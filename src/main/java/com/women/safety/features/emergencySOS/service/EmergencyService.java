package com.women.safety.features.emergencySOS.service;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;

import com.women.safety.features.emergencySOS.dto.EmergencyAlertRequestDTO;
import com.women.safety.features.emergencySOS.dto.EmergencyAlertResponseDTO;
import com.women.safety.features.emergencySOS.dto.EmergencyContactDTO;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import com.women.safety.features.emergencySOS.repository.EmergencyAlertRepository;
import com.women.safety.features.emergencySOS.repository.EmergencyContactRepository;
import com.women.safety.features.emergencySOS.utils.TwilioSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EmergencyService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyService.class);
    private static final int MAX_EMERGENCY_CONTACTS = 10;

    private final EmergencyContactRepository emergencyContactRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final AuthUserRepository authUserRepository;
    private final TwilioSmsService twilioSmsService;

    public EmergencyService(EmergencyContactRepository emergencyContactRepository,
                            EmergencyAlertRepository emergencyAlertRepository,
                            AuthUserRepository authUserRepository,
                            TwilioSmsService twilioSmsService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.authUserRepository = authUserRepository;
        this.twilioSmsService = twilioSmsService;
    }

    // Emergency Contacts Management
    @Transactional
    public EmergencyContact addEmergencyContact(String userEmail, EmergencyContactDTO contactDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user has reached maximum contacts limit
        long contactCount = emergencyContactRepository.countByUser(user);
        if (contactCount >= MAX_EMERGENCY_CONTACTS) {
            throw new IllegalArgumentException("Maximum emergency contacts limit reached (" + MAX_EMERGENCY_CONTACTS + ")");
        }

        // Check if phone number already exists for this user
        if (emergencyContactRepository.existsByUserAndPhoneNumber(user, contactDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("This phone number is already added as an emergency contact");
        }

        EmergencyContact contact = new EmergencyContact(
                user,
                contactDTO.getContactName(),
                contactDTO.getPhoneNumber(),
                contactDTO.getRelationship()
        );

        return emergencyContactRepository.save(contact);
    }

    public List<EmergencyContact> getUserEmergencyContacts(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return emergencyContactRepository.findByUserOrderByCreatedAtAsc(user);
    }

    @Transactional
    public EmergencyContact updateEmergencyContact(String userEmail, Long contactId, EmergencyContactDTO contactDTO) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency contact not found"));

        // Verify contact belongs to user
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to update this contact");
        }

        // Check if phone number already exists for this user (excluding current contact)
        if (!contact.getPhoneNumber().equals(contactDTO.getPhoneNumber()) &&
                emergencyContactRepository.existsByUserAndPhoneNumber(user, contactDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("This phone number is already added as an emergency contact");
        }

        contact.setContactName(contactDTO.getContactName());
        contact.setPhoneNumber(contactDTO.getPhoneNumber());
        contact.setRelationship(contactDTO.getRelationship());

        return emergencyContactRepository.save(contact);
    }

    @Transactional
    public void deleteEmergencyContact(String userEmail, Long contactId) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Emergency contact not found"));

        // Verify contact belongs to user
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to delete this contact");
        }

        emergencyContactRepository.delete(contact);
    }

    // Emergency Alert System
    @Transactional
    public EmergencyAlertResponseDTO triggerEmergencyAlert(String userEmail, EmergencyAlertRequestDTO alertRequest) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get user's emergency contacts
        List<EmergencyContact> emergencyContacts = emergencyContactRepository.findByUserOrderByCreatedAtAsc(user);

        if (emergencyContacts.isEmpty()) {
            throw new IllegalArgumentException("No emergency contacts found. Please add emergency contacts first.");
        }

        // Create emergency alert record
        EmergencyAlert alert = new EmergencyAlert(
                user,
                alertRequest.getAlertMessage(),
                alertRequest.getLatitude(),
                alertRequest.getLongitude(),
                alertRequest.getLocationAddress()
        );
        alert = emergencyAlertRepository.save(alert);

        // Send SMS alerts asynchronously
        String userName = (user.getFirstName() != null && user.getLastName() != null)
                ? user.getFirstName() + " " + user.getLastName()
                : user.getEmail();

        EmergencyAlert finalAlert = alert;
        CompletableFuture.runAsync(() -> sendEmergencyAlerts(finalAlert, emergencyContacts, userName));

        return new EmergencyAlertResponseDTO(alert, "Emergency alert triggered successfully. Notifying emergency contacts...");
    }

    private void sendEmergencyAlerts(EmergencyAlert alert, List<EmergencyContact> contacts, String userName) {
        int successCount = 0;

        for (EmergencyContact contact : contacts) {
            try {
                boolean sent = twilioSmsService.sendEmergencyAlert(
                        contact.getPhoneNumber(),
                        userName,
                        alert.getAlertMessage(),
                        alert.getLatitude(),
                        alert.getLongitude(),
                        alert.getLocationAddress()
                );

                if (sent) {
                    successCount++;
                    logger.info("Emergency alert sent successfully to: {}", contact.getContactName());
                } else {
                    logger.error("Failed to send emergency alert to {} ({}): SMS service returned false",
                            contact.getContactName(), contact.getPhoneNumber());
                }

                // Add delay between messages to avoid rate limiting
                Thread.sleep(1000);

            } catch (Exception e) {
                logger.error("Error sending emergency alert to {}: {}", contact.getContactName(), e.getMessage());
            }
        }

        // Update alert with notification count
        alert.setContactsNotifiedCount(successCount);
        emergencyAlertRepository.save(alert);

        logger.info("Emergency alert completed. {} out of {} contacts notified successfully.",
                successCount, contacts.size());
    }

    // Test SMS service (for testing purposes)
    public boolean testSmsService(String phoneNumber, String message) {
        if (!twilioSmsService.isServiceAvailable()) {
            throw new IllegalStateException("SMS service is not available. Please check Twilio configuration.");
        }
        return twilioSmsService.sendSms(phoneNumber, message);
    }

    public List<EmergencyAlert> getUserEmergencyAlerts(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return emergencyAlertRepository.findByUserOrderByCreatedAtDesc(user);
    }

//    @Transactional
//    public EmergencyAlert resolveEmergencyAlert(String userEmail, Long alertId) {
//        AuthUser user = authUserRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
//                .orElseThrow(() -> new IllegalArgumentException("Emergency alert not found"));
//
//        // Verify alert belongs to user
//        if (!alert.getUser().getId().equals(user.getId())) {
//            throw new IllegalArgumentException("Unauthorized to resolve this alert");
//        }
//
//        alert.setAlertStatus(EmergencyAlert.AlertStatus.RESOLVED);
//        alert.setResolvedAt(LocalDateTime.now());
//
//        return emergencyAlertRepository.save(alert);
//    }
//
//    public List<EmergencyAlert> getActiveEmergencyAlerts(String userEmail) {
//        AuthUser user = authUserRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        return emergencyAlertRepository.findByUserAndAlertStatusOrderByCreatedAtDesc(
//                user, EmergencyAlert.AlertStatus.ACTIVE);
//    }
}