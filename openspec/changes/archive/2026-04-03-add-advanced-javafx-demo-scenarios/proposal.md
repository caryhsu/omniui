## Why

The current JavaFX login demo proves the Phase 1 vertical slice, but it only exercises simple text fields, a button, and a label. That is not enough to validate how OmniUI behaves against real JavaFX control patterns such as virtualized lists, selection-based controls, hierarchical views, and tabular data.

## What Changes

- Add additional JavaFX demo screens or flows covering `ComboBox`, `ListView`, `TreeView`, `TableView`, and grid-like layouts.
- Expand demo-driven validation so OmniUI can exercise selection, expansion, and item-oriented interactions beyond simple button and text-input flows.
- Identify and document where current JavaFX discovery and action models are sufficient, and where they require contract changes for more complex controls.
- Add runnable Python demo coverage for the new scenarios so discovery, action execution, and fallback behavior can be inspected against richer UI structures.

## Capabilities

### New Capabilities

- `advanced-javafx-demo-scenarios`: Reference JavaFX demo scenarios for complex controls such as combo boxes, lists, trees, tables, and grid-oriented layouts.

### Modified Capabilities

- `javafx-automation-core`: JavaFX discovery and action requirements must cover more complex control structures, including selection-driven and virtualized controls.
- `python-automation-client`: The client contract may need to support higher-level interaction patterns for advanced JavaFX controls if `click` and `type` are not sufficient.

## Impact

- Affected code: `demo/javafx-login-app/`, `demo/python/`, `scripts/`, and JavaFX adapter logic in `java-agent/`.
- Affected test scope: new scenario validation and regression checks for complex JavaFX controls.
- Affected API surface: possible additions or clarifications around selection-oriented actions and richer control-state inspection.
