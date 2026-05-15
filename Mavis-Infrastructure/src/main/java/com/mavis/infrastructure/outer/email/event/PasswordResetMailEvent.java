package com.mavis.infrastructure.outer.email.event;

public record PasswordResetMailEvent(String to, String resetLink) {}
