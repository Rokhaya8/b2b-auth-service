package com.b2b.config;

import com.b2b.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String secret;

    // On accepte que la propriété security.jwt.expiration soit en millisecondes (comme dans application-dev.properties)
    @Value("${security.jwt.expiration:86400000}")
    private long expirationMillis;

    @Value("${security.jwt.issuer:b2b-application}")
    private String issuer;

    @Bean
    public JwtUtil jwtUtil() {
        long minutes = Math.max(1, expirationMillis / 60000L);
        return new JwtUtil(secret, issuer, minutes);
    }
}

