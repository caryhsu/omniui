## Context

OmniUI node resolution relies on `fx:id`, text, or type. Some nodes have none of these (canvas elements, custom-rendered controls). Absolute coordinate click bypasses node resolution entirely, firing a `MouseEvent` at scene-relative (x, y) coordinates.

The implementation mirrors `handleDoubleClick`'s MouseEvent construction but:
- fires at given (x, y) rather than node center
- uses `clickCount=1`
- needs `scene.getRoot()` as the fire target (since there is no resolved node)

## Goals / Non-Goals

**Goals:**
- `click_at(x, y)` fires a single `MOUSE_CLICKED` at given scene coordinates
- Java action `click_at` is a first-layer action (no node resolution)
- Coordinates are scene-relative (origin = top-left of scene content area)

**Non-Goals:**
- Screen-absolute coordinates (use scene-relative only)
- Right-click or middle-click variants
- Modifier keys (covered by existing `modifier_click`)

## Decisions

### Fire on scene root, not on stage
`scene.getRoot()` is the correct `EventTarget` for coordinate-based events; it dispatches the event through the scene graph at the correct position. Using `Stage` would bypass scene event handling.

### Scene-relative coordinates
Simpler than screen-absolute — tests don't break when window is moved. The agent already knows the scene from `sceneSupplier`.

*Alternative:* Screen-absolute with conversion — rejected as fragile (window position changes break tests).

## Risks / Trade-offs

- [Risk] Coordinates outside the scene bounds silently fire an event with no effect. → Mitigation: document that x/y must be within scene dimensions.
- [Risk] Headless / Monocle may not compute `localToScreen` properly. → Same constraint as `double_click`; acceptable.
