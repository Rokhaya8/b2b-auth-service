package com.b2b.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;               // <-- important
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private final SecretKey key;            // <-- SecretKey (pas Key)
    private final String issuer;
    private final long expirationMinutes;

    public JwtUtil(String secret, String issuer, long expirationMinutes) {
        // secret >= 32 chars pour HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    // Génération (JJWT 0.11.5 compatible)
    public String generateToken(Long userId, String email, String role, Long companyId) {
        Instant now = Instant.now();

        var builder = Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .claim("uid", userId)
                .claim("role", role);

        if (companyId != null) {
            builder.claim("companyId", companyId);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }


    // Parsing/Validation (JJWT 0.11.5 compatible)
    public Jws<Claims> parse(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        return parser.parseClaimsJws(token);
    }
}