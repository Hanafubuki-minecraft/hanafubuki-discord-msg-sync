package com.hanafubuki.discord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** messages.yml: game.* — MiniMessage для игры, discord.* — Markdown для Discord. */
public final class Messages {

    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private YamlConfiguration lang = new YamlConfiguration();
    private TagResolver style = Style.resolver(null);

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File f = new File(plugin.getDataFolder(), "messages.yml");
        if (!f.exists()) plugin.saveResource("messages.yml", false);
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        InputStream in = plugin.getResource("messages.yml");
        if (in != null) y.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
        lang = y;
        style = Style.resolver(plugin.getConfig().getConfigurationSection("style"));
    }

    public String raw(String key) {
        return lang.getString(key, "");
    }

    /** Текст для Discord с подставленными значениями: discord.join, {name} → ник. */
    public String discord(String key, String... kv) {
        String s = raw("discord." + key);
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace("{" + kv[i] + "}", kv[i + 1]);
        return s;
    }

    public Component game(String key, TagResolver... r) {
        TagResolver[] all = new TagResolver[r.length + 1];
        all[0] = style;
        System.arraycopy(r, 0, all, 1, r.length);
        return mm.deserialize(raw("game." + key), all);
    }

    public void send(CommandSender to, String key, TagResolver... r) {
        to.sendMessage(game(key, r));
    }

    public static TagResolver ph(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }
}
