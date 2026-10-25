<div align="center">

# HanafubukiDiscordSync

**Minecraft ↔ Discord chat bridge for Hanafubuki — every player posts with their own name and head.**

[![Build](https://github.com/Hanafubuki-minecraft/hanafubuki-discord-msg-sync/actions/workflows/release.yml/badge.svg)](https://github.com/Hanafubuki-minecraft/hanafubuki-discord-msg-sync/actions/workflows/release.yml)
[![Release](https://img.shields.io/github/v/release/Hanafubuki-minecraft/hanafubuki-discord-msg-sync)](https://github.com/Hanafubuki-minecraft/hanafubuki-discord-msg-sync/releases/latest)
[![License: GPL v3](https://img.shields.io/github/license/Hanafubuki-minecraft/hanafubuki-discord-msg-sync)](LICENSE)

English · [Русский](README.ru.md)

</div>

## How it works

- **Minecraft → Discord** goes through a **webhook**: each message is posted with the player's name and the head of their *current* skin (SkinsRestorer and Bedrock skins included), so the channel reads like a real conversation instead of one bot.
- **System lines** — server started/stopped, quiet join/leave, a warm "first time here" — come from "Hanafubuki" with the logo. Deaths and advancements are off by default.
- **Discord → Minecraft** needs a bot (webhooks can't read). Messages appear as `◆ Name › text` in the server palette.
- The bot shows the player count in its status and **creates the webhook itself** (needs *Manage Webhooks*), so the config only needs a token and a channel ID.
- JDA isn't bundled: Paper downloads it on first start, so the jar is tiny.

## Setup

1. [Discord Developer Portal](https://discord.com/developers/applications) → your app → **Bot**: enable **Message Content Intent**, copy the token.
2. Invite the bot with: *View Channel*, *Send Messages*, *Read Message History*, *Manage Webhooks*.
3. `plugins/HanafubukiDiscordSync/config.yml`: set `discord.token` and `discord.channel-id`, restart.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/discordsync reload` (`/ds`) | Reload config and texts | `hanafubuki.discord.admin` (op) |
| `/discordsync status` | Version and webhook status | `hanafubuki.discord.admin` |

Command name and aliases: `config.yml` → `commands`. Palette: `config.yml` → `style`. Texts: `messages.yml`.

## Releasing

Bump `version` in [`gradle.properties`](gradle.properties) and push to `master`.

## License

[GNU General Public License v3.0](LICENSE) © ByteMe6
