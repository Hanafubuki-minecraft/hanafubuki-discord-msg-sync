package com.hanafubuki.discord;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Голова игрока для аватарки в Discord — по его текущему скину на сервере (SkinsRestorer, Bedrock). */
public final class Heads {

    private static final Pattern SKIN_URL = Pattern.compile("\"url\"\\s*:\\s*\"https?://textures\\.minecraft\\.net/texture/([0-9a-fA-F]+)\"");

    private Heads() {
    }

    public static String avatar(Player p, String template) {
        String id = null;
        for (ProfileProperty prop : p.getPlayerProfile().getProperties()) {
            if (!"textures".equals(prop.getName())) continue;
            try {
                String json = new String(Base64.getDecoder().decode(prop.getValue()), StandardCharsets.UTF_8);
                Matcher m = SKIN_URL.matcher(json);
                if (m.find()) id = m.group(1);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return template.replace("{id}", id != null ? id : p.getName());
    }
}
