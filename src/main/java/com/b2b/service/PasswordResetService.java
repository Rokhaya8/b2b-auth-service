package com.b2b.service;

import com.b2b.model.Company;
import com.b2b.model.PasswordResetToken;
import com.b2b.repository.CompanyRepository;
import com.b2b.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final CompanyRepository companies;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final MailService mailService;

    // =====================================================
    // CREATE RESET TOKEN + SEND EMAIL (TOKEN ONLY)
    // =====================================================
    public Optional<String> createResetTokenForEmail(String email) {

        email = email.toLowerCase().trim();

        Company company = companies.findByEmail(email).orElse(null);
        if (company == null) {
            // Do not reveal whether the email exists
            return Optional.empty();
        }

        tokens.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        tokens.save(resetToken);

        mailService.sendResetPasswordEmail(email, token);

        return Optional.of(token);
    }

    // =====================================================
    // RESET PASSWORD USING TOKEN
    // =====================================================
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokens.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokens.delete(resetToken);
            throw new IllegalArgumentException("Token expiré");
        }

        Company company = companies.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));

        company.setPassword(encoder.encode(newPassword));
        company.setMustChangePassword(false);
        companies.save(company);

        tokens.delete(resetToken);
    }
}
