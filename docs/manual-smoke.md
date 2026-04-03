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

### 2. Run the Python demo suite

In another terminal:

```bash
python scripts/run_demo.py
```

Expected:
- node discovery prints JavaFX nodes
- advanced discovery prints `ComboBox`, `ListView`, `TreeView`, `TableView`, and grid-oriented demo nodes
- advanced interaction demos succeed for combo-box selection, list selection, tree selection, and table-row selection
- direct login succeeds
- fallback login succeeds
- recorder preview prints script lines
- benchmark prints JSON output

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

Run one of:

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

### 8. Run a focused Python flow

```bash
python scripts/demo_login_flow.py
```

Expected:
- login flow succeeds
- trace history prints
- recorder output prints stable click lines

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

### 10. Run the advanced interaction demos

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

Expected:
- combo-box demo prints `ComboBox selection succeeded`
- list-view demo prints `ListView selection succeeded`
- tree-view demo prints `TreeView selection succeeded`
- table-view demo prints `TableView selection succeeded`
- the JavaFX window reflects the corresponding status-label updates

### 11. Record any intentionally unsupported cases

Capture whether any of these cases still need follow-up:
- off-screen or non-materialized list/tree/table items
- combo-box popup interaction beyond direct selection-model control
- tree expand/collapse behavior beyond selecting visible items
- table row disambiguation when multiple rows share the same value

## Record Results

Capture at least:
- launcher used
- whether agent startup message appeared
- whether `run_demo.py` succeeded in with-agent mode
- whether advanced discovery returned the expected control ids
- whether advanced interaction demos succeeded
- whether plain mode failed clearly
- any console errors or stack traces
