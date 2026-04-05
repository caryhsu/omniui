## Context

OmniUI already has `scroll_to` / `scroll_by` for `ScrollPane`. Standalone `ScrollBar` nodes (used in custom layouts) are not yet addressable. `ScrollBar` exposes `getValue()` / `setValue()` / `getMin()` / `getMax()` directly, making this straightforward to implement via reflection.

## Goals / Non-Goals

**Goals:**
- Read `ScrollBar.getValue()` normalised to 0.0–1.0
- Set `ScrollBar.setValue()` clamped to `[min, max]`

**Non-Goals:**
- Animating scroll transitions
- Dragging the scroll thumb via mouse simulation
- Supporting `ScrollPane`'s internal scrollbars (those are handled by `scroll_to`/`scroll_by`)

## Decisions

**Return raw value vs normalised 0–1:** Return the raw value (same units as JavaFX). Callers can normalise if needed. Rationale: less surprising, consistent with JavaFX documentation. The raw `min`/`max` are also returned so callers can interpret the position.

**Clamping on set:** `setValue` will silently clamp to `[min, max]` (same as JavaFX's own behaviour). No error is raised for out-of-range values.

**FX thread:** Both operations run via `performOnFxThread` to avoid threading issues.

## Risks / Trade-offs

- [Risk] ScrollBar not in scene at call time → Mitigation: standard `selector_not_found` error path
- [Risk] Node found but is not a ScrollBar → Mitigation: `instanceof` check, return `action_not_supported`
