package com.ailearning.platform.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-hours:24}")
    private int expirationHours;

    public String generateToken(UUID userId, String username, String email, String displayName, List<String> roles) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds((long) expirationHours * 3600);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("username", username)
                    .claim("email", email)
                    .claim("displayName", displayName)
                    .claim("roles", roles)
                    .issuer("ai-learning-platform")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new MACSigner(secret.getBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}
