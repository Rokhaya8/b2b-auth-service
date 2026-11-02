package com.b2b.config;

import com.b2b.model.AdminUser;
import com.b2b.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée un administrateur au démarrage s'il n'existe pas encore.
 * Identifiants configurables (variables ADMIN_EMAIL / ADMIN_PASSWORD) ;
 * les valeurs par défaut ne servent qu'à la démo locale.
 */
@Component
public class DemoAdminSeeder implements CommandLineRunner {

    private final AdminUserRepository admins;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    public DemoAdminSeeder(AdminUserRepository admins, PasswordEncoder encoder) {
        this.admins = admins;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        String normalized = email.toLowerCase();
        if (admins.findByEmail(normalized).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setEmail(normalized);
            admin.setPassword(encoder.encode(password));
            admins.save(admin);
        }
    }
}
