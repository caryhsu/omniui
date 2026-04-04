## Context

OmniUI is a Python automation client for JavaFX apps. All existing actions (`click`, `type`, `verify_text`) are synchronous: they send an HTTP request to the Java agent and return immediately. JavaFX UI state changes (e.g. loading spinners finishing, async validation, transitions) happen after the triggering action completes, so tests that check state immediately may fail intermittently.

`is_visible()` and `is_enabled()` already exist in `engine.py` (from `node-state-queries`), reading from `get_nodes()`. `get_text()` wraps the agent's `get_text` action. These are the building blocks for poll-based waits.

## Goals / Non-Goals

**Goals:**
- Provide 5 poll-based wait helpers: `wait_for_text`, `wait_for_visible`, `wait_for_enabled`, `wait_for_node`, `wait_for_value`
- Pure Python — no Java agent changes, no new dependencies
- Configurable `timeout` (seconds, float) and `interval` (poll period, default 0.2s)
- Raise `TimeoutError` with a clear message on expiry
- Default timeout of 5 seconds

**Non-Goals:**
- Agent-side blocking/events (not needed; poll is sufficient for UI tests)
- `wait_for_invisible` / `wait_for_disabled` variants (can add later if needed)
- Async/await interface (scripts are synchronous)

## Decisions

### Decision: Poll loop in Python, not agent-side
**Choice:** Python `time.sleep` loop calling existing client methods.  
**Rationale:** No agent changes, no protocol changes, simpler to implement and test. JavaFX UI frames refresh every ~16ms; a 200ms poll interval is fast enough for test use cases and avoids hammering the agent.  
**Alternative considered:** Agent-side `waitFor` action that blocks until a condition — rejected because it requires Java changes, complicates timeout handling, and ties timeout management to the agent's HTTP server lifecycle.

### Decision: Raise `TimeoutError` on timeout, not return `False`
**Choice:** Raise `TimeoutError` on expiry.  
**Rationale:** Wait conditions are assertions — a timeout almost always means the test should fail. A silent `False` return would cause confusing failures downstream. Callers who want soft behavior can catch the exception.  
**Alternative considered:** Return `bool` — rejected for above reasons.

### Decision: `wait_for_value` as alias for `wait_for_text`
**Choice:** `wait_for_value(id, expected, timeout)` delegates to `wait_for_text`.  
**Rationale:** The ROADMAP listed both; they're the same operation. Providing both names improves discoverability. No duplication cost.

### Decision: Default `timeout=5.0`, `interval=0.2`
**Choice:** 5 seconds timeout, 0.2 second poll interval.  
**Rationale:** 5s is a sensible upper bound for UI transitions; 0.2s poll means at most 25 HTTP discovery calls — acceptable. Both are caller-overridable.

## Risks / Trade-offs

- [Risk] `get_nodes()` full discovery scan on each poll is expensive for large UIs → Mitigation: 0.2s interval limits to 5 calls/second; acceptable for tests.
- [Risk] `TimeoutError` from Python stdlib may conflict if caller catches broad exceptions → Mitigation: documented clearly; `TimeoutError` is the standard choice.
- [Risk] `wait_for_node` uses `get_nodes()` and matches by `id` — if the node has no `fxId`, it won't be found → Mitigation: document that `id=` must match a JavaFX `fx:id`; same constraint as all other selectors.
