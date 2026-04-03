# OmniUI Demos

This directory collects runnable demos for OmniUI Phase 1.

## Available Demos

### JavaFX target app

- [javafx-login-app](javafx-login-app/README.md)

This is the reference JavaFX application used by the Python demos below.

The current demo app includes:
- the original login flow
- visible advanced controls including `ComboBox`, `ListView`, `TreeView`, `TableView`, and a grid layout section

Start it first:

```bash
demo/javafx-login-app/run-dev-with-agent.bat
```

Or build a runtime image with `jlink` and run `run-with-agent.*` as documented in [javafx-login-app/README.md](javafx-login-app/README.md).

Helper scripts are available in [scripts](../scripts):
- `build_demo_runtime.ps1`
- `build_demo_runtime.bat`
- `build_demo_runtime.sh`

Those build helpers now print the exact `run-with-agent.*` and `run-plain.*` launchers to use next.

Note:
- The `.sh` helper currently targets Git Bash on Windows. The demo app and packaged launcher flow are still documented and validated primarily on Windows.

## Python demos

All Python demos assume the JavaFX login app is already running in `with-agent` mode on `http://127.0.0.1:48100`.

### Run everything

```bash
python demo/python/run_all.py
```

Runs discovery, direct login, fallback login, recorder preview, and benchmark in sequence.
It now also prints the visible advanced-control nodes before running the interaction demos.

You can also run:

```bash
python -m demo.python.run_all
python scripts/run_demo.py
```

### Discover nodes

```bash
python demo/python/discover_nodes.py
```

Lists the current JavaFX node snapshot returned by the agent.

### Discover advanced controls

```bash
python demo/python/discover_advanced_controls.py
```

Prints the currently visible advanced-control nodes from the JavaFX demo screen.

### Advanced control interactions

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

Runs direct JavaFX selection demos for `ComboBox`, `ListView`, `TreeView`, and `TableView`, then verifies each status label.

### Direct login flow

```bash
python demo/python/login_direct.py
```

Runs the login flow using direct JavaFX selectors such as `id="loginButton"`.

### Login flow with OCR fallback

```bash
python demo/python/login_with_fallback.py
```

Runs the login flow with the final click expressed as `text="Login"` so the trace can show fallback behavior when applicable.

### Recorder preview

```bash
python demo/python/recorder_preview.py
```

Runs a small flow and prints the recorder-lite output generated from action history.

### Benchmark

```bash
python demo/python/run_benchmark.py
```

Runs the Phase 1 benchmark and prints timing results for JavaFX node query and OCR parsing.
