package com.hanafubuki.discord;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;

/** Игра → Discord: чат от имени игрока с его головой, системное — тихими строками от «Hanafubuki». */
public final class GameListener implements Listener {

    private final DiscordSyncPlugin plugin;

    public GameListener(DiscordSyncPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean on(String event) {
        return plugin.getConfig().getBoolean("events." + event, false);
    }

    private static String plain(Component c) {
        return PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(c, Locale.of("ru", "RU")));
    }

    /** Ник без Markdown-разметки (у Bedrock-ников бывает «_»). */
    private static String md(String s) {
        return s.replace("\\", "\\\\").replace("_", "\\_").replace("*", "\\*").replace("~", "\\~").replace("`", "\\`").replace("|", "\\|");
    }

    /** Чат уже асинхронный; здесь только готовим текст и кладём в очередь — сеть в отдельном потоке. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!on("chat")) return;
        Player p = e.getPlayer();
        String avatar = Heads.avatar(p, plugin.getConfig().getString("discord.player-avatar", "https://mc-heads.net/avatar/{id}/64"));
        plugin.webhook().send(p.getName(), avatar, PlainTextComponentSerializer.plainText().serialize(e.message()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore() && on("first-join")) plugin.system(plugin.messages().discord("first-join", "name", md(p.getName())));
        else if (on("join-quit")) plugin.system(plugin.messages().discord("join", "name", md(p.getName())));
        plugin.updatePresenceLater();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        if (on("join-quit")) plugin.system(plugin.messages().discord("quit", "name", md(e.getPlayer().getName())));
        plugin.updatePresenceLater();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!on("deaths") || e.deathMessage() == null) return;
        plugin.system(plugin.messages().discord("death", "message", plain(e.deathMessage())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        if (!on("advancements")) return;
        var display = e.getAdvancement().getDisplay();
        if (display == null || !display.doesAnnounceToChat()) return;
        plugin.system(plugin.messages().discord("advancement",
                "name", md(e.getPlayer().getName()), "advancement", plain(display.title())));
    }
}
