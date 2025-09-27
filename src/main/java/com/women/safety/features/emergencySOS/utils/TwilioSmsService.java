package com.women.safety.features.emergencySOS.utils;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    private boolean twilioInitialized = false;

    private void initializeTwilio() {
        if (!twilioInitialized) {
            try {
                Twilio.init(accountSid, authToken);
                twilioInitialized = true;
                logger.info("Twilio initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio: {}", e.getMessage());
                throw new RuntimeException("Twilio initialization failed", e);
            }
        }
    }

    public boolean sendSms(String toPhoneNumber, String messageBody) {

        if (!smsEnabled) {
            // Mock behavior: just log instead of sending
            System.out.println("MOCK SMS → To: " + toPhoneNumber + " | Message: " + messageBody);
            return true; // pretend success
        }

        try {
            initializeTwilio();

            // Ensure phone number has country code
            String formattedNumber = formatPhoneNumber(toPhoneNumber);

            Message message = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            logger.info("SMS sent successfully to {} with SID: {}", formattedNumber, message.getSid());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            return false;
        }
    }

    public boolean sendEmergencyAlert(String toPhoneNumber, String userName, String alertMessage,
                                      Double latitude, Double longitude, String locationAddress) {
        try {
            StringBuilder emergencyMessage = new StringBuilder();
            emergencyMessage.append("EMERGENCY ALERT\n\n");
            emergencyMessage.append("From: ").append(userName).append("\n");
            emergencyMessage.append("I need immediate help! \n");

//            if (alertMessage != null && !alertMessage.trim().isEmpty()) {
//                emergencyMessage.append("Message: ").append(alertMessage).append("\n");
//            }

            emergencyMessage.append("\nLOCATION:\n");

            if (locationAddress != null && !locationAddress.trim().isEmpty()) {
                emergencyMessage.append("Address: ").append(locationAddress).append("\n");
            }
            if (latitude != null && longitude != null) {
                emergencyMessage.append("Coordinates: ").append(latitude).append(", ").append(longitude).append("\n");
                emergencyMessage.append("Google Maps: https://maps.google.com/?q=").append(latitude).append(",").append(longitude).append("\n");
            }
            emergencyMessage.append("\nThis is an automated emergency alert. Please check on this person immediately!");

            return sendSms(toPhoneNumber, emergencyMessage.toString());

        } catch (Exception e) {
            logger.error("Failed to send emergency alert to {}: {}", toPhoneNumber, e.getMessage());
            return false;
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        // Ensure number starts with +91
        if (!phoneNumber.startsWith("+91")) {
            if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned; // Indian Code
            } else if (cleaned.length() == 12 && cleaned.startsWith("91")) {
                cleaned = "+" + cleaned;
            } else {
                cleaned = "+91" + cleaned;
            }
        }else {
            cleaned = phoneNumber;
        }
        return cleaned;
    }

    public boolean isServiceAvailable() {
        try {
            initializeTwilio();
            return true;
        } catch (Exception e) {
            logger.error("Twilio service is not available: {}", e.getMessage());
            return false;
        }
    }
}
