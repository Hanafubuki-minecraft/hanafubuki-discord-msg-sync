package com.hanafubuki.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/** Отправка через вебхук: у каждого сообщения своё имя и аватарка, упоминания (@everyone) отключены. */
public final class Webhook {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Logger log;
    private volatile String url;

    public Webhook(Logger log) {
        this.log = log;
    }

    public void setUrl(String url) {
        this.url = url == null || url.isBlank() ? null : url.trim();
    }

    public boolean ready() {
        return url != null;
    }

    public void send(String username, String avatarUrl, String content) {
        send(username, avatarUrl, content, false);
    }

    /** @param blocking true — ждать отправки (нужно при остановке сервера) */
    public void send(String username, String avatarUrl, String content, boolean blocking) {
        if (url == null || content == null || content.isBlank()) return;
        String json = "{"
                + "\"username\":" + q(cut(username, 80)) + ","
                + (avatarUrl != null ? "\"avatar_url\":" + q(avatarUrl) + "," : "")
                + "\"content\":" + q(cut(content, 2000)) + ","
                + "\"allowed_mentions\":{\"parse\":[]}"
                + "}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url + "?wait=false"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        if (blocking) {
            try {
                handle(http.send(req, HttpResponse.BodyHandlers.ofString()));
            } catch (Exception e) {
                log.warning("Webhook: " + e.getMessage());
            }
        } else {
            http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(this::handle)
                    .exceptionally(e -> { log.warning("Webhook: " + e.getMessage()); return null; });
        }
    }

    private void handle(HttpResponse<String> r) {
        if (r.statusCode() >= 300) log.warning("Webhook HTTP " + r.statusCode() + ": " + cut(r.body(), 200));
    }

    private static String cut(String s, int max) {
        return s == null ? "" : (s.length() > max ? s.substring(0, max) : s);
    }

    private static String q(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }
}
