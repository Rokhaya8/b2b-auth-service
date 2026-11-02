package com.b2b.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {
    @NotBlank private String currentPassword;
    @NotBlank @Size(min = 8, max = 100) private String newPassword;
    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
}