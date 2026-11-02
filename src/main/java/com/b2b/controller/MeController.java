package com.b2b.controller;

import com.b2b.dto.ApiResponse;
import com.b2b.dto.MeResponse;
import com.b2b.model.AdminUser;
import com.b2b.model.Company;
import com.b2b.repository.AdminUserRepository;
import com.b2b.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MeController {

    private final AdminUserRepository admins;
    private final CompanyRepository companies;

    public MeController(AdminUserRepository admins, CompanyRepository companies) {
        this.admins = admins;
        this.companies = companies;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            // Normalement ça ne devrait pas arriver car /api/me est protégé
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Non authentifié"));
        }

        String email = auth.getName().toLowerCase();

        // 1) Essayer en tant qu'ADMIN
        AdminUser admin = admins.findByEmail(email).orElse(null);
        if (admin != null) {
            MeResponse data = new MeResponse(
                    admin.getEmail(),
                    "ADMIN",
                    null,
                    null  // si tu veux plus tard stocker un "nom" pour l'admin, tu pourras le mettre ici
            );
            return ResponseEntity.ok(ApiResponse.success(data));
        }

        // 2) Essayer en tant que COMPANY
        Company company = companies.findByEmail(email).orElse(null);
        if (company != null) {
            MeResponse data = new MeResponse(
                    company.getEmail(),
                    "COMPANY",
                    company.getId(),
                    company.getName()
            );
            return ResponseEntity.ok(ApiResponse.success(data));
        }

        // 3) Cas bizarre : le token contient un email qui n'existe plus
        return ResponseEntity.status(404)
                .body(ApiResponse.error("Utilisateur introuvable"));
    }
}
