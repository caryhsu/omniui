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
- JavaFX node discovery through a live JavaFX runtime reached by the local Java agent
- direct JavaFX `click`, `type`, and `get_text`
- Python APIs for `connect`, `get_nodes`, `click`, `type`, `get_text`, `verify_text`
- selector fallback chain: `javafx -> ocr -> vision`
- action tracing, retry-after-refresh, and action history
- recorder-lite script generation for stable click selectors
- reference login demo app and end-to-end demo script

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
mvn -pl demo/javafx-login-app -am package
```

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
