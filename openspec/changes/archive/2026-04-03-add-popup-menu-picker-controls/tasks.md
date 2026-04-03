## 1. Java agent overlay foundation

- [x] 1.1 Extend Java agent to enumerate all open overlay windows using `Window.getWindows()`, covering `PopupWindow` and Dialog `Stage` instances
- [x] 1.2 Implement wait-for-overlay: after a popup-triggering action, poll for the expected overlay window type with configurable timeout
- [x] 1.3 Extend the `get_nodes()` endpoint to include overlay window nodes when an overlay is visible
- [x] 1.4 Add overlay node records annotated with window type (primary, popup, dialog)

## 2. ContextMenu automation

- [x] 2.1 Implement `right_click` action: dispatch right-click event on resolved node and wait for ContextMenu popup to become visible
- [x] 2.2 Implement single-level ContextMenu item selection: match by label text or `fx:id` and dispatch click
- [x] 2.3 Implement multi-level ContextMenu traversal: hover intermediate items sequentially per path, wait for each submenu popup before proceeding
- [x] 2.4 Implement `dismiss_menu`: dispatch Escape key event and wait for ContextMenu popup to close

## 3. MenuBar automation

- [x] 3.1 Implement `open_menu` action: locate top-level Menu item by label text or `fx:id`, dispatch click, and wait for menu popup to become visible
- [x] 3.2 Implement single-level MenuBar item selection: match by label text or `fx:id` in the open menu popup and dispatch click
- [x] 3.3 Implement `navigate_menu`: activate each submenu level via hover per path from the top-level header to the final item
- [x] 3.4 Extend `dismiss_menu` to support MenuBar menus: dispatch Escape key event and wait for menu popup to close

## 4. DatePicker automation

- [x] 4.1 Implement `open_datepicker` action: dispatch click on the DatePicker calendar toggle button and wait for the calendar popup to become visible
- [x] 4.2 Implement `navigate_month`: dispatch click on forward or backward navigation button and wait for the calendar grid to update
- [x] 4.3 Implement `pick_date`: identify target date cell by `LocalDate` item property, auto-navigate months as needed, then dispatch click

## 5. Dialog automation

- [x] 5.1 Implement `get_dialog`: scan `Window.getWindows()` for a Stage with a `DialogPane` root and return title, header text, content text, and button label list
- [x] 5.2 Implement `dismiss_dialog`: match a button in DialogPane's ButtonBar by label text or `ButtonData` type and dispatch click
- [x] 5.3 Implement error reporting when the requested button is not found, including the list of available button labels

## 6. Alert automation

- [x] 6.1 Extend `get_dialog` to identify Alerts: read `AlertType` and include `alert_type` field in the returned descriptor
- [x] 6.2 Verify all four AlertTypes (INFORMATION, CONFIRMATION, WARNING, ERROR) are correctly detected and returned
- [x] 6.3 Confirm that `dismiss_dialog` label text and `ButtonData` matching works correctly for Alerts

## 7. Python client new API

- [x] 7.1 Add `right_click(selector)` method
- [x] 7.2 Add `open_menu(text=None, id=None)` and `click_menu_item(text=None, id=None, path=None)` methods
- [x] 7.3 Add `navigate_menu(path)` method
- [x] 7.4 Add `dismiss_menu()` method
- [x] 7.5 Add `open_datepicker(selector)`, `navigate_month(direction)`, and `pick_date(date)` methods
- [x] 7.6 Add `get_dialog()` and `dismiss_dialog(button=None, button_type=None)` methods
- [x] 7.7 Confirm all new methods include control type, resolution tier, and action trace in their action result

## 8. Demo app extension

- [x] 8.1 Add ContextMenu demo scene: a labeled node with a registered ContextMenu containing stable single-level and multi-level items with unique labels
- [x] 8.2 Add MenuBar demo scene: at least two top-level menus each with stable single-level items and a multi-level submenu path with unique labels
- [x] 8.3 Add DatePicker demo scene: a DatePicker with a stable initial date value and a result label that reflects the selected date
- [x] 8.4 Add Dialog demo scene: a trigger button that opens a Dialog with stable title, header, content text, and OK/Cancel buttons
- [x] 8.5 Add Alert demo scene: four trigger buttons for each AlertType, each producing an Alert with a stable and uniquely identifiable content message

## 9. Python demo scripts

- [x] 9.1 Add ContextMenu demo script: right-click trigger, single-level item selection, multi-level traversal, print action trace
- [x] 9.2 Add MenuBar demo script: open top-level menu, single-level item selection, multi-level navigate_menu, print action trace
- [x] 9.3 Add DatePicker demo script: open popup, month navigation, date selection, print action trace
- [x] 9.4 Add Dialog demo script: detect dialog, read content, dismiss by label and by ButtonData, print action trace
- [x] 9.5 Add Alert demo script: detect all four AlertTypes, read messages, dismiss, print action trace
