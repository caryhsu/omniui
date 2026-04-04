## Context

OmniUI currently has no scroll support. JavaFX `ScrollPane` wraps an arbitrary content node and exposes `setHvalue(double)` / `setVvalue(double)` (range 0.0–1.0) for programmatic scrolling. `Node.requestFocus()` + `ensureVisible()` is not available on all containers, so we drive scroll directly via `ScrollPane` properties.

The demo app (`LoginDemoApp.java`) has no `ScrollPane` today; we add a scrollable `VBox` wrapped in a `ScrollPane` with labelled rows to serve as a smoke-test fixture.

## Goals / Non-Goals

**Goals:**
- `scroll_to(**selector)` — find the `ScrollPane` ancestor of the target node and scroll it so the node is visible (using viewport/content geometry)
- `scroll_by(delta_x, delta_y, **selector)` — adjust `hvalue`/`vvalue` of the resolved `ScrollPane` by a relative amount; if selector is omitted, find the first focused/topmost `ScrollPane`
- Pure Python-side selector routing; Java agent handles reflection calls
- No Java rebuild required for Python-only additions — but `scroll_to`/`scroll_by` need Java cases, so a jlink rebuild is required

**Non-Goals:**
- Mouse-wheel simulation (use `scroll_by` instead)
- Horizontal `ListView` or `TableView` scroll (deferred; only vertical `ScrollPane` tested)
- Scroll-to with visual animation (instant jump only)

## Decisions

### D1 — Drive via `ScrollPane.setVvalue()` / `setHvalue()`

`ScrollPane` is the only scrollable container in JavaFX that exposes a public scroll API (`hvalue`, `vvalue`, `hmin/hmax`, `vmin/vmax`). `ListView` and `TableView` embed their own virtual scroll but do not expose it via a simple property — deferred.

**Alternatives considered:**
- `VirtualScrollBar` hack via CSS lookup — fragile, private API
- `KeyEvent` UP/DOWN simulation — not reliable for large offsets

### D2 — `scroll_to` walks the parent chain

For `scroll_to`, we resolve the target node, then walk `node.getParent()` upward until we find a `ScrollPane`. We then compute the node's bounds in the `ScrollPane` coordinate space and set `vvalue` proportionally.

**Formula:**
```
nodeTop = scrollPaneContent.sceneToLocal(node.localToScene(node.getBoundsInLocal())).getMinY()
contentH = scrollPaneContent.getBoundsInLocal().getHeight()
viewportH = scrollPane.getViewportBounds().getHeight()
vvalue = nodeTop / (contentH - viewportH)   // clamped 0..1
```

**Alternatives considered:**
- Pass `ScrollPane` id explicitly — requires callers to know layout structure; worse DX

### D3 — `scroll_by` delta is normalised (0.0–1.0 per scroll pane range)

`vvalue` is on a 0–1 scale. `delta_y` in `scroll_by` is a float in the same 0–1 scale (e.g. `0.1` = 10% of scroll range). This avoids pixel math and is layout-agnostic.

### D4 — Selector for `scroll_by` resolves to the `ScrollPane` directly

If `id="myScrollPane"`, the agent resolves that node (must be a `ScrollPane`). If no selector, the agent finds the first `ScrollPane` in the scene.

## Risks / Trade-offs

- [Nested ScrollPanes] `scroll_to` walks to the first `ScrollPane` ancestor — may target the inner one. **Mitigation:** Accept for now; outer ScrollPane can be targeted by id via `scroll_by`.
- [ListView/TableView] `scroll_to` will fail if the nearest ancestor is a virtual-scroll container. **Mitigation:** Log a clear error; deferred to future spec.
- [content geometry] If `ScrollPane` content height equals viewport height, `contentH - viewportH = 0` — divide-by-zero. **Mitigation:** Check and skip scroll (already visible).

## Migration Plan

1. Add `scroll_to` / `scroll_by` cases to Java agent
2. Rebuild jlink
3. Add Python methods
4. Add scrollable fixture to demo app + demo script
5. Update docs
