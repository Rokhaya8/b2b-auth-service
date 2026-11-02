package com.b2b.controller;

import com.b2b.dto.ApiResponse;
import com.b2b.dto.PasswordChangeRequest;
import com.b2b.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService account;

    public AccountController(AccountService account) {
        this.account = account;
    }

    @PutMapping("/password")
    public ResponseEntity<?> changeMyPassword(@Valid @RequestBody PasswordChangeRequest req,
                                              Authentication auth) {
        String email = (auth != null) ? auth.getName() : null;

        try {
            account.changePassword(email, req.getCurrentPassword(), req.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès", null));
        } catch (IllegalArgumentException ex) {
            // Erreurs fonctionnelles (mdp actuel invalide, même mdp, compte désactivé, etc.)
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Erreur serveur lors du changement de mot de passe"));
        }
    }
}