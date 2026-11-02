package com.b2b.controller;

import com.b2b.auth.LoginResponse;
import com.b2b.auth.UserPayload;
import com.b2b.security.JwtUtil;
import com.b2b.dto.LoginRequest;
import com.b2b.dto.RegisterRequest;
import com.b2b.dto.ApiResponse;
import com.b2b.model.AdminUser;
import com.b2b.model.Company;
import com.b2b.repository.AdminUserRepository;
import com.b2b.repository.CompanyRepository;
import com.b2b.service.PasswordResetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminUserRepository admins;
    private final CompanyRepository companies;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final PasswordResetService passwordResetService;

    public AuthController(AdminUserRepository admins,
                          CompanyRepository companies,
                          PasswordEncoder encoder,
                          JwtUtil jwt,
                          PasswordResetService passwordResetService) {
        this.admins = admins;
        this.companies = companies;
        this.encoder = encoder;
        this.jwt = jwt;
        this.passwordResetService = passwordResetService;
    }

    // ======================================================
    // REGISTER COMPANY
    // ======================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();

        if (email.isBlank() || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Email et mot de passe sont obligatoires"));
        }

        if (admins.findByEmail(email).isPresent() || companies.existsByEmail(email)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Cet email est déjà utilisé"));
        }

        if (companies.existsByIce(req.getIce())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Cet ICE est déjà utilisé"));
        }

        Company c = new Company();
        c.setName(req.getName());
        c.setEmail(email);
        c.setIce(req.getIce());
        c.setPassword(encoder.encode(req.getPassword()));
        c.setAddress(req.getAddress());
        c.setCity(req.getCity());
        c.setPhone(req.getPhone());
        c.setEnabled(true);

        companies.save(c);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entreprise créée avec succès", null));
    }

    // ======================================================
    // LOGIN (ADMIN + COMPANY)
    // ======================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String email = request.getEmail() == null ? "" : request.getEmail().toLowerCase();
        String raw = request.getPassword() == null ? "" : request.getPassword();

        try {
            // ---------- ADMIN ----------
            var adminOpt = admins.findByEmail(email);
            if (adminOpt.isPresent()) {
                AdminUser a = adminOpt.get();

                if (!a.isEnabled())
                    return ResponseEntity.status(401).body(Map.of("error", "USER_DISABLED"));

                if (!encoder.matches(raw, a.getPassword()))
                    return ResponseEntity.status(401).body(Map.of("error", "BAD_CREDENTIALS"));

                String token = jwt.generateToken(null, email, "ADMIN", null);
                return ResponseEntity.ok(
                        new LoginResponse(token, new UserPayload(null, email, "ADMIN", null), false)
                );
            }

            // ---------- COMPANY ----------
            Company c = companies.findByEmail(email).orElse(null);

            if (c == null || !encoder.matches(raw, c.getPassword()))
                return ResponseEntity.status(401).body(Map.of("error", "BAD_CREDENTIALS"));

            if (!c.isEnabled())
                return ResponseEntity.status(401).body(Map.of("error", "USER_DISABLED"));

            String token = jwt.generateToken(c.getId(), email, "COMPANY", c.getId());

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            new UserPayload(c.getId(), email, "COMPANY", c.getId()),
                            c.isMustChangePassword()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "SERVER_ERROR"));
        }
    }

    // ======================================================
    // FORGOT PASSWORD (SEND EMAIL)
    // ======================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Email est obligatoire"));
        }

        // Always return 200 (security best practice)
        passwordResetService.createResetTokenForEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.",
                        null
                )
        );
    }

    // ======================================================
    // RESET PASSWORD
    // ======================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()
                || newPassword == null || newPassword.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Token et nouveau mot de passe sont obligatoires"));
        }

        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(
                    ApiResponse.success("Mot de passe réinitialisé avec succès", null)
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(ex.getMessage()));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse.error("Erreur serveur lors de la réinitialisation"));
        }
    }

    // ======================================================
    // LOGOUT
    // ======================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Déconnexion réussie")
        );
    }
}
