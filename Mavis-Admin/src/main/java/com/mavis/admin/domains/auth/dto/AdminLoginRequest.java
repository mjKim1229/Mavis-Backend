package com.mavis.admin.domains.auth.dto;

public record AdminLoginRequest(
        String username,
        String password
) {
}
