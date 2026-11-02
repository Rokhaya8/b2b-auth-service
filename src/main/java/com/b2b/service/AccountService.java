package com.b2b.service;

public interface AccountService {

    /**
     * Change password for either an AdminUser or Company user.
     *
     * @param email        the user's email
     * @param currentRaw   the current (non-hashed) password
     * @param newRaw       the new (non-hashed) password
     */
    void changePassword(String email, String currentRaw, String newRaw);
}
