package com.b2b.dto;

public class LoginRequest {
    private String email;
    private String password;

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters (facultatifs mais utiles pour désérialisation JSON)
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
