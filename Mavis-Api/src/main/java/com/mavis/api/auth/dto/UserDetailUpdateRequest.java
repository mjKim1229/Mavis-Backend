package com.mavis.api.auth.dto;

import java.time.LocalDate;

public record UserDetailUpdateRequest(
        String nickname,
        String name,
        String gender,
        LocalDate birthDay,
        String phoneNumber
) {
}
