# HTML Test Report

OmniUI integrates with [pytest-html](https://pytest-html.readthedocs.io/) to
generate self-contained HTML test reports.  On failure, a **screenshot** of the
JavaFX app is automatically embedded in the report so you can see the exact
state of the UI when the test failed.

## Quick Start

```bash
pip install -e ".[dev]"          # pytest-html is included in dev extras

pytest tests/ --html=report.html --self-contained-html
```

Open `report.html` in any browser — no server needed.

## Features

| Feature | Description |
|---------|-------------|
| **Self-contained** | Single `.html` file — no external assets |
| **Screenshot on failure** | App screenshot embedded inline for every failed test |
| **Trace log on failure** | `format_trace()` output printed to the captured log |
| **Screenshot file** | PNG also saved to `screenshots/<test_name>.png` |
| **Custom title** | Report title is "OmniUI Test Report" |

## Configuration

You can set a default HTML output path in `pyproject.toml` so you don't need
to type it every time:

```toml
[tool.pytest.ini_options]
addopts = "--html=report.html --self-contained-html"
```

## CI / GitHub Actions

In CI the HTML report is automatically generated and uploaded as a workflow
artifact:

- **Unit tests** → artifact `unit-test-report` → `report.html`
- **Integration tests** → artifact `integration-test-report` → `report.html`

Download the artifact from the **Actions** tab on GitHub after a run completes.

## How Screenshots Work

The `omniui` pytest fixture captures a screenshot when a test fails:

```
conftest.py
  └─ pytest_runtest_makereport hook
       └─ if failed → client.screenshot()
            ├─ saves  screenshots/<test_name>.png
            └─ embeds base64 PNG into pytest-html report via extras API
```

The screenshot logic is **opt-in per test**: it only activates for tests that
use the `omniui` fixture.  Pure unit tests are not affected.

## Writing Tests

```python
def test_login_success(omniui):
    omniui.type(id="username", text="admin")
    omniui.type(id="password", text="secret")
    omniui.click(id="loginButton")
    omniui.verify_text(id="status", expected="Welcome, admin!")
```

On failure the report shows:

1. Test name and failure reason
2. Captured stdout (including the trace log)
3. Inline screenshot of the JavaFX window
