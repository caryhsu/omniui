## Context

OmniUI already has `Locator` as a reusable handle pattern. `OmniPage` is the next level up — a class that owns a `client` and groups related locators and actions for a single screen or component.

## Goals / Non-Goals

**Goals:**
- `OmniPage(client)` constructor stores `self.client`
- Optional `locator(id=...)` shorthand on the page
- Works as a plain Python base class — subclass and add methods

**Non-Goals:**
- Auto-discovery of UI elements
- Screenshot/assertion helpers on the page (users call `self.client.xxx()`)
- Framework-specific fixtures (pytest fixture lives separately)

## Decisions

**Single file `omniui/page.py`:** Keeps it discoverable. No sub-package needed.

**No magic:** `OmniPage` is just a thin wrapper. No metaclass, no auto-wiring beyond `self.client`. Keeps it readable and debuggable.

**`locator()` shorthand:** Delegates to `self.client.locator(...)` — convenience only, not required.

## Risks / Trade-offs

- None significant — purely additive, no Java changes.
