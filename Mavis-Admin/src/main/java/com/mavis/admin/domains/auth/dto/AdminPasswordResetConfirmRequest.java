package com.mavis.admin.domains.auth.dto;

public record AdminPasswordResetConfirmRequest(String token, String newPassword) {}
