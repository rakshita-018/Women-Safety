package com.women.safety.features.authentication.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private SecretKey getSigninKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    // Generate JWT
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // new builder style (instead of setSubject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr
                .signWith(getSigninKey())
                .compact();
    }

    // Extract email (subject)
    public String extractEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate token
    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmailFromToken(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    // Expiration check
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey()) // replaces setSigningKey()
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
