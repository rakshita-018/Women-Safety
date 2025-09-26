package com.women.safety.features.authentication.dto;

import lombok.Getter;

@Getter
public class AuthenticationResponseBody {
    private final String token;
    private final String message;

    public AuthenticationResponseBody(String token, String message) {
        this.token = token;
        this.message = message;
    }
}