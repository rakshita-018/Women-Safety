package com.women.safety.features.authentication.controller;

import com.women.safety.features.authentication.dto.AuthenticationRequestBody;
import com.women.safety.features.authentication.dto.AuthenticationResponseBody;
import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth/")
public class AuthUserController {
    private final AuthService authService;

    public AuthUserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthenticationResponseBody loginPage(@Valid @RequestBody AuthenticationRequestBody loginRequestBody) {
        return authService.login(loginRequestBody);
    }

    @PostMapping("/register")
    public AuthenticationResponseBody registerPage(@Valid @RequestBody AuthenticationRequestBody registerRequestBody) {
        return authService.register(registerRequestBody);
    }

    @GetMapping("/user")
    public AuthUser getUser(@RequestBody AuthUser user) {
        return user;
    }

    // Email
    @GetMapping("/send-email-verification-token")
    public String sendEmailVerificationToken(@AuthenticationPrincipal CustomUserDetails user) {
        authService.sendEmailVerificationToken(user.getUsername());
        System.out.println("Email verification token is sent to service");
        return "Email verification token sent successfully.";
    }

//    @PutMapping("/validate-email-verification-token")
//    public String verifyEmail(@RequestParam String token, @AuthenticationPrincipal CustomUserDetails user) {
//        authService.validateEmailVerificationToken(token, user.getUsername());
//        return "Email verified successfully.";
//    }

    @PutMapping("/validate-email-verification-token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token, @AuthenticationPrincipal CustomUserDetails user) {
        authService.validateEmailVerificationToken(token, user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("Message", "Email Verified successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/send-password-reset-token")
    public String sendPasswordResetToken(@RequestParam String email) {
        authService.sendPasswordResetToken(email);
        return "Password reset token sent successfully.";
    }

    @PutMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword, @RequestParam String token,
                                @RequestParam String email) {
        authService.resetPassword(email, newPassword, token);
        return "Password reset successfully.";
    }

    //Profile Management

    @GetMapping("/profile")
    public ResponseEntity<AuthUser> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthUser user = authService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthUser updatedUser = authService.updateUserProfile(
                userDetails.getUsername(), firstName, lastName, phoneNumber);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", updatedUser
        ));

    }
}
