## Why

Teams that automate Java desktop applications usually fall back to screen coordinates too early, which makes tests brittle across DPI, resolution, and layout changes. OmniUI is needed to provide a JavaFX-first automation path that can resolve elements from the scene graph and only fall back to OCR or vision when the runtime structure is unavailable.

This change is needed now to define a stable MVP boundary for a cross-platform UI automation framework before implementation begins. The proposal establishes a JavaFX-focused Phase 1 that is usable for login-flow style automation while preserving a clean extension path for Swing, web, and native targets later.

## What Changes

- Introduce an OmniUI Phase 1 MVP focused on local-machine automation of JavaFX applications.
- Define a Python client API for connecting to a target app, discovering nodes, resolving selectors, executing actions, and verifying outcomes.
- Introduce a Java agent that attaches to the target JVM, inspects the JavaFX scene graph, and executes event-level interactions without relying on screen coordinates when possible.
- Define a selector resolution pipeline that prioritizes JavaFX structural selectors, then OCR text matching, then vision template matching.
- Define action execution, debug observability, and acceptance criteria for a stable JavaFX login-flow demo.
- Include a lightweight recorder as an optional Phase 1 capability that can capture clicks and emit simple automation script lines when node mapping is available.

## Capabilities

### New Capabilities
- `javafx-automation-core`: Discover JavaFX nodes, resolve selectors, and execute direct JavaFX interactions through an attached agent.
- `multimodal-fallback-resolution`: Resolve actions through OCR and vision fallback when JavaFX structure is unavailable.
- `python-automation-client`: Provide a Python API for connection, discovery, action execution, and verification flows.
- `recorder-lite`: Capture basic click interactions and emit simple Python automation script output for supported cases.

### Modified Capabilities
- None.

## Impact

- Affected code will include new `omniui/` modules for core orchestration, selector logic, OCR, vision, Python client, Java agent, and optional recorder-lite support.
- Introduces an inter-process protocol between the Python client and Java agent; Phase 1 may use HTTP, socket, or gRPC, with the final protocol chosen in design.
- Adds dependencies for OCR and template matching in the fallback pipeline.
- Establishes behavioral contracts and acceptance criteria for local JavaFX automation, including observability data such as selector source, fallback tier, and confidence score.
