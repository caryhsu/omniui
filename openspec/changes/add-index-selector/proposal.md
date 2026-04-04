## Why

JavaFX nodes generated dynamically (ListView cells, repeated Button rows, auto-generated controls) often have no `id`. The existing `resolve()` only returns `findFirst()` — there is no way to target the 2nd or 3rd matching node. Adding `index=N` to the selector model solves this with minimal risk: it is a pure filter step on top of existing matching logic, requiring no new agent actions and no Python API changes.

## What Changes

- Java agent `resolve()`: extract optional `index` from selector; apply existing filters, then `.skip(index).findFirst()` instead of always `.findFirst()`
- No new Python methods needed — `index=2` passes through `**selector` naturally
- Add demo and documentation

## Capabilities

### New Capabilities
- `index-selector`: The selector model SHALL support an optional `index` field (0-based integer) that picks the Nth node among all nodes matching the other selector criteria

### Modified Capabilities
- `python-automation-client`: Document `index=` as a supported selector field across all action methods

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — `resolve()` only (~5 lines)
- `demo/python/index_selector_demo.py` — new demo
- `demo/python/run_all.py` — add demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- **No jlink rebuild needed** — resolve() change does not affect the agent JAR signature used by jlink; actually it does need rebuild since it changes the agent jar. jlink rebuild required.
