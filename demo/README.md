# OmniUI Demos

This directory collects runnable demos for OmniUI Phase 1.

## JavaFX target apps

Three focused demo apps are available under `demo/java/`:

| App | Port | Contents |
|-----|------|---------|
| [core-app](java/core-app/) | 48100 | Login flow, ComboBox, ListView, TreeView, TableView, GridPane |
| [input-app](java/input-app/) | 48101 | TextArea, PasswordField, Hyperlink, CheckBox, ChoiceBox, RadioButton, Slider, Spinner, ColorPicker, DatePicker |
| [advanced-app](java/advanced-app/) | 48102 | ContextMenu, MenuBar, Dialog, Alert, TabPane, Accordion, TreeTableView, SplitPane, ProgressBar, NodeState, ScrollPane, Tooltip |

Start the app you want to test:

```bash
demo/java/core-app/run-dev-with-agent.bat
demo/java/input-app/run-dev-with-agent.bat
demo/java/advanced-app/run-dev-with-agent.bat
```

Each app supports four launch modes: `run-dev-with-agent`, `run-dev-plain`, `run-with-agent` (jlink), `run-plain` (jlink).

Helper build scripts are available in [scripts](../scripts):
- `build_demo_runtime.ps1`
- `build_demo_runtime.bat`
- `build_demo_runtime.sh`

Note:
- The `.sh` helper currently targets Git Bash on Windows.

## Python demos

Python demos are organized under `demo/python/` to match the 3 apps:

| Subfolder | Targets | Port |
|-----------|---------|------|
| `demo/python/core/` | core-app | 48100 |
| `demo/python/input/` | input-app | 48101 |
| `demo/python/advanced/` | advanced-app | 48102 |

### Run everything

```bash
python demo/python/run_all.py
```

Starts each demo group in sequence. Requires all 3 apps to be running in with-agent mode.

You can also run:

```bash
python -m demo.python.run_all
python scripts/run_demo.py
```

### Run a specific group

```bash
# Core demos (requires core-app on port 48100)
python demo/python/core/login_direct.py
python demo/python/core/select_combo_role.py
python demo/python/core/discover_nodes.py

# Input demos (requires input-app on port 48101)
python demo/python/input/text_area_demo.py
python demo/python/input/date_picker_demo.py

# Advanced demos (requires advanced-app on port 48102)
python demo/python/advanced/context_menu_demo.py
python demo/python/advanced/discover_advanced_controls.py
```

### Benchmark

```bash
python demo/python/run_benchmark.py
```

Runs the Phase 1 benchmark and prints timing results for JavaFX node query and OCR parsing.
