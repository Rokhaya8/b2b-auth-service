package com.b2b.auth;

public record LoginResponse(String token, UserPayload user, boolean mustChangePassword) {}
