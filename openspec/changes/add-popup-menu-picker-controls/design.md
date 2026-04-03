## Context

The OmniUI JavaFX automation framework (`add-omniui-javafx-automation-framework`) established:
- A loopback HTTP JSON protocol between the Python client and the Java agent
- Scene graph enumeration via `Platform.runLater` on the JavaFX application thread
- Priority-based selector resolution: `fx:id` → type+text → OCR → vision
- Direct node-level event dispatch for `click`, `type`, `get_text`

Popup-backed controls — ContextMenu, MenuBar menus, DatePicker, Dialog, and Alert — materialize as separate `Window` instances outside the primary `Stage` scene graph. The existing agent only enumerates the primary scene. This change extends the agent and Python client to discover and interact with overlay windows.

Key constraints from the existing design:
- Java agent communicates over loopback HTTP only
- All JavaFX scene access happens on the JavaFX application thread
- No OS-level pointer movement for JavaFX-resolvable interactions
- Python client API surface must remain backward-compatible

## Goals / Non-Goals

**Goals:**
- Extend the Java agent to enumerate nodes in all open `Window` instances, including `PopupWindow` and Dialog `Stage` instances
- Implement wait-for-overlay: after triggering a popup action, poll until the expected overlay window appears
- Support multi-level menu traversal for ContextMenu and MenuBar via sequential item hover dispatch
- Support DatePicker calendar popup navigation (month) and date cell selection by `LocalDate` value
- Support Dialog and Alert detection, content reading, and button interaction
- Add control-specific Python client methods: `right_click`, `open_menu`, `navigate_menu`, `pick_date`, `get_dialog`, `dismiss_dialog`
- Extend the demo app with scenes for all five new control types

**Non-Goals:**
- No transport protocol changes — HTTP JSON remains
- No new selector resolution tiers
- No support for detached or remote overlay windows (headless, cross-JVM)
- No automated interaction with OS-native file chooser dialogs
- No drag interactions within DatePicker

## Decisions

### 1. Enumerate overlay nodes via `Window.getWindows()`

JavaFX's `Window.getWindows()` returns all open windows, including `PopupWindow` (ContextMenu, MenuBar popup, DatePicker popup) and `Stage` (Dialog, Alert). After triggering a popup action, the agent polls this list until the expected overlay window appears.

Rationale:
- No application code hooking or window-creation event listeners required
- Consistent approach for all popup-backed control types
- Natural extension of the existing `Platform.runLater` agent model

Alternatives considered:
- Listen for `WindowEvent.WINDOW_SHOWN`: requires event hookup, inconsistent with existing agent structure
- Poll primary scene graph children for popup nodes: does not cover separate `Window` instances

### 2. Wait-for-overlay uses short polling with configurable timeout

After a popup-triggering action, the agent polls `Window.getWindows()` on the JavaFX thread at a short interval (default 50 ms) until a matching overlay window appears, or the timeout expires (default 2 seconds).

Rationale:
- JavaFX popup rendering is asynchronous; no synchronous API is available to wait for it
- Short polling avoids busy-wait CPU cost
- Configurable timeout lets tests run reliably across different machine speeds

Alternatives considered:
- Fixed sleep: non-deterministic and wasteful on fast machines
- `AnimationTimer`: more complex, marginal benefit over polling loop

### 3. Multi-level menu traversal uses sequential hover dispatch

For multi-level ContextMenu and MenuBar submenus, the Python client passes a path (e.g., `["File", "Export", "CSV"]`). The agent dispatches a mouse-entered event (hover) on each intermediate item and waits for its submenu window to appear in `Window.getWindows()` before proceeding to the next level.

Rationale:
- Consistent with the existing design principle of event-level JavaFX interaction over OS coordinates
- JavaFX submenus reliably expand on hover events without OS mouse movement
- Path-based API is intuitive and readable in automation scripts

