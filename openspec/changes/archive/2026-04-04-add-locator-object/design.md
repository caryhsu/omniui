## Context

OmniUI automation scripts frequently target the same node across multiple calls: wait until visible, click, then verify text. Each call currently requires repeating the selector (`id="loginBtn"`), making scripts verbose. Page Object Model patterns also benefit from a handle that encapsulates the node selector rather than exposing raw kwargs everywhere.

`OmniUIClient` methods already accept `**selector: Any` uniformly. A `Locator` only needs to store those kwargs and forward them — no protocol or Java agent changes required.

## Goals / Non-Goals

**Goals:**
- Provide `Locator` class obtainable via `client.locator(**selector)`
- Forward all node-scoped `OmniUIClient` methods without repeating the selector
- Raise `ValueError` on construction if no selector kwargs provided
- `wait_for_*` methods require `id=` in the selector; raise `ValueError` with a clear message if missing
- Export `Locator` from the package root

**Non-Goals:**
- Lazy evaluation or remote node handle (no Java agent changes)
- Chaining / fluent builder beyond the current method set
- Thread safety or async interface

## Decisions

### Decision: Locator as a thin delegation wrapper
**Choice:** `Locator` stores `_client` and `_sel` dict; each method calls the corresponding `OmniUIClient` method with `**self._sel` merged in.
**Rationale:** Zero duplication — any new `OmniUIClient` method is available on `Locator` with one line of delegation. Selector logic stays in `OmniUIClient`.
**Alternative considered:** Subclassing `OmniUIClient` — rejected because Locator is not a client; it's a selector-bound view over one.

### Decision: wait_for_* require id=
**Choice:** Extract `id` from `_sel`; raise `ValueError` with clear message if not present.
**Rationale:** The underlying `wait_for_visible(id: str)` etc. take a positional string id, not `**selector`. Adding full selector support to wait methods is a separate future change. A clear `ValueError` is better than a silent `KeyError`.

### Decision: Lazy import inside locator() to avoid circular import
**Choice:** `engine.py`'s `locator()` method does `from omniui.locator import Locator` at call time.
**Rationale:** `locator.py` imports `OmniUIClient` only under `TYPE_CHECKING`, so the runtime import graph is acyclic. Lazy import inside the factory avoids the need for any restructuring.

## Risks / Trade-offs

- [Risk] `wait_for_*` methods only accept `id=` — locators built with `text=` or `type=` cannot use wait methods → Mitigation: `ValueError` with clear message; documented constraint.
- [Risk] New methods added to `OmniUIClient` won't automatically appear on `Locator` → Mitigation: explicit delegation list; future PR adds new methods to both.
