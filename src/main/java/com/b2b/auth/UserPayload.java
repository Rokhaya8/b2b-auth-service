package com.b2b.auth;

public record UserPayload(Long id, String email, String role, Long companyId) {}
