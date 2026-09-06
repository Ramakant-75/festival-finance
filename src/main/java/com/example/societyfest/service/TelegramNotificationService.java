package com.example.societyfest.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Sends admin notifications via the Telegram Bot API.
 *
 * Setup:
 * 1. Create a bot via @BotFather on Telegram, get the bot token.
 * 2. Message your new bot once from the admin's Telegram account (so Telegram
 *    has a chat to deliver to), then call https://api.telegram.org/bot<token>/getUpdates
 *    to read back the chat id.
 * 3. Set telegram.bot-token and telegram.admin-chat-id (see application-local.yml).
 *
 * Notifications are fire-and-forget: failures are logged, never thrown, so a
 * Telegram outage can never break signup or any other calling flow.
 */
@Slf4j
@Service
public class TelegramNotificationService {

    private final WebClient webClient;

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.admin-chat-id:}")
    private String adminChatId;

    @Value("${telegram.enabled:true}")
    private boolean enabled;

    public TelegramNotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.telegram.org")
                .build();
    }

    /**
     * Notify the admin that a new user has signed up and is pending approval.
     */
    public void notifyAdminOfNewSignup(String username, String mailId) {
        String text = String.format(
                "🆕 *New user signup pending approval*%n%n" +
                        "👤 Username: %s%n" +
                        "📧 Email: %s%n%n" +
                        "Please review and activate this user from the admin panel.",
                escapeMarkdown(username),
                escapeMarkdown(mailId)
        );

        sendMessage(text);
    }

    /**
     * Generic helper for sending any admin alert via Telegram. Other features
     * (e.g. large expense approvals, milestone alerts) can reuse this directly.
     */
    public void sendMessage(String text) {
        if (!enabled) {
            log.debug("Telegram notifications disabled, skipping message");
            return;
        }
        if (botToken == null || botToken.isBlank() || adminChatId == null || adminChatId.isBlank()) {
            log.warn("Telegram bot-token or admin-chat-id not configured, skipping notification");
            return;
        }

        webClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .bodyValue(Map.of(
                        "chat_id", adminChatId,
                        "text", text,
                        "parse_mode", "Markdown"
                ))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .doOnError(e -> log.error("Failed to send Telegram notification: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private String escapeMarkdown(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}
