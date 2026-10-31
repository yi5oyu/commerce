<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->

<!-- Procedure lives in the root AGENTS.md. This file holds only this folder's facts. -->

# frontend

## Commands

Run from `frontend/` (Node 24, see `.nvmrc`).

- Build: `npm run build`
- Lint: `npm run lint`
- Type check: `npx tsc --noEmit`
- Test: (none yet — no test runner installed)
- Run locally: `npm run dev` (unverified) — http://localhost:3000; `/api/*` is proxied to Spring at `localhost:8080` in development

## Structure

- Owns the commerce web UI
- Stack: Next.js 16 (App Router, Cache Components), React 19, TypeScript, Tailwind CSS 4
- Routes and layouts: `app/`
- Browser calls the backend via relative `/api/...`; server components call it directly via `API_INTERNAL_URL` (`.env.example`)
- `NEXT_PUBLIC_` prefix only for values safe to expose to the browser
