## Why

Complex JavaFX UIs often have duplicate node IDs or identical text labels in different panels. Selectors like `id="ok"` are ambiguous when multiple panels each have an OK button. Scoped selector `within` lets tests scope a lookup to a specific container, eliminating ambiguity and making tests more robust.

## What Changes

- Add `client.within(container_selector)` context manager to `OmniUIClient`
- Within the context, all find-based actions restrict their node search to descendants of the matched container
- Java agent: extend `performOnFxThread` to accept an optional `scope` field in the selector; when present, walk the subtree of the scoped node instead of the full scene graph

## Capabilities

### New Capabilities

- `scoped-selector`: `within(id=..., text=..., type=...)` context manager on `OmniUIClient`; actions issued inside the `with` block carry a `scope` selector that constrains node lookup to that subtree

### Modified Capabilities

- `node-lookup`: extend existing lookup logic to accept an optional `scope` constraint; if scope is set, search only within matched container's subtree

## Impact

- `omniui/core/engine.py`: add `within()` context manager
- `java-agent/ReflectiveJavaFxTarget.java`: extend `findNode` / `performOnFxThread` to accept `scope` in selector JSON and restrict traversal
- No breaking changes; existing selectors without scope continue to work unchanged
