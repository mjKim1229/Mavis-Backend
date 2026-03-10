package com.mavis.api.auth.dto;

public record UserPasswordFoundVerifyCreateRequest(
        String username,
        String email
) {
}
