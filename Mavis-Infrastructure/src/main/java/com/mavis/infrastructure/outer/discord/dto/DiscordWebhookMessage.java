package com.mavis.infrastructure.outer.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record DiscordWebhookMessage(
        String username,
        List<Embed> embeds
) {

    @Builder
    public record Embed(
            String title,
            String description,
            int color,
            List<Field> fields
    ) {
    }

    @Builder
    public record Field(
            String name,
            String value,
            boolean inline
    ) {
    }
}
