# OmniUI

[繁體中文說明](README.zh-TW.md)

OmniUI is a multi-modal UI automation framework with a JavaFX-first execution path.

Phase 1 focuses on local JavaFX automation with this priority order:
- JavaFX scene graph resolution and direct event-level interaction
- OCR text fallback
- Vision template-match fallback

The current repo includes:
- a Python client in [omniui](omniui)
- a local HTTP Java agent in [java-agent](java-agent)
- a reference JavaFX login app in [demo/javafx-login-app](demo/javafx-login-app)
- demo and benchmark scripts in [scripts](scripts)

## Status

Implemented in this repo today:

**Core infrastructure**
- JavaFX node discovery through a live JavaFX runtime reached by the local Java agent
- selector fallback chain: `javafx -> ocr -> vision`
- action tracing, retry-after-refresh, and action history
- recorder-lite script generation for stable click selectors

**Actions — basic interaction**
- `click`, `right_click`, `type`, `get_text`, `verify_text`
- `select` (ComboBox / ChoiceBox / ListView), `get_selected`, `set_selected` (CheckBox / RadioButton / ToggleButton)
- `is_visible`, `is_enabled` — query node visibility / enabled state
- `wait_for_text`, `wait_for_visible`, `wait_for_enabled`, `wait_for_node`, `wait_for_value` — poll-based wait conditions

**Actions — menus**
- `open_menu`, `navigate_menu`, `dismiss_menu`, `click_menu_item` (MenuBar + ContextMenu)

**Actions — DatePicker**
- `open_datepicker`, `navigate_month`, `pick_date`, `set_date`

**Actions — dialogs**
- `get_dialog`, `dismiss_dialog` (Alert: information / warning / error / confirmation)

**Actions — form controls**
- `set_slider`, `set_spinner`, `step_spinner`, `get_progress`, `get_value`

**Actions — tabs**
- `get_tabs`, `select_tab`

**Actions — Accordion**
- `expand_pane`, `collapse_pane`, `get_expanded`

**Actions — Hyperlink**
- `get_visited`

**Actions — TreeTableView**
- `select_tree_table_row`, `get_tree_table_cell`
- `expand_tree_table_item`, `collapse_tree_table_item`, `get_tree_table_expanded`

**Actions — ColorPicker**
- `set_color`, `get_color`, `open_colorpicker`, `dismiss_colorpicker`

**Actions — SplitPane**
- `get_divider_positions`, `set_divider_position`

**Demo suite** (all passing via `python demo/python/run_all.py`)
- Login, ComboBox, ListView, TableView, TreeView, ContextMenu, MenuBar, DatePicker, Alert
- RadioButton, Slider+Spinner, Progress, Tab, TextArea, PasswordField, Hyperlink
- CheckBox, ChoiceBox, Accordion, TreeTableView, ColorPicker, SplitPane, Node State, Wait Conditions

Not implemented yet:
- dynamic JVM attach to arbitrary third-party JavaFX processes
- real OCR engine integration such as Tesseract or PaddleOCR
- real template-matching backend such as OpenCV
- OS-level click dispatch for fallback bounds

## Layout

```text
omniui/
  core/
  selector_engine/
  ocr_module/
  vision_module/
  recorder_lite/
java-agent/
demo/javafx-login-app/
scripts/
openspec/
```

## Prerequisites

- Python 3.11+
- Java 21+
- Maven 3.9+
- Windows is the currently validated desktop target for the demo app build

## Quick Start

1. Start the JavaFX demo app:

```bash
demo\javafx-login-app\run-dev-with-agent.bat
```

This launches the sample login window with the OmniUI Java agent enabled on `http://127.0.0.1:48100`.

2. In another terminal, run the Python demo flow:

```bash
python scripts/demo_login_flow.py
```

Expected behavior:
- username and password fields are driven through JavaFX direct interaction
- the login button click is demonstrated through OCR fallback using `text="Login"`
- the script verifies that the status label becomes `Success`

3. Optional: run the benchmark script while the demo app is still running:

```bash
python scripts/benchmark_phase1.py
```

This reports average timings for:
- JavaFX node query
- OCR fallback parsing

More runnable demos:
- [demo/README.md](demo/README.md)

Single command demo entry:

```bash
python scripts/run_demo.py
```

Packaged demo runtime helpers:
- `scripts/build_demo_runtime.ps1`
- `scripts/build_demo_runtime.bat`
- `scripts/build_demo_runtime.sh`

After building, those helpers print the exact packaged `with-agent` and `plain` launchers to use next.

The `.sh` helper is currently intended for Git Bash on Windows. The demo app and packaged launcher workflow are still primarily documented and validated on Windows.

## Python API

Documentation hub:

- [docs/index.md](docs/index.md)
- [Architecture diagrams](docs/architecture.md)
- [Manual smoke checklist](docs/manual-smoke.md)

Full API reference:

- [docs/api/python-client.md](docs/api/python-client.md)

Minimal example:

```python
from omniui import OmniUI

client = OmniUI.connect(app_name="LoginDemo")

client.click(id="username")
client.type("admin", id="username")

client.click(id="password")
client.type("1234", id="password")

client.click(text="Login")
client.verify_text(id="status", expected="Success")
```

Use the full API reference for parameters, result models, fallback semantics, and return fields.

## Recorder-lite

Recorder-lite currently works from the client action history and only emits stable click expressions.

Example output:

```text
click(id="username")
click(text="Login")
```

Unsupported interactions are intentionally skipped instead of falling back to raw coordinates.

## Testing

Run the Python test suite:

```bash
python -m unittest tests.test_agent_contracts tests.test_client tests.test_demo_script tests.test_recorder tests.test_benchmark tests.test_markdown_i18n
```

Build the Java agent and demo app:

```bash
# Install agent module to local Maven repo first
mvn install -f java-agent/pom.xml
# Then build the jlink runtime image (which embeds the agent module)
mvn package javafx:jlink -f demo/javafx-login-app/pom.xml
```

> **Note:** The jlink runtime image embeds `dev.omniui.agent` as a module. Always run
> `mvn install -f java-agent/pom.xml` before rebuilding the jlink image, or agent
> changes will not be picked up.

Check markdown i18n consistency:

```bash
python scripts/check_markdown_i18n.py
```

## OpenSpec

The implementation was developed through the OpenSpec workflow. Change artifacts are under:

[openspec/changes/add-omniui-javafx-automation-framework](openspec/changes/add-omniui-javafx-automation-framework)

Key artifacts:
- [proposal.md](openspec/changes/add-omniui-javafx-automation-framework/proposal.md)
- [design.md](openspec/changes/add-omniui-javafx-automation-framework/design.md)
- [tasks.md](openspec/changes/add-omniui-javafx-automation-framework/tasks.md)

## Notes

- The agent protocol is documented in [docs/protocol/agent-protocol.md](docs/protocol/agent-protocol.md).
- The Java agent is now the owner of HTTP startup and JavaFX runtime discovery for the demo support path.
- The fallback engines in this repo are deterministic placeholder implementations intended to keep the Phase 1 architecture executable before integrating production OCR and vision libraries.
