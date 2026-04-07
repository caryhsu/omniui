## Context

The Recorder GUI (`omniui/recorder/gui.py`) already supports Start / Stop recording and displays the generated script in a `ScrolledText` widget. Real-time polling was added in the previous change. The next logical step is letting users run the recorded script without leaving the Recorder, matching the UX of SQL clients (Run All / Run Selection).

The Recorder holds a reference to an `OmniUIClient` instance (`self.client`) used for recording; the same client is reused for playback.

## Goals / Non-Goals

**Goals:**
- Add **Run All** button: execute the full script text in the preview widget
- Add **Run Selection** button: execute only the selected text in the preview widget
- Show run status in the GUI (Idle / Running… / ✅ Passed / ❌ Failed: \<message\>)
- Run in a background thread so the GUI stays responsive

**Non-Goals:**
- Step-by-step debugger or breakpoints
- Saving scripts to disk (separate concern)
- Modifying the Java agent or engine.py

## Decisions

### D1 — Use `exec()` with a pre-built namespace

Run the script using Python's built-in `exec(code, namespace)` where `namespace = {"client": self.client}`. This requires zero new dependencies and matches how users would write scripts manually (`client.click(...)` etc.).

**Alternative considered:** `subprocess` launching a new Python process. Rejected — requires writing the script to a temp file and re-connecting to the agent, which is slower and more fragile.

### D2 — Run in a `threading.Thread`

Execution runs in a daemon thread so the Tkinter main loop is never blocked. The Run buttons are disabled while a run is in progress to prevent concurrent runs.

**Alternative considered:** `asyncio`. Rejected — Tkinter is not async-native; mixing event loops adds unnecessary complexity.

### D3 — Status bar replaces last-line label

A single `StringVar`-backed label at the bottom of the window shows the current run state. States: `""` (idle), `"Running…"`, `"✅ Passed"`, `"❌ Failed: <exc>"`. Color-coded (grey / orange / green / red).

### D4 — Run Selection uses `widget.get(SEL_FIRST, SEL_LAST)`

If the user has a text selection in the script preview, Run Selection extracts it and executes only that fragment. If nothing is selected, a tooltip / status message says "No text selected".

## Risks / Trade-offs

- **Partial script execution** — running a selection mid-script may fail if it references variables set earlier. Acceptable: the user is responsible for selecting a self-contained snippet. → Mitigation: status bar shows the full exception message.
- **exec() security** — `exec` runs arbitrary code in-process. Acceptable because the Recorder is a local developer tool, not a user-facing service.
- **Thread safety of OmniUIClient** — `_request_json` uses `urllib` which is thread-safe at the socket level. No shared mutable state beyond `_recording` flag; run-mode calls (click, type) do not mutate it. → Low risk.

## Migration Plan

No migration needed — pure GUI addition, no API changes, no data model changes.

## Open Questions

- Should Run Selection be disabled if nothing is selected, or show a warning? → Plan: show `"❌ No text selected"` in status bar and return early.
