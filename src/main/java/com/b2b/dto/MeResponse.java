package com.b2b.dto;

public class MeResponse {

    private String email;
    private String role;
    private Long companyId;
    private String name;

    public MeResponse(String email, String role, Long companyId, String name) {
        this.email = email;
        this.role = role;
        this.companyId = companyId;
        this.name = name;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getCompanyId() { return companyId; }
    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public void setName(String name) { this.name = name; }
}
