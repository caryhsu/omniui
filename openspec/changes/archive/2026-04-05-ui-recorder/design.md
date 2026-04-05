## Context

OmniUI currently requires testers to hand-write every `client.click(id=...)` call. The Java agent already has full scene graph access and can intercept JavaFX events at the `Scene` level via `EventFilter`. The Python client already speaks HTTP-JSON to the agent; adding a `/events` endpoint is a natural extension of the existing `/actions` pattern.

## Goals / Non-Goals

**Goals:**
- Record click, type, and key-press events while the user interacts with the running app
- Infer a stable selector for each event (`fxId` → `text` → `nodeType+index`)
- Generate a Python test script string from the recorded events
- Expose `start_recording()` / `stop_recording()` / `RecordedScript.save(path)` on `OmniUIClient`

**Non-Goals:**
- Visual codegen UI / browser extension
- Recording scroll, drag, hover (can be added later)
- Playback fidelity / timing assertions (generated script uses action calls, not time delays)
- Support for non-JavaFX targets in this iteration

## Decisions

### D1: EventFilter on Scene root (Java)
Attach `EventFilter` for `MouseEvent.MOUSE_CLICKED` and `KeyEvent.KEY_TYPED` at `Scene` level (not individual nodes). This captures all events before they reach handlers, without modifying app code.

**Alternative considered**: node-level listeners — too invasive, requires walking the full scene graph on every new node.

### D2: In-memory event buffer in agent; flush via `/events` endpoint
Events accumulate in a `ConcurrentLinkedDeque<Map<String,Object>>`. `GET /events?flush=true` returns them as JSON array and clears the buffer. Python client calls this on `stop_recording()`.

**Alternative considered**: WebSocket streaming — overengineered for the use case; HTTP poll is simpler and consistent with existing transport.

### D3: Selector inference priority: fxId → text → nodeType+index
- `fxId` present → `{"id": fxId}` — most stable
- no fxId, text ≤ 40 chars → `{"text": text}` — human-readable
- fallback → `{"type": nodeType, "index": n}` — fragile, flagged with a comment in generated code

### D4: Script generator as a pure function `generate_script(events) -> str`
No class needed; a module-level function in `omniui/recorder/script_gen.py` is simpler and easier to test.

### D5: `RecordedEvent` and `RecordedScript` as dataclasses in `models.py`
Keeps the model layer consistent with `UISnapshot`/`UIDiff`.

## Risks / Trade-offs

- **[Risk] EventFilter interferes with app event handling** → Mitigation: use `addEventFilter` (not `addEventHandler`); call `event.consume()` only in debug mode off by default.
- **[Risk] Agent crashes leave EventFilter registered** → Mitigation: `stop_recording()` always sends a `DELETE /events` to deregister the filter.
- **[Risk] High-frequency events flood the buffer** → Mitigation: debounce KEY_TYPED events (50 ms window); cap buffer at 1 000 events with a warning.
- **[Risk] Generated script uses fragile `type+index` selectors** → Mitigation: mark those lines with `# WARN: fragile selector` comment in generated output.

## Open Questions

- Should `type` events be aggregated into a single `type(id, text)` call per field, or kept as individual key events? (Recommendation: aggregate — much cleaner output.)
