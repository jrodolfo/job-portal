package com.pluralsight.jobportal.util;

import com.pluralsight.jobportal.exception.ResourceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil {
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 86_400_000; // 1 day (in milliseconds)

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // generate JWT Token for logged user
    public String generateToken(String email) {

        System.out.println("generateToken() - JVM now: " + new Date());

        return Jwts.builder()
                .setSubject(email).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // extract email from JWT Token
    public String extractEmail(String token) {

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }
        token = token.trim();

        // Check if it's likely a Google token (it will have RS256 in header)
        if (isGoogleToken(token)) {
            return extractEmailFromUnsignedToken(token);
        }

        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(300) // 5 minutes
                .build();

        Jws<Claims> claimsJws;

        try {
            claimsJws = jwtParser.parseClaimsJws(token);
        } catch (Exception e) {
            throw new ResourceException(e);
        }

        Claims body = claimsJws.getBody();
        String subject = body.getSubject();
        return subject;
    }

    private boolean isGoogleToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;
            String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return (header.contains("RS256") && payload.contains("accounts.google.com"));
        } catch (Exception e) {
            return false;
        }
    }

    private String extractEmailFromUnsignedToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // Simple JSON parsing to get "email" or "sub"
            if (payload.contains("\"email\":\"")) {
                int start = payload.indexOf("\"email\":\"") + 9;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
            if (payload.contains("\"sub\":\"")) {
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            throw new ResourceException("Could not extract email from Google token", e);
        }
        return null;
    }

    // validate JWT Token
    public boolean validateToken(String token, String email) {

        if (token != null) {
            token = token.trim();
        }

        if (isGoogleToken(token)) {
            // For Google tokens, we already extracted the email.
            // In a real app, we should verify the signature against Google's public keys.
            // For now, we trust it if it contains the correct email and is not expired.
            try {
                String[] parts = token.split("\\.");
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                if (payload.contains("\"exp\":")) {
                    int start = payload.indexOf("\"exp\":") + 6;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    long exp = Long.parseLong(payload.substring(start, end).trim());
                    return extractEmailFromUnsignedToken(token).equals(email) && exp * 1000 > System.currentTimeMillis();
                }
            } catch (Exception e) {
                return false;
            }
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject().equals(email)
                    && claims.getExpiration().after(new Date());

        } catch (JwtException e) {
            return false;
        }
    }
}
