# Manual Smoke Checklist

[繁體中文版本](manual-smoke.zh-TW.md)

Use this checklist to validate the current OmniUI Java agent workflow manually on a local machine.

## Scope

This checklist targets:
- the three JavaFX demo apps (`core-app`, `input-app`, `advanced-app`)
- standard Java agent startup
- Python demo flows
- plain-mode isolation

It is intended for local validation on the currently supported Windows-based demo environment.

## Prerequisites

- Java 21+
- Maven 3.9+
- Python 3.11+
- A clean build of the Java agent and demo apps if you recently changed code

## Development Mode Smoke

### 1. Start the core-app in with-agent mode

Run one of:

```bat
demo\java\core-app\run-dev-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-dev-with-agent.ps1
```

```bash
./demo/java/core-app/run-dev-with-agent.sh
```

Expected:
- the JavaFX core demo window opens
- the console prints `OmniUI agent listening on http://127.0.0.1:48100`

### 2. Run the core Python demo suite

In another terminal:

```bash
python demo/python/run_all.py
```

Expected:
- All 20+ component demos print `succeeded` or `succeeded ✓`
- Exit code 0

Individual demos that are validated:
- Login (direct + fallback), keyboard shortcuts, double-click, flexible verify
- ComboBox, ListView, TreeView, TableView selection, multi-select, index selector
- CSS style inspection, recorder preview
- (Input) TextArea, PasswordField, Hyperlink, CheckBox, ChoiceBox
- (Input) RadioButton, Slider+Spinner, ColorPicker, DatePicker
- (Advanced) ContextMenu, MenuBar, Dialog, Alert, TabPane
- (Advanced) Accordion, TreeTableView, SplitPane, ProgressBar, NodeState, ScrollPane, Tooltip

### 3. Close the app

Expected:
- the JavaFX window closes
- the process exits cleanly

## Plain Mode Isolation Smoke

### 4. Start the core-app in plain mode

Run one of:

```bat
demo\java\core-app\run-dev-plain.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-dev-plain.ps1
```

```bash
./demo/java/core-app/run-dev-plain.sh
```

Expected:
- the JavaFX core demo window opens
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

### 6. Build the packaged runtimes

> **Important:** The jlink image embeds `dev.omniui.agent` as a module. Always install
> the agent to the local Maven repository first, or agent changes will not be reflected
> in the packaged runtimes.

```bash
mvn install -f java-agent/pom.xml
mvn package javafx:jlink -f demo/java/core-app/pom.xml
mvn package javafx:jlink -f demo/java/input-app/pom.xml
mvn package javafx:jlink -f demo/java/advanced-app/pom.xml
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

### 7. Start the packaged core-app in with-agent mode

Run one of:

```bat
demo\java\core-app\run-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-with-agent.ps1
```

```bash
./demo/java/core-app/run-with-agent.sh
```

Expected:
- the JavaFX core demo window opens
- the agent listens on `http://127.0.0.1:48100`

### 8. Run the full demo suite against the packaged runtime

```bash
python demo/python/run_all.py
```

Expected: same as step 2 — all demos succeed, exit code 0.

## Advanced Control Smoke

### 9. Run the advanced discovery script

Start `advanced-app` first (`demo\java\advanced-app\run-dev-with-agent.bat`, port 48102), then:

```bash
python demo/python/advanced/discover_advanced_controls.py
```

Expected:
- output includes advanced control node IDs

### 10. Run individual component demos (optional — all are covered by run_all.py)

```bash
python demo/python/core/select_combo_role.py
python demo/python/core/select_list_item.py
python demo/python/core/select_tree_item.py
python demo/python/core/select_table_row.py
python demo/python/advanced/context_menu_demo.py
python demo/python/advanced/menu_bar_demo.py
python demo/python/input/date_picker_demo.py
python demo/python/advanced/alert_demo.py
python demo/python/advanced/treetableview_demo.py
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
