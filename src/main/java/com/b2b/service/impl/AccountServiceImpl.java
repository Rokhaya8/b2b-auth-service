package com.b2b.service.impl;

import com.b2b.model.AdminUser;
import com.b2b.model.Company;
import com.b2b.repository.AdminUserRepository;
import com.b2b.repository.CompanyRepository;
import com.b2b.service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final CompanyRepository companyRepository;
    private final AdminUserRepository adminRepository;
    private final PasswordEncoder encoder;

    public AccountServiceImpl(CompanyRepository companyRepository,
                              AdminUserRepository adminRepository,
                              PasswordEncoder encoder) {
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
        this.encoder = encoder;
    }

    @Override
    public void changePassword(String email, String currentRaw, String newRaw) {
        if (email == null) throw new IllegalArgumentException("Not authenticated");
        String normalized = email.toLowerCase();

        // Administrateur
        AdminUser admin = adminRepository.findByEmail(normalized).orElse(null);
        if (admin != null) {
            checkPasswords(admin.getPassword(), currentRaw, newRaw);
            admin.setPassword(encoder.encode(newRaw));
            adminRepository.save(admin);
            return;
        }

        // Entreprise
        Company company = companyRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        checkPasswords(company.getPassword(), currentRaw, newRaw);
        company.setPassword(encoder.encode(newRaw));
        company.setMustChangePassword(false);
        companyRepository.save(company);
    }

    private void checkPasswords(String hash, String currentRaw, String newRaw) {
        if (!encoder.matches(currentRaw, hash))
            throw new IllegalArgumentException("Invalid current password");
        if (encoder.matches(newRaw, hash))
            throw new IllegalArgumentException("New password must be different");
    }
}
