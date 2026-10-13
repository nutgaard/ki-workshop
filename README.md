# Mini-matrikkel: AI workshop

A small fullstack toy version of Matrikkelen (Ktor + SQLite + React/Bun) that we use to practise working with Claude Code. All data is fictional.

## Before the workshop (≈10 min, please do this in advance)

1. **Tools:** JDK 21, [Bun](https://bun.sh), git, and [Claude Code](https://code.claude.com) (logged in).
2. **Clone and check that everything runs:**
   ```bash
   git clone <repo-url> mini-matrikkel && cd mini-matrikkel
   (cd backend && ./gradlew test)
   (cd frontend && bun install && bun test)
   ```
3. **Install Matt Pocock's skills** (they're already enabled in `.claude/settings.json`):
   ```bash
   claude plugin install mattpocock-skills@claude-plugins-official --scope project
   ```
4. Start `claude` in the repo root and accept the folder trust prompt. Type `/grill-me`. If it autocompletes, you're ready.

## Run the app

```bash
cd backend && ./gradlew seed && ./gradlew run    # API on http://127.0.0.1:8090
cd frontend && bun run dev                       # UI on http://localhost:5180
```

## The exercise (in pairs)

1. Pick a request from [`TICKETS.md`](TICKETS.md).
2. `/grill-me`: paste the request and answer Claude's questions until you're aligned. Notice the questions you hadn't thought about.
3. Ask Claude to write the result as a spec in `.scratch/<feature>/spec.md` (or use `/to-spec`).
4. Ask Claude to implement it. Use plan mode (`shift+tab`) first if it's a big one.
5. Make sure `./gradlew test` and `bun test` are green and that the app does what you agreed.
6. Run `/code-review` on your changes.

Stuck? The `solution` branch has reference implementations of tickets 1 and 2.

## Security setup in this repo

`.claude/settings.json` turns on the **sandbox**. Bash commands run with filesystem and network isolation, and only the package registries we need are reachable. It also **denies** reading `.env` and `secrets/`, and blocks `git push`. Run `/sandbox` inside Claude to see the active config.
