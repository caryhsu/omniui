## Context

The current OmniUI reference app exercises only a narrow JavaFX slice: text input, button click, and status verification. That is useful for proving the agent and Python control path, but it does not expose the harder JavaFX control patterns that typically break automation frameworks first: virtualized controls, popup-based selectors, hierarchical views, and selection models that are not well-represented by raw click semantics.

This change is intentionally demo-driven. The goal is not to claim full support for every complex JavaFX control immediately, but to build representative scenarios that force the Java agent, selector model, and Python API to confront real control behavior. That will reveal which interactions can remain simple `click` / `type` calls and which need explicit higher-level contracts such as selection or expansion.

Current constraints:
- The Java agent and Python client integration already exist and should remain the base execution path.
- The demo app should stay reference-quality: readable, stable, and easy to run manually.
- The new scenarios should be chosen to reveal structural automation gaps, not to maximize UI volume.
- The repo still uses placeholder OCR and vision backends, so the primary focus remains JavaFX structural interaction.

## Goals / Non-Goals

**Goals:**
- Add representative JavaFX demo scenarios for `ComboBox`, `ListView`, `TreeView`, `TableView`, and grid-oriented layouts.
- Use those scenarios to validate current JavaFX discovery against virtualized, popup-driven, and hierarchical controls.
- Determine which advanced interactions can be expressed with existing actions and which require explicit new action contracts.
- Keep the demo navigable for both manual smoke testing and Python-driven validation.
- Produce runnable Python demos that expose how OmniUI behaves on richer controls, including where support is partial or intentionally constrained.

**Non-Goals:**
- Full production-grade support for every JavaFX control or skin implementation.
- Broad support for drag-and-drop, inline cell editing, keyboard navigation coverage, or custom third-party controls in this change.
- Replacing OCR or vision placeholders with production backends.
- Solving all virtualization edge cases across arbitrarily large datasets.
- Expanding beyond JavaFX in this change.

## Decisions

### 1. Organize advanced controls as explicit demo scenarios, not as one oversized screen

The demo app should expose separate, named scenarios or tabs for advanced controls rather than placing every control into one dense window.

Recommended scenario grouping:
- selection controls: `ComboBox`, `ListView`
- hierarchical controls: `TreeView`
- tabular/grid controls: `TableView`, grid-like layout

Rationale:
- Keeps scenario intent readable for both humans and Python scripts.
- Makes failures easier to isolate and document.
- Avoids a giant scene graph whose noise obscures what each test is exercising.

Alternatives considered:
- One all-in-one showcase screen: rejected because it increases scene complexity and weakens failure isolation.
- Separate standalone demo apps per control: rejected for now because it adds too much operator overhead.

### 2. Treat selection-oriented interactions as first-class candidates for new action contracts

The design should assume that some advanced controls are better modeled with actions such as `select`, `expand`, and `collapse` rather than only `click`.

Expected mapping:
- `ComboBox`: open/select item, then verify selected value
- `ListView`: select item by text or index, then verify selection
- `TreeView`: expand/collapse/select tree item
- `TableView`: select row or cell, then read visible values

Rationale:
- These controls are driven by selection state, not just pointer activation.
- Forcing every interaction through `click` makes scripts brittle and hides semantic intent.
- A demo-driven change is the right place to decide whether the public API needs new high-level actions.

Alternatives considered:
- Keep all interactions as raw `click` for now: acceptable only as a temporary fallback, but rejected as the main design direction because it obscures required contracts.

### 3. Model virtualized controls around visible state first

For `ListView`, `TreeView`, and `TableView`, the first implementation should explicitly focus on visible and currently materialized UI state.

Implications:
- discovery may only guarantee access to visible cells or rows
- Python demos should use datasets sized to make expected items visible without scrolling in the first pass
- follow-up changes can add scroll-aware resolution and off-screen item handling later

