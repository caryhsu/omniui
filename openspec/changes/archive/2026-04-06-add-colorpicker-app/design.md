## Context

OmniUI supports automation of JavaFX controls via a reflection-based Java agent. ColorPicker (`javafx.scene.control.ColorPicker`) was previously implemented but lost. The existing `colorpicker-automation` spec fully defines the required behavior. Implementation mirrors the DatePicker pattern: direct-set (`set_color`), read (`get_color`), and popup open/dismiss (`open_colorpicker` / `dismiss_colorpicker`).

Port 48106 is next in the sequential demo-app port scheme (core=48100, input=48101, advanced=48102, drag=48103, progress=48104, image=48105).

## Goals / Non-Goals

**Goals:**
- Restore ColorPicker Java agent actions and Python API
- Standalone `color-app` demo with a single ColorPicker, result label, and reset button
- Python demo script that exercises all 4 API methods

**Non-Goals:**
- SplitPane (separate concern)
- Custom color palette or HSB/RGB sliders

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Port | 48106 | Sequential scheme |
| Demo app location | `demo/java/color-app/` | Consistent with other standalone apps |
| `set_color` implementation | `Color.web(hex)` → `setValue()` | Simple, matches DatePicker `setValue` pattern |
| `get_color` implementation | `getValue()` → format `#RRGGBB` | Same as previous implementation |
| `open_colorpicker` | `performWithOverlayWait` + `isColorPickerWindow` predicate | Consistent with `open_datepicker` |
| `dismiss_colorpicker` | Press Escape via `doDismissColorPicker()` | Consistent with `dismiss_menu` / `dismiss_datepicker` |

## Risks / Trade-offs

- [Risk] `isColorPickerWindow` predicate may misidentify popup windows → Mitigation: check for `ColorPalette` CSS style class in popup scene
- [Risk] `Color.web()` throws on invalid hex strings → Mitigation: wrap in try/catch, return `set_color_failed`
