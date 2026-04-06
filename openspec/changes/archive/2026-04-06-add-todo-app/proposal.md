## Why

OmniUI lacks a realistic CRUD demo that combines multiple JavaFX controls in a single workflow. A Todo App exercises ListView with custom cells, ComboBox, DatePicker, TextField, ToolBar buttons, and CheckBox together — validating that OmniUI can automate real task-management UIs end-to-end.

## What Changes

- New standalone JavaFX demo app (`todo-app`, port 48107) with a `BorderPane` layout
- `ListView<Task>` with custom `ListCell<Task>` cells: each cell shows title, priority badge, due date, and an inline **CheckBox** for marking completion
- ToolBar with **Add**, **Edit**, **Delete**, **Show Completed** (ToggleButton), and a **Search** TextField
- Bottom Input Panel: title TextField, Priority ComboBox (Low / Medium / High), Due Date DatePicker, Add Button
- Edit flow: select an item in the list → bottom panel pre-fills → modify fields → click **Edit** to save
- New Python demo script (`demo/python/todo/todo_demo.py`) exercising the full CRUD + filter workflow
- `run_all.py` integration (port 48107)

## Capabilities

### New Capabilities

- `todo-app-demo-scenarios`: End-to-end automation scenarios for the Todo App (add task, edit task, delete task, mark complete via checkbox, search/filter, show-completed toggle)

### Modified Capabilities

- `javafx-automation-core`: No requirement changes — all needed actions (click, type, select, set_date, get_text, get_selected) already exist

## Impact

- New Java Maven module: `demo/java/todo-app/`
- New Python package: `demo/python/todo/`
- `demo/python/run_all.py` gains a Todo App section
- No changes to core OmniUI engine or Java agent (all required automation primitives already supported)
