package com.hanafubuki.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Отправка через вебхук в отдельном потоке «HanafubukiDiscord-Webhook».
 * Чат и тики сервера только кладут сообщение в очередь и никогда не ждут Discord.
 * Отправка последовательная, с учётом лимитов Discord (429 → подождать и повторить).
 * У каждого сообщения своё имя и аватарка; упоминания (@everyone) отключены.
 */
public final class Webhook {

    private static final Pattern RETRY_AFTER = Pattern.compile("\"retry_after\"\\s*:\\s*([0-9.]+)");

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "HanafubukiDiscord-Webhook");
        t.setDaemon(true);
        return t;
    });
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

    /** В очередь — мгновенно, без ожидания сети. */
    public void send(String username, String avatarUrl, String content) {
        if (url == null || content == null || content.isBlank()) return;
        String json = json(username, avatarUrl, content);
        try {
            worker.execute(() -> post(json));
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            // плагин выключается
        }
    }

    /** Синхронно (при остановке сервера): дождаться отправки, но не дольше 5 секунд. */
    public void sendNow(String username, String avatarUrl, String content) {
        if (url == null || content == null || content.isBlank()) return;
        String json = json(username, avatarUrl, content);
        try {
            worker.submit(() -> post(json)).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warning("Webhook (shutdown): " + e.getMessage());
        }
    }

    public void shutdown() {
        worker.shutdown();
        try {
            worker.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void post(String json) {
        String target = url;
        if (target == null) return;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(target + "?wait=false"))
                        .timeout(Duration.ofSeconds(8))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() == 429) {
                    Thread.sleep(retryAfterMillis(r));
                    continue;
                }
                if (r.statusCode() >= 300) log.warning("Webhook HTTP " + r.statusCode() + ": " + cut(r.body(), 200));
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warning("Webhook: " + e.getMessage());
                return;
            }
        }
    }

    private static long retryAfterMillis(HttpResponse<String> r) {
        Matcher m = RETRY_AFTER.matcher(r.body() == null ? "" : r.body());
        if (m.find()) {
            try {
                return Math.min(10_000, (long) (Double.parseDouble(m.group(1)) * 1000) + 50);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1000;
    }

    private static String json(String username, String avatarUrl, String content) {
        return "{"
                + "\"username\":" + q(cut(username, 80)) + ","
                + (avatarUrl != null ? "\"avatar_url\":" + q(avatarUrl) + "," : "")
                + "\"content\":" + q(cut(content, 2000)) + ","
                + "\"allowed_mentions\":{\"parse\":[]}"
                + "}";
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
