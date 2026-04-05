## Why

UI test authoring requires manual coding — testers must know the `fxId` of every widget before writing a single line. Playwright solves this with `codegen`; OmniUI needs equivalent capability so non-developers can create tests by simply clicking through the app.

## What Changes

- Java agent attaches an `EventFilter` to the Scene and exposes a `/events` endpoint to stream or flush captured events
- Python client gains `start_recording()` / `stop_recording()` session methods
- A new `SelectorInference` layer derives the most-stable selector from raw event metadata (`fxId` → `text` → `type+index`)
- A `ScriptGenerator` serialises a list of `RecordedEvent` objects into a runnable Python test script string
- `RecordedScript.save(path)` writes the generated script to disk

## Capabilities

### New Capabilities

- `event-capture`: Java agent intercepts and buffers user interaction events (click, type, key press) via Scene EventFilter; exposes `/events` flush endpoint
- `selector-inference`: Python-side logic that selects the most stable selector from a captured event's node metadata
- `script-generation`: Converts a list of `RecordedEvent` objects into a Python test script string
- `record-session`: High-level `start_recording()` / `stop_recording()` / `RecordedScript.save()` API on `OmniUIClient`

### Modified Capabilities

*(none — all capabilities are new)*

## Impact

- **Java**: `ReflectiveJavaFxTarget.java` — new EventFilter registration, event buffer, `/events` HTTP handler
- **Python models**: new `RecordedEvent`, `RecordedScript` dataclasses in `omniui/core/models.py`
- **Python engine**: new methods on `OmniUIClient` in `omniui/core/engine.py`
- **New module**: `omniui/recorder/` (or inline in engine) for `SelectorInference` and `ScriptGenerator`
- **Demo**: `demo/python/advanced/recorder_demo.py` + `run_all.py` section
- **Tests**: `SnapshotDiffTests`-style unit tests in `tests/test_client.py`
- **ROADMAP**: mark Event capture, Selector inference, Script generation, Record session API as `[x]`
