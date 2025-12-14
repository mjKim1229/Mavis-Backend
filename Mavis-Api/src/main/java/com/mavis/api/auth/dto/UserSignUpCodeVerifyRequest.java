package com.mavis.api.auth.dto;

public record UserSignUpCodeVerifyRequest(
        String email,
        Integer code
) {
}
