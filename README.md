# Chatbot Platform

Spring Boot backend ve React + TypeScript frontend içeren, JWT ile kimlik doğrulama yapan, içerik filtreleme ve outbox tabanli event yayini barindiran bir chatbot platformu.

## Proje Açıklaması

Bu proje kullanicilarin kayit olup giris yapabildigi, sohbet baslatabildigi, mesaj gecmisini gorebildigi ve admin rolundeki kullanicilarin icerik filtre kurallarini yonetebildigi tam yigin bir chatbot uygulamasidir. Backend PostgreSQL, RabbitMQ ve Ollama ile calisir. Frontend Vite tabanlidir ve yerel oturum listesini tarayici `localStorage` uzerinde tutar.

## Teknolojiler

- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway, RabbitMQ, JWT, springdoc-openapi
- Frontend: React 19, TypeScript, Vite, TanStack Query, React Router, Tailwind CSS 4, Radix UI, Sonner
- Veritabani ve servisler: PostgreSQL 17, RabbitMQ Management, Ollama

## Gereksinimler

- Java 21
- Node.js 20+ ve npm
- Docker Desktop veya ayri kurulu PostgreSQL, RabbitMQ ve Ollama
- Windows kullanicilari icin `mvnw.cmd` calistirmak isterseniz PowerShell erisimi
- Alternatif olarak kurulu bir Maven surumu

## Kurulum

1. Ornek ortam dosyasini olusturun:

```bash
copy .env.example .env
copy frontend\.env.example frontend\.env
```

2. Gerekirse `JWT_SECRET` degerini degistirin.
3. Altyapi servislerini baslatin:

```bash
docker compose up -d
```

4. Ollama icinde kullanacaginiz modeli ayri olarak indirin:

```bash
ollama pull llama3.2:3b
```

5. Frontend bagimliliklarini yukleyin:

```bash
cd frontend
npm install
```

## Backend Çalıştırma Adımları

`application.properties` dosyasina gore backend varsayilan olarak `http://localhost:8080` uzerinden calisir.

Maven kuruluysa:

```bash
mvn spring-boot:run
```

Wrapper ile:

```bash
mvnw.cmd spring-boot:run
```

Not: Bu workspace icinde `mvnw.cmd` komutu PowerShell bulunmadigi icin calismadi. Windows ortaminda wrapper kullanacaksaniz PowerShell erisimi gerekli.

## Frontend Çalıştırma Adımları

Frontend `frontend/` altindadir ve Vite dev server varsayilan olarak `http://localhost:5173` portunu kullanir.

```bash
cd frontend
npm run dev
```

Build almak icin:

```bash
npm run build
```

Lint kontrolu icin:

```bash
npm run lint
```

## Environment Variables

Kok `.env`:

- `SPRING_APPLICATION_NAME`
- `DB_URL`
- `DB_USER`
- `DB_PASS`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `RABBIT_HOST`
- `RABBIT_PORT`
- `RABBIT_USER`
- `RABBIT_PASS`
- `FLYWAY_ENABLED`
- `LLM_BASE_URL`
- `LLM_MODEL`
- `LLM_TIMEOUT_SECONDS`
- `JWT_SECRET`
- `JWT_EXPIRES_MINUTES`
- `EVENTS_EXCHANGE`
- `EVENTS_QUEUE`
- `OUTBOX_BATCH_SIZE`
- `OUTBOX_MAX_ATTEMPTS`
- `OUTBOX_INITIAL_DELAY`
- `OUTBOX_POLL_INTERVAL_MS`
- `CORS_ALLOWED_ORIGINS`

`frontend/.env`:

- `VITE_API_BASE_URL`
  - Bos birakilabilir. `vite.config.ts` icindeki proxy sayesinde `/api` istekleri `http://localhost:8080` adresine yonlenir.

## Proje Yapısı

```text
chatbot-platform/
|- docker-compose.yml
|- pom.xml
|- src/
|  |- main/java/com/aryil/chatbot/
|  |  |- auth/        # JWT, login, register, user modeli
|  |  |- chat/        # conversation, message, chat service/controller
|  |  |- common/      # security, exception, locale, openapi, config
|  |  |- events/      # rabbit config, outbox entity/repository/publisher
|  |  |- guard/       # content filtering
|  |  |- llm/         # Ollama istemcisi ve properties
|  |  |- policy/      # admin pattern yonetimi
|  |- main/resources/
|  |  |- application.properties
|  |  |- db/migration/
|- frontend/
|  |- package.json
|  |- vite.config.ts
|  |- src/
|  |  |- api/
|  |  |- components/
|  |  |- hooks/
|  |  |- lib/
|  |  |- pages/
```

## Uygulama Akışı

1. Kullanici `POST /api/auth/register` veya `POST /api/auth/login` ile JWT alir.
2. Frontend tokeni `localStorage` icinde `cb_token` anahtariyla tutar.
3. `POST /api/chat` yeni sohbet baslatir veya mevcut `conversationId` ile devam eder.
4. `ContentGuardService` kullanici mesajini kontrol eder.
5. Uygun mesajlar Ollama istemcisine gider, cevap tekrar filtrelenir.
6. Mesajlar PostgreSQL uzerinde saklanir, event kaydi `outbox_events` tablosuna yazilir.
7. `OutboxPublisher` zamanlanmis sekilde bekleyen eventleri RabbitMQ exchange'ine gonderir.
8. Admin kullanicisi `api/admin/patterns` endpointleriyle filtre kurallarini yonetir.

## API Endpoint Özeti

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`

Chat:

- `POST /api/chat`
- `GET /api/conversations/{id}/messages`

Admin:

- `GET /api/admin/patterns`
- `POST /api/admin/patterns`
- `PATCH /api/admin/patterns/{id}`
- `DELETE /api/admin/patterns/{id}`
- `PUT /api/admin/patterns/{id}/toggle?enabled=true|false`

Dokumantasyon:

- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`

## Sık Karşılaşılan Hatalar

- `JWT secret is missing`:
  - `.env` icinde `JWT_SECRET` tanimli degilse uygulama acilmaz.
- `Cannot start maven from wrapper`:
  - Bu Windows wrapper surumu PowerShell cagiriyor. PowerShell yoksa kurulu Maven kullanin.
- Frontend login olurken 401:
  - Demo kullanicilar migration `V5__fix_demo_users.sql` ile `admin@local.dev / admin123` ve `user@local.dev / user123` olarak eklenir.
- Chat cevap vermiyor:
  - `docker compose up -d` sonrasi Ollama modeli ayrica cekilmemisse `LLM_MODEL` istegi basarisiz olur.
- CORS hatasi:
  - Frontend farkli origin uzerinden aciliyorsa `CORS_ALLOWED_ORIGINS` guncellenmelidir.

## Geliştirme Notları

- Frontend kaynak disi klasorler olan `frontend/node_modules` ve `frontend/dist` repoya dahil edilmemelidir.
- `frontend/src/hooks/use-sessions.ts` sadece local oturum basliklarini tutar; backend tarafinda sohbet listeleme endpointi henuz yoktur.
- `pom.xml` icinde gereksiz/tekrarlayan starter bagimliliklari temizlendi; testler icin standart `spring-boot-starter-test` ve `spring-security-test` yeterlidir.
- Genel API hata yapisi `ApiError` record'u ile standardize edilmistir.
- Local denemeler icin Swagger uzerinden auth alip korumali endpointleri test etmek kolaydir.
