# Mini-matrikkel

A toy version of Matrikkelen (the Norwegian cadastre). It's used for an AI workshop. All data is fictional.

## Layout

- `backend/`: Ktor (Kotlin, JVM 21), SQLite, Flyway for schema, kotliquery for SQL.
  - `src/main/kotlin/no/kartverket/workshop/`: `Application.kt` (wiring), `Routes.kt` (HTTP), `MatrikkelRepository.kt` (all SQL), `Models.kt` (DTOs + `formatMatrikkelnummer`).
  - `src/main/resources/db/migration/`: Flyway migrations. **Schema only, never data.**
  - `src/test/kotlin/.../DevData.kt`: fictional dev data. Tests call `seedDevData`, and `./gradlew seed` runs it against the local DB.
- `frontend/`: React + TypeScript on Bun, built with Vite. `src/api.ts` is the HTTP client, `src/format.ts` holds pure formatting helpers, and `src/App.tsx` holds the UI.

## Commands

```bash
cd backend && ./gradlew test      # backend tests
cd backend && ./gradlew seed      # create + seed mini-matrikkel.db
cd backend && ./gradlew run       # API on http://127.0.0.1:8090
cd frontend && bun install
cd frontend && bun test           # frontend tests
cd frontend && bun run typecheck
cd frontend && bun run dev        # UI on http://localhost:5180 (proxies /api to 8090)
```

## Conventions

- Use domain terms from `GLOSSARY.md` in code, API and UI. Keep the Norwegian names (`matrikkelenhet`, `eierforhold`, `gardsnummer`) and don't translate them.
- Write SQL with kotliquery `queryOf(...)` and named parameters. kotliquery only recognises parameter names with **2+ characters** (`:id` works, `:q` silently doesn't).
- Every schema change is a new Flyway migration `V<n>__<description>.sql`. Never edit an existing migration.
- Test data changes go in `DevData.kt`, never in a migration.
- Backend tests are HTTP-level tests in `MatrikkelApiTest.kt` that use `testApplication` and a fresh seeded temp DB. Add a test for every new endpoint or behaviour.
- Run backend and frontend tests before saying something is done.

## Agent skills

### Issue tracker

Issues and specs live as local markdown under `.scratch/`. Raw feature requests are in `TICKETS.md`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
