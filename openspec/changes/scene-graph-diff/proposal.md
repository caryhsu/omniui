## Why

After automating a UI action, tests need to verify what changed. Comparing raw `get_nodes()` output manually is verbose. `snapshot()` + `diff()` give tests a concise way to assert that exactly the right nodes appeared, disappeared, or changed.

## What Changes

- Add `client.snapshot()` — wraps `get_nodes()` and returns a `UISnapshot` (timestamped list of node dicts)
- Add `client.diff(before, after)` — compares two `UISnapshot`s and returns a `UIDiff` with `added`, `removed`, and `changed` lists

## Capabilities

### New Capabilities

- `scene-graph-snapshot`: `client.snapshot()` returns a `UISnapshot` dataclass with a `nodes` list and `timestamp`
- `scene-graph-diff`: `client.diff(before, after)` returns a `UIDiff` dataclass with `added`, `removed`, `changed` node lists

### Modified Capabilities

(none)

## Impact

- `omniui/core/engine.py`: add `snapshot()` and `diff()` methods
- `omniui/core/models.py`: add `UISnapshot` and `UIDiff` dataclasses
- `omniui/__init__.py`: export `UISnapshot`, `UIDiff`
- No Java changes required
- No breaking changes
