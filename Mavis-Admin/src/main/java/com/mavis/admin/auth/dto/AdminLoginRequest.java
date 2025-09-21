package com.mavis.admin.auth.dto;

public record AdminLoginRequest(
        String username,
        String password
) {
}
