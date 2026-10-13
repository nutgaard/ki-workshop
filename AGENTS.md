# Mini-matrikkel

A toy version of Matrikkelen (the Norwegian cadastre). It's used for an AI workshop. All data is fictional.

## Layout

- `backend/`: Ktor (Kotlin, JVM 21), SQLite, Flyway for schema, kotliquery for SQL.
  - `src/main/kotlin/no/kartverket/workshop/`: `Application.kt` (wiring), `Routes.kt` (HTTP), `MatrikkelRepository.kt` (all SQL), `Models.kt` (DTOs + `formatMatrikkelnummer`).
  - `src/main/resources/db/migration/`: Flyway migrations. **Schema only, never data.**
  - `src/test/kotlin/.../DevData.kt`: fictional dev data. Tests call `seedDevData`, and `./gradlew seed` runs it against the local DB.
- `frontend/`: React + TypeScript on Bun, built with Vite. `src/api.ts` is the HTTP client, `src/format.ts` holds pure formatting helpers, and `src/App.tsx` holds the UI.

## Sandbox

Agents in this repo run inside [cplt](https://github.com/navikt/cplt), and its config is in `.cplt.toml`. Some things are blocked by design: reading `.env`, environment secrets, and network access to anything but the allowlist. If a command fails with "Operation not permitted" or a 403 from the proxy, report it. Don't work around it.

Shell commands may start in the repo root, so use `backend/gradlew -p backend ...` or `cd backend && ...` in the same shell command.

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

Skills come from [mattpocock/skills](https://github.com/mattpocock/skills) and are managed with [APM](https://github.com/microsoft/apm). `apm.yml` declares them, `apm.lock.yaml` pins the exact commit and content hashes, and `apm install` deploys them to `.agents/skills/` (Copilot) and `.claude/skills/` (Claude Code). Don't edit the deployed skill files by hand, because `apm audit` reports that as drift. Change `apm.yml` and run `apm install` instead.

### Issue tracker

Issues and specs live as local markdown under `.scratch/`. Raw feature requests are in `TICKETS.md`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
