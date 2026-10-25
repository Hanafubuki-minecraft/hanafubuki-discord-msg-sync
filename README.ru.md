<div align="center">

# HanafubukiDiscordSync

**Мост чата Minecraft ↔ Discord для Hanafubuki — каждый игрок пишет со своим ником и головой.**

[English](README.md) · Русский

</div>

## Как работает

- **Minecraft → Discord** идёт через **вебхук**: у каждого сообщения ник игрока и голова его *текущего* скина (включая SkinsRestorer и Bedrock), поэтому канал выглядит как живой разговор, а не как один бот.
- **Системные строки** — сервер запущен/остановлен, тихие входы и выходы, тёплое «впервые на сервере» — от «Hanafubuki» с логотипом. Смерти и достижения по умолчанию выключены.
- **Discord → Minecraft** требует бота (вебхук не умеет читать). Сообщения в игре — `◆ Ник › текст` в палитре сервера.
- Бот показывает онлайн в статусе и **сам создаёт вебхук** (нужно право *Управление вебхуками*), так что в конфиге — только токен и ID канала.
- Всё общение с Discord идёт в отдельном потоке `HanafubukiDiscord-Webhook`: чат и тики сервера только кладут сообщение в очередь и никогда не ждут Discord; отправка последовательная, с учётом лимитов (429 → подождать и повторить).
- JDA не вшит: Paper скачивает его при первом запуске, jar весит килобайты.

## Настройка

1. [Discord Developer Portal](https://discord.com/developers/applications) → приложение → **Bot**: включи **Message Content Intent**, скопируй токен.
2. Пригласи бота с правами: *Просмотр канала*, *Отправка сообщений*, *Чтение истории*, *Управление вебхуками*.
3. `plugins/HanafubukiDiscordSync/config.yml`: укажи `discord.token` и `discord.channel-id`, перезапусти сервер.

## Команды

| Команда | Описание | Право |
|---|---|---|
| `/discordsync reload` (`/ds`) | Перезагрузить конфиг и тексты | `hanafubuki.discord.admin` (op) |
| `/discordsync status` | Версия и состояние вебхука | `hanafubuki.discord.admin` |

Имя команды и алиасы — `config.yml` → `commands`. Палитра — `config.yml` → `style`. Тексты — `messages.yml`.

## Релизы

Поднимаешь `version` в [`gradle.properties`](gradle.properties) и пушишь в `master`.

## Лицензия

[GNU General Public License v3.0](LICENSE) © ByteMe6
