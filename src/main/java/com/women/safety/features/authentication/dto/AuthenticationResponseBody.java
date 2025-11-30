package com.women.safety.features.authentication.dto;

import com.women.safety.features.authentication.model.AuthUser;
import lombok.Getter;

@Getter
public class AuthenticationResponseBody {
    private final String token;
    private final String message;

    public AuthenticationResponseBody(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    //    private final String token;
//    private final String message;
//    private final UserInfo user;
//
//    public AuthenticationResponseBody(String token, String message) {
//        this.token = token;
//        this.message = message;
//        this.user = null;
//    }
//
//    public AuthenticationResponseBody(String token, String message, AuthUser authUser) {
//        this.token = token;
//        this.message = message;
//        this.user = new UserInfo(authUser);
//    }
//
//    @Getter
//    public static class UserInfo {
//        private final Long id;
//        private final String email;
//        private final String firstName;
//        private final String lastName;
//        private final String fullName;
//        private final String phoneNumber;
//        private final Boolean emailVerified;
//        private final String role;
//        private final Boolean hasEmergencyContacts;
//        private final Long activeEmergencyAlerts;
//
//        public UserInfo(AuthUser authUser) {
//            this.id = authUser.getId();
//            this.email = authUser.getEmail();
//            this.firstName = authUser.getFirstName();
//            this.lastName = authUser.getLastName();
//            this.fullName = authUser.getFullName();
//            this.phoneNumber = authUser.getPhoneNumber();
//            this.emailVerified = authUser.getEmailVerified();
//            this.role = authUser.getUserRole().name();
//            this.hasEmergencyContacts = authUser.hasEmergencyContacts();
//            this.activeEmergencyAlerts = authUser.getActiveEmergencyAlertsCount();
//        }
//    }
}