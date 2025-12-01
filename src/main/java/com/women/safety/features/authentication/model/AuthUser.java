package com.women.safety.features.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authUser")
@Data
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "email" , unique = true)
    private String email;
    private Boolean emailVerified = false;
    private String emailVerificationToken = null;
    private LocalDateTime emailVerificationTokenExpiryDate = null;

    @JsonIgnore
    private String password;
    private String passwordResetToken = null;
    private LocalDateTime passwordResetTokenExpiryDate = null;

    private String phoneNumber;

    private String firstName = null;
    private String lastName = null;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Emergency Contact Relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    // Emergency Alert Relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmergencyAlert> emergencyAlerts = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AuthUser(){}

    public AuthUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthUser(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emergencyContacts = new ArrayList<>();
        this.emergencyAlerts = new ArrayList<>();
    }

    // Utility methods for emergency contacts
    public void addEmergencyContact(EmergencyContact contact) {
        emergencyContacts.add(contact);
        contact.setUser(this);
    }

    public void removeEmergencyContact(EmergencyContact contact) {
        emergencyContacts.remove(contact);
        contact.setUser(null);
    }

    // Utility methods for emergency alerts
    public void addEmergencyAlert(EmergencyAlert alert) {
        emergencyAlerts.add(alert);
        alert.setUser(this);
    }

    // Helper method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return email; // Fallback to email if no name is provided
        }
    }

    // Helper method to check if user has emergency contacts
    public boolean hasEmergencyContacts() {
        return emergencyContacts != null && !emergencyContacts.isEmpty();
    }

    // Helper method to get active emergency alerts count
    public long getActiveEmergencyAlertsCount() {
        if (emergencyAlerts != null) {
            return emergencyAlerts.stream()
                    .filter(alert -> alert.getAlertStatus() == EmergencyAlert.AlertStatus.ACTIVE)
                    .count();
        }
        return 0;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public LocalDateTime getEmailVerificationTokenExpiryDate() {
        return emailVerificationTokenExpiryDate;
    }

    public void setEmailVerificationTokenExpiryDate(LocalDateTime emailVerificationTokenExpiryDate) {
        this.emailVerificationTokenExpiryDate = emailVerificationTokenExpiryDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public LocalDateTime getPasswordResetTokenExpiryDate() {
        return passwordResetTokenExpiryDate;
    }

    public void setPasswordResetTokenExpiryDate(LocalDateTime passwordResetTokenExpiryDate) {
        this.passwordResetTokenExpiryDate = passwordResetTokenExpiryDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public List<EmergencyAlert> getEmergencyAlerts() {
        return emergencyAlerts;
    }

    public void setEmergencyAlerts(List<EmergencyAlert> emergencyAlerts) {
        this.emergencyAlerts = emergencyAlerts;
    }
}
