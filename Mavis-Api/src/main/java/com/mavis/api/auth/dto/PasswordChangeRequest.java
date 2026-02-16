package com.mavis.api.auth.dto;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {
}
