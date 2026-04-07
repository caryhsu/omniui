## Why

Currently the Recorder GUI only reveals the captured script after the user presses Stop,
making it impossible to see whether actions are being recorded correctly mid-session.
Industry-standard tools (Playwright Codegen, Selenium IDE, Katalon) all show each step
immediately as it is recorded. Closing this gap is a high-impact, low-risk improvement
that also unlocks Run Selection (you need to see steps to select them).

## What Changes

- New Java endpoint `GET /sessions/{sessionId}/events/pending` returns events buffered
  since the last poll and clears the "delivered" cursor (events are NOT consumed —
  `DELETE /events` still flushes everything for `stop_recording`).
- New Python method `OmniUI.poll_events() → list[dict]` calls the endpoint.
- Recorder GUI starts a background polling thread (every 500 ms) when recording begins;
  each poll result is appended to the script preview in real time.
- Polling thread is stopped and cleaned up when Stop is pressed.

## Capabilities

### New Capabilities

- `recorder-poll-events`: A non-destructive polling endpoint on the Java agent that lets
  clients read buffered recorder events incrementally without stopping the recording session.

### Modified Capabilities

- `recorder-session`: The existing record session API gains a companion poll method;
  the stop/flush contract is unchanged (no breaking change).

## Impact

- **Java**: `OmniUiAgentServer.java` — new GET handler; `ReflectiveJavaFxTarget.java` —
  add a `deliveredIndex` cursor alongside the existing `recorderBuffer`.
- **Python**: `omniui/core/engine.py` — new `poll_events()` method on `OmniUI`.
- **Python**: `omniui/recorder/gui.py` — polling thread + real-time append to `Text` widget.
- **Tests**: new unit tests for `poll_events` and the GUI polling behaviour.
- **No breaking changes** — existing `start_recording` / `stop_recording` API is unchanged.
