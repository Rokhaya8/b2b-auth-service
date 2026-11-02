package com.b2b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Les utilisateurs sont gérés par AuthController : pas d'utilisateur par défaut de Spring Security
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class B2bAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(B2bAuthServiceApplication.class, args);
    }
}