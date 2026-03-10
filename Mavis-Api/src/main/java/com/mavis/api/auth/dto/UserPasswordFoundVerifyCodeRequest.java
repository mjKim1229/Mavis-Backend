package com.mavis.api.auth.dto;

public record UserPasswordFoundVerifyCodeRequest(
        String token,
        String password
) {
}
