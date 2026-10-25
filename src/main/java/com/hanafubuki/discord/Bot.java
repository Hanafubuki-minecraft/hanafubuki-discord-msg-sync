package com.hanafubuki.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import static com.hanafubuki.discord.Messages.ph;

/**
 * Бот нужен только чтобы читать канал (Discord → игра) и один раз найти/создать вебхук.
 * Всё, что уходит в Discord, отправляется вебхуком — с именем и головой игрока.
 */
public final class Bot extends ListenerAdapter {

    public static final String WEBHOOK_NAME = "Hanafubuki";

    private final DiscordSyncPlugin plugin;
    private JDA jda;
    private long channelId;

    public Bot(DiscordSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String token = plugin.getConfig().getString("discord.token", "");
        channelId = plugin.getConfig().getLong("discord.channel-id", 0);
        if (token == null || token.isBlank() || channelId == 0) {
            plugin.getLogger().warning("discord.token / discord.channel-id are not set in config.yml — the bridge is off");
            return;
        }
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();
        } catch (Exception e) {
            plugin.getLogger().severe("Could not start the Discord bot: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (jda != null) jda.shutdownNow();
    }

    @Override
    public void onReady(ReadyEvent e) {
        String manual = plugin.getConfig().getString("discord.webhook-url", "");
        if (manual != null && !manual.isBlank()) {
            plugin.webhook().setUrl(manual);
            plugin.onWebhookReady();
            return;
        }
        TextChannel ch = jda.getTextChannelById(channelId);
        if (ch == null) {
            plugin.getLogger().warning("Discord channel " + channelId + " not found or the bot can't see it");
            return;
        }
        long self = jda.getSelfUser().getIdLong();
        ch.retrieveWebhooks().queue(list -> {
            var own = list.stream()
                    .filter(w -> WEBHOOK_NAME.equals(w.getName()) && w.getOwnerAsUser() != null && w.getOwnerAsUser().getIdLong() == self)
                    .findFirst();
            if (own.isPresent()) {
                plugin.webhook().setUrl(own.get().getUrl());
                plugin.onWebhookReady();
            } else {
                ch.createWebhook(WEBHOOK_NAME).queue(w -> {
                    plugin.webhook().setUrl(w.getUrl());
                    plugin.getLogger().info("Created webhook \"" + WEBHOOK_NAME + "\" in #" + ch.getName());
                    plugin.onWebhookReady();
                }, err -> plugin.getLogger().severe("Could not create a webhook — give the bot the Manage Webhooks permission: " + err.getMessage()));
            }
        }, err -> plugin.getLogger().severe("Could not read webhooks — give the bot the Manage Webhooks permission: " + err.getMessage()));
    }

    /** Discord → игра. Свои же сообщения (вебхук) и других ботов пропускаем. */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (!plugin.getConfig().getBoolean("events.discord-to-game", true)) return;
        if (e.getChannel().getIdLong() != channelId) return;
        if (e.isWebhookMessage() || e.getAuthor().isBot()) return;

        String name = e.getMember() != null ? e.getMember().getEffectiveName() : e.getAuthor().getEffectiveName();
        Message m = e.getMessage();
        String text = m.getContentDisplay().replace('\n', ' ').trim();
        if (!m.getAttachments().isEmpty()) text = (text + " " + plugin.messages().raw("game.attachment")).trim();
        if (text.isEmpty()) return;
        final String msg = text.length() > 256 ? text.substring(0, 256) + "…" : text;

        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.broadcast(plugin.messages().game("from-discord", ph("name", name), ph("message", msg))));
    }

    /** Статус бота: «Играет: 3/20 на Hanafubuki». */
    public void presence(int online, int max) {
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) return;
        if (!plugin.getConfig().getBoolean("discord.status", true)) return;
        String text = plugin.messages().discord("status", "online", String.valueOf(online), "max", String.valueOf(max));
        jda.getPresence().setActivity(Activity.playing(text));
    }
}