Rationale:
- JavaFX virtualized controls recycle cells and do not guarantee stable nodes for off-screen content.
- A visibility-scoped first pass provides immediate value without pretending the current model fully solves virtualization.
- It keeps scenario behavior deterministic for smoke testing.

Alternatives considered:
- Attempt full scroll and virtualization handling immediately: rejected because it adds too much complexity before the baseline semantics are established.

### 4. Treat popup-backed controls as separate window-discovery cases

Controls such as `ComboBox` may render popup content outside the primary scene root. The JavaFX discovery model must therefore expect multiple windows or popup containers to appear during interaction.

Rationale:
- The current agent-side discovery already moved toward window-based runtime detection.
- Popup controls are a direct test of whether discovery can see beyond the base scene root.
- Without this assumption, `ComboBox` demos would provide false confidence.

Alternatives considered:
- Avoid popup controls until discovery is improved: rejected because popup-backed controls are too common to defer.

### 5. Use demo data and selectors that reveal structure clearly

The advanced demo scenarios should use small, named datasets with stable visible text and IDs where appropriate.

Examples:
- `ComboBox` options: clear labeled values such as `Admin`, `Operator`, `Viewer`
- `ListView` items: short, unique entries
- `TreeView` items: explicit parent-child labels
- `TableView` rows: stable sample records with identifiable columns

Rationale:
- Makes discovery output readable.
- Simplifies selector design and regression assertions.
- Helps distinguish control support issues from ambiguous test data.

Alternatives considered:
- Randomized or large synthetic data: rejected because it makes debugging harder during contract exploration.

### 6. Add scenario-specific Python demos instead of overloading the existing login flow

The existing login flow should remain intact, while new Python demos should be added for advanced controls.

Expected additions:
- discovery demo for advanced scenarios
- selection demo
- tree/table demo
- optional smoke script that runs a curated subset of advanced interactions

Rationale:
- Preserves the current Phase 1 vertical slice as a stable baseline.
- Lets advanced-control validation evolve independently.
- Keeps `run_demo.py` and documentation understandable.

Alternatives considered:
- Extend the current login flow script until it covers everything: rejected because it mixes unrelated interaction models into one script.

## Risks / Trade-offs

- [Virtualized controls may expose only visible cells, leading to partial discovery] -> Mitigation: explicitly scope first-pass requirements to visible state and use deterministic demo datasets.
- [Popup-backed controls may not appear in the current discovery path] -> Mitigation: design the demo to exercise popup visibility explicitly and update discovery contracts where required.
- [Adding new action verbs may expand the Python API more quickly than planned] -> Mitigation: use demo scenarios to justify only the smallest necessary additions and keep unsupported cases documented rather than silently approximated.
- [Complex demo screens may create noisy discovery output] -> Mitigation: separate scenarios into logical views or tabs and keep each dataset concise.
- [Advanced demos may reveal agent limitations faster than implementation can close them] -> Mitigation: treat the demos as contract-discovery tools, not as a blanket support claim.

## Migration Plan

1. Design the advanced demo app structure and choose a small set of representative control scenarios.
2. Implement the JavaFX demo screens or tabs with stable IDs and visible sample data.
3. Run discovery against each scenario and document what the Java agent can currently observe.
4. Decide whether current actions are sufficient or whether new action contracts are required.
5. Add focused Python demo scripts for the advanced scenarios.
6. Extend manual smoke guidance and regression tests to cover the new scenarios.

Rollback strategy:
- If one advanced control type proves too unstable, keep the others and defer that control to a follow-up change.
- Do not block the entire demo expansion on fully solving virtualization or popup handling in one iteration.

## Open Questions

- Should advanced controls live on one multi-tab screen or as separate routes/scenes inside the demo app?
- Is `select(...)` required immediately, or can the first pass use `click` plus verification for some controls?
- How should `TreeView` item identity be expressed in selectors: text-only, hierarchical text path, or a richer selector shape?
- Should `TableView` automation target rows, cells, or both in the first pass?
