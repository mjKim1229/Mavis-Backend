package com.mavis.admin.domains.auth.dto;

public record AdminPasswordChangeRequest(String currentPassword, String newPassword) {}
