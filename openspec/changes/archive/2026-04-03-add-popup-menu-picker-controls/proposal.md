## Why

The current OmniUI framework covers node discovery and interaction for in-scene selection controls (ComboBox, ListView, TreeView, TableView). Popup-backed and overlay-driven controls — ContextMenu, MenuBar, DatePicker, Dialog, and Alert — materialize outside the primary scene graph as ephemeral overlay windows, requiring dedicated discovery and interaction handling. Adding this group covers the most common overlay-driven automation patterns in JavaFX desktop applications.

## What Changes

- Add automation support for **ContextMenu** — open by right-click or programmatic trigger, traverse single-level and multi-level menu item hierarchies, and click a target item.
- Add automation support for **MenuBar** — navigate top-level menu headers and single-level or multi-level submenu hierarchies, and click a target menu item.
- Add automation support for **DatePicker** — open the calendar popup, navigate months, and select a target date cell.
- Add automation support for **Dialog** — detect an open modal dialog, read its title and content text, and click a named button.
- Add automation support for **Alert** — detect alert type (information, confirmation, warning, error), read the alert message, and click a named button.
- Extend the advanced demo application with dedicated scenes for each new control.
- Extend the advanced Python demo scripts to exercise each new control interaction.
- Extend `javafx-automation-core` to cover ephemeral overlay node enumeration and interaction dispatch for popup-backed controls.

## Capabilities

### New Capabilities

- `contextmenu-automation`: Automate ContextMenu controls including right-click trigger, single-level and multi-level item traversal, and item selection.
- `menubar-automation`: Automate MenuBar menus including top-level header activation and single-level and multi-level submenu traversal and item selection.
- `datepicker-automation`: Automate DatePicker controls including popup open, month navigation, and date cell selection.
- `dialog-automation`: Automate modal Dialog controls including open detection, title and content reading, and button interaction.
- `alert-automation`: Automate Alert controls including type detection, message reading, and button interaction.

### Modified Capabilities

- `javafx-automation-core`: Extend node discovery and interaction dispatch to cover ephemeral overlay-backed controls (popup windows, context menus, date picker popups, dialogs, alerts) that materialize outside the primary scene graph.
- `advanced-javafx-demo-scenarios`: Add reference demo scenes and stable sample data for ContextMenu, MenuBar, DatePicker, Dialog, and Alert to support new automation validation.

## Impact

- Java agent must enumerate overlay `Stage` and `PopupWindow` nodes in addition to the primary scene graph.
- Python client gains new action methods: `right_click`, `open_menu`, `navigate_menu`, `pick_date`, `get_dialog`, `dismiss_dialog`.
- Demo app (`omniui/demo`) gains new JavaFX scenes for each new control group.
- New Python demo scripts added alongside existing advanced demos.
- No breaking changes to existing selectors or action signatures.
