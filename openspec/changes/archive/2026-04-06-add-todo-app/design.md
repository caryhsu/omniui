## Context

OmniUI already supports all the JavaFX automation primitives needed: `click`, `type`, `select`, `set_date`, `get_text`, `get_selected`. The existing demo apps (color-app, image-app, drag-app) each exercise one or two controls. The Todo App is the first multi-control CRUD demo, validating that OmniUI can handle realistic task-management workflows.

Port allocation: 48107 (next after color-app 48106).

## Goals / Non-Goals

**Goals:**
- Standalone JavaFX `todo-app` at port 48107 with full CRUD: Add, Edit, Delete tasks
- `ListView<Task>` with `ListCell<Task>` cells — each cell contains a CheckBox (completion), title Label, priority badge Label, and due-date Label
- ToolBar: Add, Edit, Delete buttons + Show Completed ToggleButton + Search TextField
- Bottom Input Panel: title TextField, Priority ComboBox (Low/Medium/High), Due Date DatePicker, Add Button
- Edit flow: select item → bottom panel pre-fills → edit → click ✎ Edit to save
- Python demo script exercising: add task, check task complete, search, delete, show-completed toggle
- `run_all.py` integration

**Non-Goals:**
- Persistence (no DB / file storage — in-memory only)
- Task sorting or reordering
- Multi-select delete
- Sub-tasks or task hierarchy

## Decisions

### D1: Task data model as a simple POJO (not JavaFX Properties)
**Decision:** `Task` class with plain fields (`String title`, `String priority`, `String dueDate`, `boolean completed`).  
**Rationale:** Simplest approach; no need for reactive binding. The ListView refreshes via `getItems().setAll(...)`.  
**Alternative:** `SimpleStringProperty` / `SimpleBooleanProperty` fields with two-way bindings — more idiomatic JavaFX but adds complexity with no automation benefit.

### D2: CheckBox in ListCell triggers immediate completion toggle
**Decision:** Clicking the CheckBox in a cell directly toggles `task.completed` and refreshes the list.  
**Rationale:** Direct affordance for automation — `click(text="Buy groceries")` selects the row; a separate `click` on the CheckBox (by cell position) toggles completion. The CheckBox is accessible via `id="check_<index>"`.  
**Alternative:** A "Mark Done" toolbar button — less discoverable, breaks single-interaction UX.

### D3: Cell text representation uses task title as primary identifier
**Decision:** Each `ListCell` renders title as the primary `Label` text, making `click(text="Buy groceries")` work for selection.  
**Rationale:** OmniUI's `click(text=...)` matches against node text; title is the most stable unique identifier for a task.

### D4: Edit flow uses the bottom Input Panel (no popup dialog)
**Decision:** Selecting a task pre-fills the bottom Input Panel. Clicking **✎ Edit** saves changes.  
**Rationale:** No new JavaFX Dialog automation required. Reuses existing `type()` / `select()` / `set_date()` on the same controls used for Add.

### D5: Search is real-time filter on the ListView
**Decision:** `searchField` `textProperty().addListener(...)` filters the ListView on every keystroke.  
**Rationale:** Immediate feedback; automation can `type("Buy", id="searchField")` and then verify filtered results.

### D6: Show Completed is a ToggleButton (not a CheckBox)
**Decision:** ToggleButton with id `"showCompleted"` — selected = show all, deselected = hide completed.  
**Rationale:** ToggleButton is visually a button (matches ToolBar style). OmniUI `click()` toggles it; `get_selected()` reads state.

## Risks / Trade-offs

- **CheckBox cell addressing** → The CheckBox inside each cell doesn't have a globally unique fxId (only `check_0`, `check_1`, etc. by index). Automation must select the row first, then interact by index. Mitigation: Python demo script uses row index explicitly.
- **List refresh after edit** → After edit, `listView.getItems().setAll(...)` resets selection. Demo script re-selects if needed. Mitigation: assert by text after operation.
- **Real-time search + Show Completed interaction** → Filtering hides items; automation must account for filtered state. Mitigation: clear search before delete/edit operations.
