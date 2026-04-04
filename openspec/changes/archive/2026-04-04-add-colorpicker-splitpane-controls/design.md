## Context

OmniUI already supports 20+ JavaFX control types. `ColorPicker` and `SplitPane` are standard controls absent from the current action set. The existing pattern (ReflectiveJavaFxTarget switch cases + Python engine methods) is well-established and can be extended without architectural changes.

`ColorPicker` behaves like `DatePicker`: it has a direct `setValue` path (reliable) and a popup-based path. `SplitPane` exposes `getDividerPositions()` and `setDividerPosition(int, double)` via reflection.

The jlink image embeds `dev.omniui.agent` as a module — after any Java change, `mvn install -f java-agent/pom.xml` must precede `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`.

## Goals / Non-Goals

**Goals:**
- `set_color(color, **selector)` — direct `ColorPicker.setValue(Color)` via reflection
- `get_color(**selector)` — read `ColorPicker.getValue()` as `#RRGGBB` hex string
- `open_colorpicker(**selector)` / `dismiss_colorpicker()` — popup interaction path
- `get_divider_positions(**selector)` — read all divider positions as `list[float]`
- `set_divider_position(index, position, **selector)` — set one divider by index
- Demo UI sections and Python demo scripts for both controls
- Both demos wired into `run_all.py`

**Non-Goals:**
- Full color palette/eyedropper popup interaction beyond open/dismiss
- Custom `SplitPane` drag simulation (only programmatic position setting)
- ColorPicker custom color input (hex field typing)

## Decisions

**D1 — `set_color` uses `Color.web(hexString)` for parsing**
`javafx.scene.paint.Color.web("#RRGGBB")` is the idiomatic string-to-Color factory. Accepts standard hex strings (`#RGB`, `#RRGGBB`) and named colors. Alternative: `Color.valueOf()` — same behavior, less familiar. Chosen: `Color.web()` for clarity.

**D2 — `get_color` returns `#RRGGBB` hex**
`Color.toString()` returns `0xRRGGBBAA`. Converting to `#RRGGBB` (no alpha) matches CSS convention and what `set_color` accepts, making round-trips lossless for opaque colors. Alpha is dropped (ColorPicker values are always opaque in standard use).

**D3 — `open_colorpicker` reuses `performWithOverlayWait` pattern**
Consistent with `open_datepicker` and `open_menu`. The overlay predicate checks for a `ColorPalette` or `CustomColorDialog` window.

**D4 — SplitPane index-based divider API**
`SplitPane` supports N dividers. `set_divider_position(index, position)` mirrors the JavaFX API directly. `get_divider_positions()` returns all positions as a list, letting the caller pick by index.

## Risks / Trade-offs

- [Risk] `Color.web()` may throw `IllegalArgumentException` for invalid strings → Mitigation: catch and return `set_color_failed` with detail
- [Risk] SplitPane divider index out of bounds → Mitigation: validate index < divider count; return `invalid_divider_index` on failure
- [Risk] jlink rebuild required after Java changes → Mitigation: documented in README and manual-smoke; `mvn install` step is explicit

## Migration Plan

1. Close demo app
2. `mvn install -f java-agent/pom.xml`
3. `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`
4. Restart demo app via `run-with-agent.bat`
5. Run `python demo/python/run_all.py` to verify all demos pass

No breaking changes. All existing actions unaffected.

## Open Questions

None.
