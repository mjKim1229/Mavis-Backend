package com.mavis.infrastructure.outer.discord.client;

import com.mavis.infrastructure.outer.discord.dto.DiscordWebhookMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "DiscordWebhookClient",
        url = "${discord.webhook.url}"
)
public interface DiscordWebhookClient {

    @PostMapping
    void send(@RequestBody DiscordWebhookMessage message);
}
