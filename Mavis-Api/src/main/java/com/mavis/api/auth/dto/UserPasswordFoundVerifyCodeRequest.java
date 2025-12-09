package com.mavis.api.auth.dto;

public record UserPasswordFoundVerifyCodeRequest(
        String email,
        Integer code
) {
}
