## 1. Java Demo App — Task Model

- [x] 1.1 Create `demo/java/todo-app/pom.xml` (port 48107, artifact `omniui-todo-demo`, parent `omniui-demos`)
- [x] 1.2 Create `demo/java/todo-app/src/main/java/module-info.java`
- [x] 1.3 Implement `Task.java`: fields `String title`, `String priority`, `String dueDate`, `boolean completed`; constructor + getters/setters

## 2. Java Demo App — UI

- [x] 2.1 Implement `TaskCell.java` (ListCell<Task>): HBox with CheckBox (id `check_<index>`), title Label, priority Label (badge style), due-date Label; strikethrough on completed
- [x] 2.2 Implement `TodoDemoApp.java` — BorderPane layout:
  - Top ToolBar: `addButton`, `editButton`, `deleteButton`, Separator, `showCompleted` ToggleButton, `searchField` TextField
  - Center: `taskList` ListView<Task> with TaskCell factory
  - Bottom: HBox with `taskTitleField`, `priorityCombo` (Low/Medium/High), `dueDatePicker`, `addButton2` (id `"addButton"`)
- [x] 2.3 Wire Add logic: `addButton` clicks → validate title not empty → create Task → add to master list → refresh
- [x] 2.4 Wire Select logic: `taskList` selection change → pre-fill bottom Input Panel
- [x] 2.5 Wire Edit logic: `editButton` → update selected task fields → refresh list
- [x] 2.6 Wire Delete logic: `deleteButton` → remove selected task → refresh list
- [x] 2.7 Wire CheckBox toggle in TaskCell → toggle `task.completed` → refresh ListView
- [x] 2.8 Wire Search filter: `searchField` textProperty listener → filter by title (case-insensitive)
- [x] 2.9 Wire Show Completed toggle: `showCompleted` ToggleButton → include/exclude completed tasks in filter
- [x] 2.10 Wire Add button disabled when `taskTitleField` is empty
- [x] 2.11 Create `demo/java/todo-app/run-dev-with-agent.bat` (port 48107)
- [x] 2.12 Confirm `mvn -f demo/java/todo-app/pom.xml compile` succeeds

## 3. Python Demo Package

- [x] 3.1 Create `demo/python/todo/__init__.py`, `_bootstrap.py`, `_runtime.py` (port 48107)
- [x] 3.2 Implement `demo/python/todo/todo_demo.py`:
  - Add task "Buy groceries" (priority High, date 2026-04-30) → assert appears in list
  - Add task "Call doctor" (priority Medium, date 2026-05-01) → assert appears in list
  - Select "Buy groceries" → assert bottom panel pre-fills (title)
  - Edit title to "Buy groceries & vegetables" → click editButton → assert list updated
  - Click CheckBox for "Call doctor" (index 1) → assert completed state
  - Type "Buy" in searchField → assert only "Buy groceries & vegetables" visible (get_text on items)
  - Clear searchField → assert both items visible
  - Delete "Buy groceries & vegetables" → assert removed from list
  - Click showCompleted toggle → assert "Call doctor" becomes visible
  - Print "todo_demo succeeded (ok)"

## 4. Integration

- [x] 4.1 Add todo-app import and section to `demo/python/run_all.py` (port 48107)
- [x] 4.2 Run `python -m pytest tests/ -q` — confirm baseline tests still pass
