package com.hanafubuki.discord;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

import static com.hanafubuki.discord.Messages.ph;

public final class DiscordSyncPlugin extends JavaPlugin {

    private Messages messages;
    private Webhook webhook;
    private Bot bot;
    private volatile boolean announcedStart;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        messages = new Messages(this);
        messages.load();
        webhook = new Webhook(getLogger());

        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        CommandExecutor admin = (CommandSender s, Command c, String label, String[] a) -> {
            String sub = a.length == 0 ? "" : a[0].toLowerCase(Locale.ROOT);
            if (sub.equals("reload")) {
                reloadConfig();
                messages.load();
                String manual = getConfig().getString("discord.webhook-url", "");
                if (manual != null && !manual.isBlank()) webhook.setUrl(manual);
                updatePresenceLater();
                messages.send(s, "reloaded");
            } else if (sub.equals("status")) {
                messages.send(s, "status", ph("webhook", webhook.ready() ? "✔" : "✘"),
                        ph("version", getPluginMeta().getVersion()));
            } else {
                messages.send(s, "usage", ph("label", label));
            }
            return true;
        };
        Commands.register(this, "admin", "Hanafubuki Discord sync: reload, status", "hanafubuki.discord.admin",
                List.of("ds"), admin, (s, c, l, a) -> a.length == 1
                        ? List.of("reload", "status").stream().filter(x -> x.startsWith(a[0].toLowerCase(Locale.ROOT))).toList()
                        : List.of());

        bot = new Bot(this);
        bot.start();
        // онлайн в статусе бота — раз в 30 секунд, если поменялся (лимиты Discord)
        Bukkit.getScheduler().runTaskTimer(this, this::updatePresence, 600L, 600L);
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("events.start-stop", true) && webhook != null && webhook.ready()) {
            webhook.sendNow(systemName(), systemAvatar(), messages.discord("server-stop"));
        }
        if (webhook != null) webhook.shutdown();
        if (bot != null) bot.shutdown();
    }

    /** Вебхук готов (найден, создан или задан вручную) — объявляем запуск сервера один раз. */
    void onWebhookReady() {
        if (!announcedStart && getConfig().getBoolean("events.start-stop", true)) {
            announcedStart = true;
            system(messages.discord("server-start"));
        }
        updatePresenceLater();
    }

    public void system(String text) {
        webhook.send(systemName(), systemAvatar(), text);
    }

    private String systemName() {
        return getConfig().getString("discord.system-name", "Hanafubuki");
    }

    private String systemAvatar() {
        String a = getConfig().getString("discord.system-avatar", "");
        return a == null || a.isBlank() ? null : a;
    }

    private int lastOnline = -1;

    void updatePresenceLater() {
        lastOnline = -1;
        Bukkit.getScheduler().runTaskLater(this, this::updatePresence, 40L);
    }

    private void updatePresence() {
        int online = Bukkit.getOnlinePlayers().size();
        if (online == lastOnline) return;
        lastOnline = online;
        if (bot != null) bot.presence(online, Bukkit.getMaxPlayers());
    }

    public Messages messages() {
        return messages;
    }

    public Webhook webhook() {
        return webhook;
    }
}
