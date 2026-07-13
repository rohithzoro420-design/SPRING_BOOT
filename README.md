# SPRING_BOOT
# 📚 The Ledger — Reading Tracker (Microservices Demo)

A two-service Spring Boot project that demonstrates server-to-server communication with `RestTemplate`. **Server 1** owns a book catalog. **Server 2** lets a user track their reading progress against books pulled live from Server 1, and renders a small "ledger" web UI.

```
┌──────────────┐        HTTP         ┌──────────────────┐
│   Browser    │ ───────────────────▶│   Server 2        │
│ index.html   │ ◀───────────────────│  reading-tracker   │
└──────────────┘                     │  :8082             │
                                      └─────────┬──────────┘
                                                 │ RestTemplate
                                                 │ (CatalogClient)
                                                 ▼
                                      ┌──────────────────┐
                                      │   Server 1        │
                                      │  catalog-server    │
                                      │  :8081             │
                                      └──────────────────┘
```

- **Server 1 — `server1-catalog`** — owns the book catalog (title, author, genre, page count) and exposes it over REST.
- **Server 2 — `server2-tracker`** — stores reading progress in memory, calls Server 1 to validate books and pull catalog/genre data, and serves the `index.html` dashboard.

## Tech stack

- Java 17
- Spring Boot 3.3.2 (`spring-boot-starter-web`)
- Maven
- Vanilla JS / HTML / CSS front end (no build step)

## Project structure

```
.
├── server1-catalog/
│   ├── pom.xml
│   └── src/main/resources/application.properties   (server.port=8081)
│   └── src/main/java/com/example/catalog/...        (Book entity, controller, etc.)
│
└── server2-tracker/
    ├── pom.xml
    ├── src/main/resources/
    │   ├── application.properties                   (server.port=8082, catalog.server.url)
    │   └── static/index.html
    └── src/main/java/com/example/tracker/
        ├── TrackerApplication.java
        ├── BookDto.java
        ├── CatalogClient.java
        ├── ReadingController.java
        ├── ReadingProgress.java
        └── config/RestTemplateConfig.java
```

> **Note:** Only Server 2's source files are included in this snapshot. Server 1 is expected to expose the REST contract described below — recreate it (or drop in your own implementation) under `server1-catalog` before running the full stack.

## Server 1 — Catalog API (expected contract)

Server 2's `CatalogClient` calls the following endpoints on Server 1:

| Method | Path                  | Description                          |
|--------|-----------------------|---------------------------------------|
| GET    | `/books`               | List all books                       |
| GET    | `/books?genre={genre}` | List books filtered by genre         |
| GET    | `/books/{id}`          | Get a single book by id (404 if missing) |

Each book returned should match the `BookDto` shape:

```json
{
  "id": 1,
  "title": "Dune",
  "author": "Frank Herbert",
  "genre": "Sci-Fi",
  "pages": 412
}
```

## Server 2 — Reading Tracker API

Base path: `/reading`

| Method | Path                              | Description                                                  |
|--------|------------------------------------|----------------------------------------------------------------|
| POST   | `/reading/start/{bookId}`          | Start tracking a book (validates it exists via Server 1)      |
| POST   | `/reading/progress/{bookId}?pagesRead=N` | Log pages read for a tracked book                        |
| POST   | `/reading/finish/{bookId}?rating=1-5`    | Mark a book finished and rate it                          |
| GET    | `/reading/all`                     | List all tracked books and their progress                     |
| GET    | `/reading/catalog`                 | Proxy of Server 1's full catalog (avoids CORS in the browser)  |
| GET    | `/reading/recommendations`         | Recommends unread books in genres rated ≥ 4★                  |
| GET    | `/reading/stats`                   | Aggregate stats: books tracked, finished, pages read, avg rating |

Reading progress is stored **in-memory** (`ConcurrentHashMap`), so it resets on restart.

## Getting started

### 1. Clone and build

```bash
git clone <your-repo-url>
cd <your-repo>
```

### 2. Run Server 1 (catalog) — port 8081

```bash
cd server1-catalog
./mvnw spring-boot:run
```

### 3. Run Server 2 (tracker) — port 8082

```bash
cd server2-tracker
./mvnw spring-boot:run
```

Server 2's `application.properties` should point at Server 1, e.g.:

```properties
server.port=8082
spring.application.name=server2-tracker
catalog.server.url=http://localhost:8081
```

### 4. Open the app

Visit **http://localhost:8082** in your browser. The dashboard will:

- Load your currently tracked books ("Currently on the desk")
- Pull the live catalog from Server 1 ("Start a new book")
- Show recommendations once you've rated a finished book 4★ or higher

## How it works

1. The browser talks **only** to Server 2 (`/reading/...`).
2. Server 2's `CatalogClient` makes outbound `RestTemplate` calls to Server 1 whenever it needs book metadata — starting a book, browsing the catalog, or generating recommendations by genre.
3. Server 2 caches a snapshot of each book's title/genre/page count on `ReadingProgress` at the moment tracking starts, so progress calculations don't require hitting Server 1 again.

## Possible next steps

- Persist `ReadingProgress` to a database instead of an in-memory map
- Add resilience (timeouts/circuit breaker) around the `CatalogClient` HTTP calls
- Add authentication if this moves beyond a local demo
- Write integration tests that spin up both services (e.g. with Testcontainers or WireMock for Server 1)

## License

Add a license of your choice (MIT is a common default for demo projects).
