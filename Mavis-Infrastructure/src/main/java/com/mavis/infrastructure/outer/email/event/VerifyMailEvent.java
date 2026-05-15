package com.mavis.infrastructure.outer.email.event;

public record VerifyMailEvent(String to, String subject, String authCode) {}
