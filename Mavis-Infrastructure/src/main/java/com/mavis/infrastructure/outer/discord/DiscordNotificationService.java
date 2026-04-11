package com.mavis.infrastructure.outer.discord;

import com.mavis.infrastructure.outer.api.discord.DiscordWebhookClient;
import com.mavis.infrastructure.outer.api.discord.dto.DiscordWebhookMessage;
import com.mavis.infrastructure.outer.api.discord.dto.DiscordWebhookMessage.Embed;
import com.mavis.infrastructure.outer.api.discord.dto.DiscordWebhookMessage.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private static final int ERROR_COLOR = 16711680; // 빨강 (#FF0000)
    private static final int MAX_STACK_TRACE_LENGTH = 1000;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscordWebhookClient discordWebhookClient;

    @Async
    public void sendErrorNotification(Exception e, String requestUri, String httpMethod) {
        try {
            DiscordWebhookMessage message = buildErrorMessage(e, requestUri, httpMethod);
            discordWebhookClient.send(message);
        } catch (Exception ex) {
            log.warn("Discord 에러 알림 전송 실패", ex);
        }
    }

    private DiscordWebhookMessage buildErrorMessage(Exception e, String requestUri, String httpMethod) {
        String stackTrace = truncateStackTrace(e);

        List<Field> fields = List.of(
                Field.builder().name("Method").value(httpMethod).inline(true).build(),
                Field.builder().name("URI").value(requestUri).inline(true).build(),
                Field.builder().name("발생 시각").value(LocalDateTime.now().format(FORMATTER)).inline(false).build(),
                Field.builder().name("Stack Trace").value("```\n" + stackTrace + "\n```").inline(false).build()
        );

        Embed embed = Embed.builder()
                .title("서버 에러 발생: " + e.getClass().getSimpleName())
                .description(e.getMessage() != null ? e.getMessage() : "메시지 없음")
                .color(ERROR_COLOR)
                .fields(fields)
                .build();

        return DiscordWebhookMessage.builder()
                .username("Mavis Error Bot")
                .embeds(List.of(embed))
                .build();
    }

    private String truncateStackTrace(Exception e) {
        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .reduce(e.toString() + "\n", (acc, line) -> acc + "\tat " + line + "\n");

        if (stackTrace.length() > MAX_STACK_TRACE_LENGTH) {
            return stackTrace.substring(0, MAX_STACK_TRACE_LENGTH) + "\n... (생략됨)";
        }
        return stackTrace;
    }
}
