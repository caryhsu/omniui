## Why

OmniUI currently only supports single-item selection (`select(value=...)`). Many real-world JavaFX UIs use `ListView` and `TableView` in `MULTIPLE` selection mode — e.g. batch-delete, bulk-assign, multi-file upload. Without multi-select support, automation scripts cannot fully test these workflows.

## What Changes

- Add `select_multiple(id=..., values=[...])` to the Python client — selects a list of items by value in a `ListView` or `TableView` with `MULTIPLE` selection mode enabled
- Add `get_selected_items()` to the Python client — returns all currently selected values in a multi-select control
- Java agent: add `select_multiple` and `get_selected_items` action handlers
- Demo app: switch `serverList` ListView to `SelectionMode.MULTIPLE`; add `get_selected_items` verification
- Demo: `multi_select_demo.py` smoke test

## Capabilities

### New Capabilities

- `multi-select`: Python and Java agent support for selecting multiple items and reading the full selection from ListView and TableView

### Modified Capabilities

- `python-automation-client`: add `select_multiple` and `get_selected_items` methods to the public API surface

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new cases in `performOnFxThread()`
- `omniui/core/engine.py` — two new public methods
- `demo/javafx-login-app/src/main/java/dev/omniui/demo/LoginDemoApp.java` — enable MULTIPLE selection on `serverList`
- `demo/python/multi_select_demo.py` — new demo
- `demo/python/run_all.py` — add entry
- `docs/api/python-client.md`, `README.md`, `README.zh-TW.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- Java agent jlink rebuild required
