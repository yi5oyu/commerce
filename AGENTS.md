# AGENTS.md

## Procedure

A plan (ep) = `docs/plans/active/ep-{YYYYMMDD}-{slug}.md` — created by copying
`ep-0000-template.md`. **The plan file is the only state.**

Create an ep only after a source change is agreed in conversation — never during
questions, investigation, or discussion. Once it exists, anything inside
`docs/plans/` is written without approval.

1. **Plan** — fill the template with what was agreed in conversation. Read the
   relevant code; `## Acceptance Criteria` must be decidable. If unclear, ask in
   conversation before the ep.
2. **Approve** — present the plan path, files, criteria, and the branch to work
   on, then wait. Branch: on a feature branch, continue it; on the default
   branch, propose a new feature branch — split into another feature only when
   the human says so. **No tool calls while waiting.** On approval, copy the
   files, criteria, and branch into `## Approval` and set Status to
   `[Approved]`.
3. **Implement** — only approved files, only as much as the criteria need.
   One ep at a time. Right before the first source change, if there are
   uncommitted changes outside `docs/plans/`, stop and show them — keeping or
   discarding them is the human's call. Then create or switch to the approved
   branch, record it in `**Branch:**`, set Status to `[In Progress]`, and
   append one `## Change Log` line immediately per change. Never commit on the
   default branch.
4. **Verify** — check the criteria in `## Approval` one by one, tick them in
   `## Acceptance Criteria`, and present pass/fail together with a proposed
   commit message. Human approval — it covers the commit and the push. Or a
   review request: set Status to `[In Review]` and wait for `review` in a fresh
   session. On `APPROVED`, present again for approval; on `NEEDS_REVISION`, set
   it back to `[In Progress]`, fix within the same ep, and verify again.
5. **Close** — if reviewed, only when the last verdict in `## Review` is
   `APPROVED`. Set Status to `[Done]`, move the file to `done/`, then commit
   only the files in the Change Log and this ep file, by path (one-line
   subject + footer `ep: {slug}`), and push. Never rewrite pushed history.
   Do not open a PR — closing an ep is not finishing the feature.

**PR** — outside the ep procedure. Open one only when the human says the
feature is done: run `## Commands` for every folder the branch spans → collect
this branch's eps from the `ep:` trailers in `git log {default branch}..{branch}`
→ present the PR title and body (one line of Purpose per ep + verification
results) and wait → on approval, open the PR. Merging is the human's.
(git strategy: GitHub Flow — these steps 2-5 and PR are all of it)

- In a plan, `## Approval`·`## Change Log`·`## Review`·`## Run History` are append-only —
  never edit past lines to match reality. Every other section is filled in
  place. Status and the criteria checkboxes are updated; record Status before
  the change that produced it. Adding a file to `## Affected Files` requires
  re-approval.
- At a fork, record the choice and the rejected option with its reason in
  `## Decisions` — the rejected option is what the code cannot recover.
- Ambiguous is not approved — re-present. If it reads as a stop, stop.
- One `## Run History` line at every run end — done, aborted, or failed.
- Facts (`## Commands` · `## Structure` · `## Forbidden`) live in subfolder
  AGENTS.md files — a file's facts are in its nearest AGENTS.md. Read them at
  planning time, and verify by running `## Commands` for every folder the change
  spans.