Alternatives considered:
- OS mouse movement: violates the design principle; unstable across DPI
- Trigger submenu show via reflection: over-coupled to JavaFX internals

### 4. DatePicker date cells identified by `LocalDate` item property

DatePicker popup cells (`DateCell`) expose their value as a `LocalDate` via the `item` property. The agent enumerates cells in `DatePickerContent`, matches by `LocalDate` value, and dispatches a click. Month navigation uses click dispatch on the forward/backward navigation buttons.

Rationale:
- `LocalDate` identity is stable and locale-independent
- More robust than matching display text, which varies by locale

Alternatives considered:
- Match by cell text (e.g., "15"): ambiguous across locales and month layouts
- Match by grid position: shifts with different month start days

### 5. Dialog and Alert identified by `DialogPane` root type

Dialogs and Alerts are separate `Stage` instances whose scene root is a `DialogPane`. The agent identifies them by scanning `Window.getWindows()` for a `Stage` with a `DialogPane` root, then reads header text, content text, and buttons from the `ButtonBar`.

Rationale:
- `DialogPane` is the standard JavaFX dialog root; detection requires no application code
- Reading `ButtonData` from buttons allows semantic identification (OK, CANCEL) independent of locale text

Alternatives considered:
- Identify by window title: fragile; applications may customize titles
- Identify by `ButtonType` directly: `ButtonData` is more stable than button display text

### 6. Each control type gets a dedicated Python API method

Rather than a generic "click overlay item" API, five dedicated methods are exposed: `right_click`, `open_menu`, `navigate_menu`, `pick_date`, `get_dialog`, `dismiss_dialog`.

Rationale:
- Communicates intent clearly in automation scripts
- Each method has a well-defined parameter contract and error surface
- Enables per-control-type action tracing and observability

Alternatives considered:
- Generic `interact_overlay(type, action, ...)`: obscures control-type differences; harder to debug

## Risks / Trade-offs

- [JavaFX popup rendering timing varies across machines and JVM versions] → Mitigation: wait-for-overlay timeout and polling interval are configurable per invocation
- [Multi-level submenu hover timing is not deterministic] → Mitigation: each hover waits for the submenu window to appear before proceeding; no fixed sleeps
- [DatePicker internal node structure (`DatePickerContent`, `DateCell`) may differ across JavaFX versions] → Mitigation: document a tested JavaFX version support matrix; add a version assertion in the demo
- [Dialog detection assumes standard `javafx.scene.control.Dialog`; custom dialog implementations may not be detected] → Mitigation: Phase 1 scope is explicitly limited to standard `Dialog` and `Alert`; document this boundary
- [`Window.getWindows()` may include non-popup windows requiring careful filtering] → Mitigation: filter by window type and scene root type to minimize false positives

## Migration Plan

This change extends the existing framework with no breaking changes to existing selectors or actions. Rollout order:

1. Extend the Java agent to enumerate overlay windows via `Window.getWindows()` and add wait-for-overlay polling
2. Add ContextMenu trigger, item selection, and multi-level traversal
3. Add MenuBar menu activation and multi-level submenu traversal
4. Add DatePicker popup open, month navigation, and date selection
5. Add Dialog detection, content reading, and button interaction
6. Add Alert detection, message reading, and button interaction
7. Extend the demo app with a scene per control; add Python demo scripts
8. Update the Python client with the new API methods

Rollback strategy:
- If overlay enumeration destabilizes existing actions, gate overlay window scanning behind a feature flag so primary scene behavior is not affected

## Open Questions

- What should the default wait-for-overlay timeout be to balance CI speed and reliability on slower machines?
- Should `navigate_menu(path=[...])` support both text and fx:id as identifiers for each level?
- Should `pick_date` auto-navigate across multiple months if the target date is many months away?
- Should dialog button matching prefer `ButtonData` semantic type with text as fallback, or require an explicit parameter to select the matching strategy?
- Should Alert expanded text (if present) be included in the `get_dialog()` response?
