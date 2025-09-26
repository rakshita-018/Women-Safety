package com.women.safety.features.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

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


    public AuthUser(){}

    public AuthUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
