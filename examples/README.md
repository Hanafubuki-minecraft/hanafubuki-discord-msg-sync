# HanafubukiDiscordSync: примеры конфигурации

Мост чата Minecraft ↔ Discord: каждый игрок пишет со своим ником и головой, системные строки — тихие.

## Файлы

| Файл | Что это |
|---|---|
| [`config.full.yml`](config.full.yml) | Полный `config.yml` по умолчанию со всеми комментариями |
| [`minimal-setup.yml`](minimal-setup.yml) | Минимальная настройка: токен бота и канал |
| [`feed-only.yml`](feed-only.yml) | Только лента из игры в Discord, без сообщений из Discord в игру |
| [`loud-events.yml`](loud-events.yml) | Всё подряд: смерти и достижения тоже |
| [`own-webhook.yml`](own-webhook.yml) | Свой вебхук вместо автоматического и другие головы игроков |

## Как пользоваться

Готовые примеры — это **кусочки** конфига. Перенеси нужные строки в `plugins/HanafubukiDiscordSync/config.yml` (не заменяй весь файл), затем выполни `/discordsync reload` (токен и канал — после рестарта).

## Все настройки

Таблица собрана из комментариев самого `config.yml`, поэтому совпадает с текущей версией плагина.

| Ключ | По умолчанию | Что делает |
|---|---|---|
| `discord.token` | `""` | Bot token (Discord Developer Portal → your app → Bot → Reset Token). Keep it secret. |
| `discord.channel-id` | `0` | ID of the channel to sync (Discord: Developer Mode → right click the channel → Copy ID) |
| `discord.webhook-url` | `""` | Webhook for sending to Discord. Empty — the bot finds or creates one named "Hanafubuki" itself (the bot needs the "Manage Webhooks" permission in that channel) |
| `discord.status` | `true` | Bot status with the player count: "Playing 3/20 on Hanafubuki" |
| `discord.system-name` | `"Hanafubuki"` | Name and avatar for system messages (start/stop, joins) |
| `discord.system-avatar` | `"https://raw.githubusercontent.com/Hanafubuki-minecraft/hanafubuki-brand/main/export/discord/hanafubuki-discord-icon-512.png"` | Аватарка системных сообщений (ссылка на картинку) |
| `discord.player-avatar` | `"https://mc-heads.net/avatar/{id}/64"` | Player avatar: {id} — the texture of the player's current skin (or the nickname if there is none) |
| `events.chat` | `true` | Minecraft chat → Discord, each player with their own name and head |
| `events.discord-to-game` | `true` | Discord channel → Minecraft chat |
| `events.start-stop` | `true` | "Server started / stopped" |
| `events.join-quit` | `true` | Quiet join/leave lines |
| `events.first-join` | `true` | "Welcome X — first time on Hanafubuki!" |
| `events.deaths` | `false` | Сообщения о смертях в Discord |
| `events.advancements` | `false` | Достижения в Discord (с BACAP их очень много — по умолчанию выключено) |
| **`commands`** | | Commands: name, aliases, enabled. Restart to apply. |
| `commands.admin` | `{ enabled: true, name: discordsync, aliases: [ds] }` | Команда: `enabled` — включена ли, `name` — имя, `aliases` — алиасы. Меняется после рестарта |
| **`style`** | | Palette of in-game messages (tags in messages.yml → game: <accent> <text> <muted> <dim> <success> <error> <gold>, <symbol>) Hanafubuki style: https://github.com/Hanafubuki-minecraft/hanafubuki-brand/blob/main/STYLE.md |
| `style.symbol` | `"❀"` | Символ в префиксе сообщений (тег `<symbol>`) |
| `style.accent` | `"#F7A1C0"` | Акцент — сакура (тег `<accent>`) |
| `style.text` | `"#F4EEF6"` | Основной текст (`<text>`) |
| `style.muted` | `"#C9BED3"` | Приглушённый текст (`<muted>`) |
| `style.dim` | `"#9488A6"` | Тихий, служебный текст (`<dim>`) |
| `style.success` | `"#A8E6B5"` | Успех (`<success>`) |
| `style.error` | `"#FF8A9B"` | Ошибка (`<error>`) |
| `style.gold` | `"#FFCF6B"` | Золото, редкие акценты (`<gold>`) |
