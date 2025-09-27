package com.women.safety.features.authentication.dto;

import lombok.Data;

@Data
public class AuthenticationRequestBody {
    private String email;
    private String password;
    private String firstname;
    private String lastname;

    public AuthenticationRequestBody(String email, String password) {
        this.email = email;
        this.password = password;
    }
}