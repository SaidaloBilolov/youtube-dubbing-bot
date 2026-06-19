# YouTube Subtitle-to-Dubbing Telegram Bot

A production-oriented Telegram bot that takes a YouTube link, downloads its
subtitles, translates them (Google Gemini Flash, timestamps preserved),
synthesizes Uzbek voice-over (Edge-TTS / XTTS v2), and merges the dubbed audio
back into the video (FFmpeg).

Built with **Spring Boot** following **Clean Architecture**, with heavy work
offloaded to a **RabbitMQ Producer-Consumer** pipeline so the bot stays responsive.

## Architecture

```
domain/          Pure business models, repository ports, exceptions (no framework)
application/     Use-cases (services), ports for external systems, DTOs
infrastructure/  Telegram, JPA persistence, RabbitMQ, AI adapters, process runner
python/          AI helpers invoked via subprocess (Gemini translate, TTS)
```

Dependencies point inward only: `infrastructure -> application -> domain`.

### Request flow

1. `BotController` receives a YouTube link and calls `VideoQueueService.enqueue` (returns immediately).
2. A `PENDING` `Video` row is persisted and a small `DubbingJob` is published to RabbitMQ **after commit**.
3. `DubbingConsumer` pulls the job; `DubbingService` runs the pipeline:
   `yt-dlp` (subs) -> Gemini (translate) -> Edge-TTS (voice) -> FFmpeg (mux).
4. On success the result video is sent back; on failure the user's minutes are refunded
   and the message is dead-lettered after retries.

## Prerequisites

- Java 17, Maven
- `ffmpeg`, `yt-dlp`, Python 3 with `python/requirements.txt`
- RabbitMQ, PostgreSQL (Neon in production)

## Local run

```bash
cp .env.example .env        # fill in tokens / Neon credentials
docker compose up -d        # RabbitMQ (+ local Postgres)
pip install -r python/requirements.txt
set -a && source .env && set +a
mvn spring-boot:run
```

Or fully containerized:

```bash
docker build -t dubbing-bot .
docker run --env-file .env dubbing-bot
```

## Configuration

All settings are environment-driven; see `.env.example` and
`src/main/resources/application.yml`. Neon cold-start is handled in the HikariCP
section (generous connection timeout, small warm pool, keepalive).

## Database schema

Managed by Flyway (`src/main/resources/db/migration`): `users`, `videos`, `transactions`.
