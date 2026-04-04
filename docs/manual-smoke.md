# Manual Smoke Checklist

[繁體中文版本](manual-smoke.zh-TW.md)

Use this checklist to validate the current OmniUI Java agent workflow manually on a local machine.

## Scope

This checklist targets:
- the JavaFX login demo app
- standard Java agent startup
- Python demo flows
- plain-mode isolation

It is intended for local validation on the currently supported Windows-based demo environment.

## Prerequisites

- Java 21+
- Maven 3.9+
- Python 3.11+
- A clean build of the Java agent and demo app if you recently changed code

## Development Mode Smoke

### 1. Start the app in with-agent mode

Run one of:

```bat
demo\javafx-login-app\run-dev-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-with-agent.ps1
```

```bash
./demo/javafx-login-app/run-dev-with-agent.sh
```

Expected:
- the JavaFX login window opens
- the console prints `OmniUI agent listening on http://127.0.0.1:48100`

### 2. Run the full Python demo suite

In another terminal:

```bash
python demo/python/run_all.py
```

Expected:
- All 20+ component demos print `succeeded` or `succeeded ✓`
- Exit code 0

Individual demos that are validated:
- Login (direct + fallback)
- ComboBox, ListView, TreeView, TableView selection
- ContextMenu (single-level and submenu)
- MenuBar navigation (single-level and nested)
- DatePicker `set_date`
- Alert dialogs (information, warning, error, confirmation)
- RadioButton, Slider+Spinner, ProgressBar, TabPane
- TextArea, PasswordField, Hyperlink
- CheckBox, ChoiceBox, Accordion, TreeTableView

### 3. Close the app

Expected:
- the JavaFX window closes
- the process exits cleanly

## Plain Mode Isolation Smoke

### 4. Start the app in plain mode

Run one of:

```bat
demo\javafx-login-app\run-dev-plain.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-plain.ps1
```

```bash
./demo/javafx-login-app/run-dev-plain.sh
```

Expected:
- the JavaFX login window opens
- no OmniUI agent startup message is printed

### 5. Attempt a Python demo against plain mode

In another terminal:

```bash
python scripts/run_demo.py
```

Expected:
- the script exits clearly
- the message tells you to start the app in `with-agent` mode
- it does not silently connect to a fallback target

## Packaged Runtime Smoke

### 6. Build the packaged runtime

> **Important:** The jlink image embeds `dev.omniui.agent` as a module. Always install
> the agent to the local Maven repository first, or agent changes will not be reflected
> in the packaged runtime.

```bash
mvn install -f java-agent/pom.xml
mvn package javafx:jlink -f demo/javafx-login-app/pom.xml
```

Or run the helper scripts (which call the same Maven goals):

```bat
scripts\build_demo_runtime.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build_demo_runtime.ps1
```

```bash
./scripts/build_demo_runtime.sh
```

Expected:
- build completes successfully
- the script prints the next-step `run-with-agent.*` and `run-plain.*` launchers

### 7. Start the packaged app in with-agent mode

Run one of:

```bat
demo\javafx-login-app\run-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-with-agent.ps1
```

```bash
./demo/javafx-login-app/run-with-agent.sh
```

Expected:
- the JavaFX login window opens
- the agent listens on `http://127.0.0.1:48100`

### 8. Run the full demo suite against the packaged runtime

```bash
python demo/python/run_all.py
```

Expected: same as step 2 — all demos succeed, exit code 0.

## Advanced Control Smoke

### 9. Run the advanced discovery script

```bash
python demo/python/discover_advanced_controls.py
```

Expected:
- output includes `roleCombo`
- output includes `serverList`
- output includes `assetTree`
- output includes `userTable`
- output includes `settingsGrid`

### 10. Run individual component demos (optional — all are covered by run_all.py)

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
python demo/python/context_menu_demo.py
python demo/python/menu_bar_demo.py
python demo/python/date_picker_demo.py
python demo/python/alert_demo.py
python demo/python/treetableview_demo.py
```

Expected:
- each demo prints `succeeded` or `succeeded ✓`
- the JavaFX window reflects the corresponding UI updates

## Record Results

Capture at least:
- launcher used
- whether agent startup message appeared
- whether `run_all.py` succeeded in with-agent mode (exit code 0)
- whether plain mode failed clearly
- any console errors or stack traces
