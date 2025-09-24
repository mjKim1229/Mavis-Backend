package com.mavis.api.auth.dto;

public record UserLoginRequest(
        String username,
        String password
){
}
