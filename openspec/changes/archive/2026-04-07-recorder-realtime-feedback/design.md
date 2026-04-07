## Context

The Recorder GUI (`python -m omniui.recorder`) currently collects events during a recording
session and only shows the generated script after `stop_recording()` is called. The Java agent
buffers events in `recorderBuffer` (a `Deque<Map<String,Object>>`). The existing
`DELETE /events` endpoint atomically flushes and returns all events.

Industry-standard tools (Playwright Codegen, Selenium IDE) show each step in real time.
Closing this gap also unblocks **Run Selection** (users must see steps to select them).

## Goals / Non-Goals

**Goals:**
- Show each recorded step in the Recorder GUI within ~500 ms of it occurring
- Non-destructive polling: `stop_recording` still works exactly as before
- No breaking changes to the public Python API

**Non-Goals:**
- Sub-100 ms latency (polling is sufficient; SSE/WebSocket is a future upgrade path)
- Streaming support for the Python automation client (only the GUI uses polling)
- Changing the internal event buffer format or the script generation pipeline

## Decisions

### Decision: Cursor-based polling, not a separate queue

**Options considered:**
1. **Separate delivered queue** — copy delivered events to a second deque; flush on stop
2. **Integer index cursor** — add `deliveredUpTo: AtomicInteger` alongside `recorderBuffer`; poll returns `buffer[deliveredUpTo:]` and advances the cursor; stop_recording ignores the cursor and flushes everything

**Choice: Option 2 (index cursor)**
- `recorderBuffer` stays as-is; `stop_recording` is completely unaffected
- No data duplication
- Thread-safe with `AtomicInteger` + the deque's existing volatile semantics

### Decision: Poll on the Python side, not push from Java

**Options considered:**
1. SSE / WebSocket push from Java
2. GUI-side polling thread via `threading.Thread`

**Choice: Option 2 (polling thread)**
- `com.sun.net.httpserver` has no WebSocket support; SSE requires a long-lived connection manager
- 500 ms polling on localhost is imperceptible and near-zero CPU cost
- No change to the transport layer; upgrade to SSE is a drop-in replacement later

### Decision: Polling endpoint is GET, not POST

`GET /sessions/{sessionId}/events/pending` — idempotent reads are semantically GET.
The response returns new events and advances the server-side cursor; this is acceptable
because polling is only used during an active recording session.

## Risks / Trade-offs

- **Race at stop time** → GUI thread may call `poll_events` at the same moment `stop_recording` calls `DELETE /events`. Mitigation: GUI polling thread is stopped *before* `stop_recording` sends DELETE; any final events are merged from the flush response.
- **Duplicate final events** → the last poll and the stop flush may overlap. Mitigation: GUI de-duplicates by event `timestamp` when merging stop-flush events with already-displayed events.
- **Deque growth** → `recorderBuffer` is already capped at `MAX_RECORDER_EVENTS`; no change needed.
