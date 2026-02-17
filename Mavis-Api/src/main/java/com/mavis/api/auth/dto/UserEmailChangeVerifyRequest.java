package com.mavis.api.auth.dto;

public record UserEmailChangeVerifyRequest(
        String newEmail,
        Integer code
) {
}
