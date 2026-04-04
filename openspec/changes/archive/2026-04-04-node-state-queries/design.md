## Context

OmniUI's Java agent already populates `visible` and `enabled` boolean fields in the node metadata returned by the discovery endpoint. The Python engine's `find()` method returns this metadata, but callers must know to look at `result["visible"]` or `result["metadata"]["visible"]` — the exact key path is not obvious and couples callers to the internal metadata shape.

`is_visited()` and `get_expanded()` are established precedents for boolean state accessors that hide internal detail and return a plain `bool`.

## Goals / Non-Goals

**Goals:**
- Add `is_visible(**selector) -> bool` and `is_enabled(**selector) -> bool` as first-class methods on `OmniUiClient`
- Return `False` on node-not-found (Playwright-style, no exception)
- Consistent with existing `is_visited()` / `get_expanded()` signature style
- Zero Java agent changes — pure Python implementation

**Non-Goals:**
- `verify_visible` / `verify_enabled` assertion variants (can be added later)
- Watching for state changes (belongs in `wait_for_*` change)
- `is_focused`, `is_selected` or other state properties (separate change)

## Decisions

**D1: Read from discovery metadata, not a dedicated action**
Both `visible` and `enabled` are already in the discovery snapshot returned by `find()`. Sending a separate agent action round-trip would add latency with no benefit.
→ Use `find()` internally and read `metadata["visible"]` / `metadata["enabled"]`.

**D2: Return `False` on node-not-found, not raise**
Consistent with Playwright conventions and OmniUI's own `is_visited()`. Tests checking "is this gone yet?" should not throw.

**D3: `bool` return type, not `ActionResult`**
Consistent with `is_visited()` and `get_expanded()`. These are pure read accessors — callers don't need ok/trace overhead.

## Risks / Trade-offs

- [Risk] `find()` triggers a full discovery scan on every call → Mitigation: acceptable for now; caching is a separate concern
- [Trade-off] Returning `False` on missing node hides typos in `id` — callers must check carefully; documented in docstring

## Open Questions

None — design is straightforward given existing precedents.
