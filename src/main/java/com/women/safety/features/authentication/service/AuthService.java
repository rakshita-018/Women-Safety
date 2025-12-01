package com.women.safety.features.authentication.service;

import com.women.safety.features.authentication.dto.AuthenticationRequestBody;
import com.women.safety.features.authentication.dto.AuthenticationResponseBody;
import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.authentication.repository.AuthUserRepository;
import com.women.safety.features.authentication.utils.EmailService;
import com.women.safety.features.authentication.utils.JwtService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final int durationInMinutes = 5;

    private final JwtService jwtService;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(JwtService jwtService, AuthUserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, EmailService emailService) {
        this.jwtService = jwtService;
        this.authUserRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public static String generateEmailVerificationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(5);
        for (int i = 0; i < 6; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }

    public void sendEmailVerificationToken(String email){
        Optional<AuthUser> user = authUserRepository.findByEmail(email);
        if(!user.get().getEmailVerified()){
            String emailVerificationToken = generateEmailVerificationToken();
            String hashedToken = passwordEncoder.encode(emailVerificationToken);
            user.get().setEmailVerificationToken(hashedToken);
            user.get().setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));
            authUserRepository.save(user.get());

            String subject = "Email Verification";
            String body = String.format("Only one step to take full advantage of Women Safety App .\n\n"
                            + "Enter this code to verify your email: " + "%s\n\n" + "The code will expire in " + "%s"
                            + " minutes.",
                    emailVerificationToken, durationInMinutes);
            try{
                emailService.sendEmail(email, subject, body);
                System.out.println("Email verification token is sent service");
            }catch (Exception e) {
                logger.info("Error while sending email: {}", e.getMessage());
            }
        }else{
            throw new IllegalArgumentException("Email verification token failed, or email is already verified.");
        }
    }

    @Transactional
    public void validateEmailVerificationToken(String token, String email){
        Optional<AuthUser> user = authUserRepository.findByEmail(email);
        if(user.isPresent() && passwordEncoder.matches(token, user.get().getEmailVerificationToken()) &&
        !user.get().getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())){
            user.get().setEmailVerified(true);
            user.get().setEmailVerificationToken(null);
            user.get().setEmailVerificationTokenExpiryDate(null);
            authUserRepository.save(user.get());
        }else if(user.isPresent() && passwordEncoder.matches(token, user.get().getEmailVerificationToken())
                && user.get().getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Email verification token expired.");
        }else{
            throw new IllegalArgumentException("Email verification token failed");
        }
    }

    @Transactional
    public AuthenticationResponseBody register(AuthenticationRequestBody registerRequestBody){

        AuthUser user = authUserRepository.save(new AuthUser(
                registerRequestBody.getEmail(), passwordEncoder.encode(registerRequestBody.getPassword())
        ));
        String emailVerificationToken = generateEmailVerificationToken();
        String hashedToken = passwordEncoder.encode(emailVerificationToken);
        user.setEmailVerificationToken(hashedToken);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));

        authUserRepository.save(user);

        String subject = "Email Verification";
        String body = String.format("Only one step to take full advantage of Women Safety App\n\n"
                        + "Enter this code to verify your email: " + "%s\n\n" + "The code will expire in " + "%s"
                        + " minutes.",
                emailVerificationToken, durationInMinutes);
        try {
            emailService.sendEmail(registerRequestBody.getEmail(), subject, body);
        } catch (Exception e) {
            logger.info("Error while sending email: {}", e.getMessage());
        }
        String authToken = jwtService.generateToken(registerRequestBody.getEmail());
        return new AuthenticationResponseBody(authToken, "User registered successfully.");
    }

    @Transactional
    public AuthenticationResponseBody login(AuthenticationRequestBody loginRequestBody){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestBody.getEmail(),
                        loginRequestBody.getPassword()
                )
        );

        AuthUser user = authUserRepository.findByEmail(loginRequestBody.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(loginRequestBody.getEmail());
        return  new AuthenticationResponseBody(token, "Authentication Succeeded.");
    }


    //    password reset token
    public void sendPasswordResetToken(String email) {
        Optional<AuthUser> user = authUserRepository.findByEmail(email);
        if (user.isPresent()) {
            String passwordResetToken = generateEmailVerificationToken();
            String hashedToken = passwordEncoder.encode(passwordResetToken);
            user.get().setPasswordResetToken(hashedToken);
            user.get().setPasswordResetTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));
            authUserRepository.save(user.get());
            String subject = "Password Reset";
            String body = String.format("""
                            You requested a password reset.
                            
                            Enter this code to reset your password: %s. The code will expire in %s minutes.""",
                    passwordResetToken, durationInMinutes);
            try {
                emailService.sendEmail(email, subject, body);
            } catch (Exception e) {
                logger.info("Error while sending email: {}", e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found.");
        }
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String token) {
        Optional<AuthUser> user = authUserRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(token, user.get().getPasswordResetToken())
                && !user.get().getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            user.get().setPasswordResetToken(null);
            user.get().setPasswordResetTokenExpiryDate(null);
            user.get().setPassword(passwordEncoder.encode(newPassword));
            authUserRepository.save(user.get());
        } else if (user.isPresent() && passwordEncoder.matches(token, user.get().getPasswordResetToken())
                && user.get().getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token expired.");
        } else {
            throw new IllegalArgumentException("Password reset token failed.");
        }
    }

    // Get user profile
    public AuthUser getUserProfile(String email) {
        return authUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // Update user profile
    @Transactional
    public AuthUser updateUserProfile(String email, String firstName, String lastName, String phoneNumber) {
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (firstName != null) {
            user.setFirstName(firstName.trim().isEmpty() ? null : firstName.trim());
        }

        if (lastName != null) {
            user.setLastName(lastName.trim().isEmpty() ? null : lastName.trim());
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
        }

        return authUserRepository.save(user);
    }



}
