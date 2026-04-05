## Context

JavaFX `Pagination` exposes `getCurrentPageIndex()`, `setCurrentPageIndex()`, and `getPageCount()` directly. The agent already has patterns for reading/setting numeric values via `safeInvoke` (e.g. ScrollBar). Pagination is simpler — no clamping edge case beyond `[0, pageCount-1]`.

## Goals / Non-Goals

**Goals:**
- Read current page index (0-based) and total page count
- Set page to an arbitrary index (clamped silently to valid range)
- Navigate +1 / -1 page

**Non-Goals:**
- Animating page transitions
- Reading page content/children
- `INDETERMINATE` page count (`Pagination.INDETERMINATE = Integer.MAX_VALUE`) — returned as-is

## Decisions

**0-based index:** JavaFX uses 0-based; we expose the same. No translation layer.

**next/prev as separate actions:** Avoids client needing to read current page first; also maps cleanly to single action semantics already established by the framework.

**Clamping on set_page:** `setCurrentPageIndex` out-of-range → clamp to `[0, pageCount-1]`. Same pattern as ScrollBar.

**FX thread:** All operations via `performOnFxThread`.

## Risks / Trade-offs

- [Risk] `getPageCount()` returns `INDETERMINATE` — Mitigation: return raw value, document behaviour
- [Risk] Node not a Pagination → Mitigation: `getSimpleName().equals("Pagination")` check, return `action_not_supported`
