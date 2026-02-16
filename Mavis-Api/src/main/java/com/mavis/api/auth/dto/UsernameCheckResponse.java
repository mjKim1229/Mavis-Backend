package com.mavis.api.auth.dto;

public record UsernameCheckResponse(
        String username,
        boolean available
) {
}
