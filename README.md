# Mini-matrikkel: AI workshop

A small fullstack toy version of Matrikkelen (Ktor + SQLite + React/Bun) that we use to practise working with AI coding agents. All data is fictional.

The workshop uses **GitHub Copilot CLI inside [cplt](https://github.com/navikt/cplt)**, NAV's kernel-level sandbox. Claude Code works too (see below).

## Before the workshop (≈10 min, please do this in advance)

1. **Tools:** JDK 21, [Bun](https://bun.sh), git, [Copilot CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli) (run `copilot` once and log in), and cplt:
   ```bash
   brew install navikt/tap/cplt     # or: brew upgrade cplt
   ```
2. **Clone and approve the sandbox config:**
   ```bash
   git clone <repo-url> mini-matrikkel && cd mini-matrikkel
   cat .cplt.toml                   # read what the repo asks for
   cplt trust accept --all          # approve it (JVM attach + localhost ports 5180/8090)
   ```
3. **Check that everything builds inside the sandbox:**
   ```bash
   cplt exec -- backend/gradlew -p backend test
   cplt exec -c "cd frontend && bun install && bun test"
   ```
4. **Network allowlist (recommended).** By default cplt allows outbound HTTPS to any domain. For the workshop we turn on its fail-closed allowlist. Gradle needs one extra domain, and the file has to live **outside** the repo, because cplt refuses an allowlist the agent could edit:
   ```bash
   mkdir -p ~/.config/cplt && cp docs/workshop/allowed-domains.txt ~/.config/cplt/allowed-domains.txt
   ```
5. **Start the agent and check the skills:**
   ```bash
   cplt --with-proxy --default-allowlist --allowed-domains ~/.config/cplt/allowed-domains.txt
   ```
   This starts Copilot CLI in the sandbox. Type `/grill-me`. If it autocompletes, you're ready. `/skills` lists all of them.

## Skills

Matt Pocock's skills are **vendored** in [`.claude/skills/`](.claude/skills/README.md), so there's nothing to install. Copilot (CLI, cloud agent, VS Code and JetBrains agent mode) and Claude Code all read skills from that folder. Repo instructions are in `AGENTS.md`, which Copilot reads directly. `CLAUDE.md` just imports it.

**IntelliJ:** skills work in Copilot agent mode there too. That support is a preview feature: enable it under *Settings → GitHub Copilot → Chat → Agent*, and on a Business/Enterprise plan an admin must allow "Editor preview features". But **cplt does not sandbox the IntelliJ plugin**. Do the exercise in Copilot CLI under cplt, and use IntelliJ to read and review the diffs.

## Run the app

```bash
cd backend && ./gradlew seed && ./gradlew run    # API on http://127.0.0.1:8090
cd frontend && bun run dev                       # UI on http://localhost:5180
```

## The exercise (in pairs)

1. Pick a request from [`TICKETS.md`](TICKETS.md).
2. `/grill-me`: paste the request and answer the agent's questions until you're aligned. Notice the questions you hadn't thought about.
3. `/to-spec`: write the result as a spec in `.scratch/<feature>/spec.md`.
4. `/implement` (or just ask). For bigger tickets, ask for a plan first.
5. Make sure the backend and frontend tests are green and that the app does what you agreed.
6. `/code-review`: review your changes against the repo's conventions and your spec.

Stuck? The `solution` branch has reference implementations of tickets 1 and 2.

## Security setup in this repo

- **`.cplt.toml`** (committed, so cplt trusts it): it hides `MATRIKKEL_API_TOKEN` from the agent, and asks for JVM attach (Gradle) and localhost ports 5180/8090. Even without it, cplt blocks `.env` files, `~/.ssh`, cloud credentials and secret env vars. Run `cplt check` to see the protections in action.
- **`.claude/settings.json`**, for Claude Code users: Claude's own sandbox, a network allowlist, and deny rules for `.env`, `secrets/` and `git push`.

### Using Claude Code instead

```bash
cplt --agent claude --with-proxy --default-allowlist --allowed-domains ~/.config/cplt/allowed-domains.txt
```

The same skills, instructions and tickets apply. Claude's built-in `/code-review` and the vendored `code-review` skill have the same name. Use `/code-review` and check which one runs.
