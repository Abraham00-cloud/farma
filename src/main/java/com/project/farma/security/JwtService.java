package com.project.farma.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService{
    private static final String SECRET_KEY = "3c9a6f8b5d2e7c4a1b0d9f8e7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email, Long userId, Long organisationId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("organisationId", organisationId);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
//        return getAllClaims(token).getSubject();  this works well without the functional interface get claim method
        return getClaim(token, Claims::getSubject);
    }

    public Long extractOrganisationId(String token) {
//        return getAllClaims(token).get("organisationId", Long.class);
        return getClaim(token, claims -> claims.get("organisationId", Long.class));
    }

    public Long extractUserId(String token) {
        return getClaim(token, claims -> claims.get("userId", Long.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }


}
