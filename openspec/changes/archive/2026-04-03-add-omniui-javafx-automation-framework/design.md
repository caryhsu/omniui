## Context

OmniUI Phase 1 targets a difficult but tractable automation problem: reliable UI automation for JavaFX desktop applications on a single local machine. The key constraint is that the framework must avoid coordinate-driven automation whenever the JavaFX runtime can expose node identity and event dispatch paths. OCR and vision are fallback mechanisms, not peer strategies.

The MVP spans multiple modules: a Python client API, an automation engine, a Java agent attached to the target JVM, selector resolution logic, fallback OCR and vision services, and a lightweight recorder. The design therefore needs explicit contracts between the Python control plane and the Java agent, plus consistent observability across all action paths.

Primary constraints:
- Phase 1 supports JavaFX only.
- The controller runs from Python.
- The target app is local to the machine.
- Direct JavaFX interaction is preferred over mouse-level automation.
- OCR fallback must remain acceptable under one second for common cases.
- The architecture should admit later adapters for Swing, web, and native apps without rewriting the Python API surface.

## Goals / Non-Goals

**Goals:**
- Provide a stable Python API for connect, discover, find, click, type, get_text, verify_text, screenshot, OCR, and vision-match flows.
- Attach a Java agent to a target JVM and expose a local RPC interface that returns the JavaFX node tree and executes node-bound actions.
- Define a selector resolution engine with deterministic priority: `fx:id`, structural text/type match, OCR text match, then vision template match.
- Standardize action results and debug traces so each action reports selector input, resolution tier, fallback usage, and confidence where applicable.
- Keep adapter boundaries clean so JavaFX is the first concrete adapter under a future multi-platform automation architecture.
- Support a recorder-lite mode that emits simple Python script lines for click actions only when the clicked target can be mapped back to a resolvable selector.

**Non-Goals:**
- Swing, browser, or native desktop automation in Phase 1.
- Multi-application orchestration, remote execution, or distributed workers.
- Deep-learning based vision models or semantic detection.
- Full recorder/playback fidelity, drag-and-drop recording, or keyboard macro recording.
- OS-level event synthesis as the primary interaction mechanism for JavaFX-discoverable nodes.

## Decisions

### 1. Use a local HTTP JSON protocol between Python and the Java agent

The Phase 1 agent will expose a loopback-only HTTP API with JSON payloads instead of gRPC or raw sockets.

Rationale:
- Faster to implement and debug for an MVP spanning Python and Java.
- Easy to inspect with logs and capture during demo scenarios.
- Sufficient for local-machine latency requirements.

Alternatives considered:
- gRPC: stronger contracts, but adds more setup friction and code generation overhead for Phase 1.
- Raw sockets: smaller runtime footprint, but weaker debuggability and higher protocol maintenance cost.

### 2. Model the automation engine as adapter plus fallback services behind a single selector resolver

The Python automation engine will expose one action API and internally coordinate:
- a JavaFX adapter for structural discovery and direct interaction,
- an OCR fallback service for text-based resolution from screenshots,
- a vision fallback service for template matching.

Rationale:
- Preserves a stable client API while allowing backend resolution strategies to evolve.
- Keeps future Swing/web/native adapters compatible with the same action orchestration shape.

Alternatives considered:
- Let the Python client choose the backend explicitly: rejected because it leaks engine complexity into scripts and weakens stability.
- Separate engines for structure and image automation: rejected because fallback chaining becomes inconsistent and hard to observe.

### 3. Normalize every resolved target into a `ResolvedElement` contract

Selector resolution will produce a normalized result containing:
- `tier`: `javafx`, `ocr`, or `vision`
- `selector_used`
- `matched_attributes`
- `confidence`
- `target_ref`
- `debug_context`

Rationale:
- Makes action execution independent of how the target was found.
- Enables consistent logs, retries, and acceptance assertions.
- Prevents OCR and vision from becoming special cases in the public API.

Alternatives considered:
- Returning backend-specific result types: rejected because action and debug logic would fragment quickly.

### 4. Prefer event-level JavaFX actions over input-device synthesis

When a JavaFX node is resolved, the agent will invoke node-level behavior through the JavaFX application thread instead of issuing OS mouse clicks.

Rationale:
- Avoids DPI and coordinate instability.
- Preserves determinism in headless-like or visually unstable states.
- Directly supports the core non-functional requirement of structural stability.

Alternatives considered:
- Always moving the mouse and clicking the node's bounds: rejected because it defeats the main value proposition of JavaFX introspection.

### 5. Separate discovery snapshots from action commands

Node enumeration will return a snapshot of the scene graph with stable fields such as:
- runtime node handle/reference
- `fx:id`
- node type
- text content
- hierarchy path
- visibility and enabled state when available

Action commands will resolve against a fresh or cached snapshot based on staleness policy.

Rationale:
- Keeps `get_nodes()` simple and testable.
- Avoids coupling discovery latency to every action.
- Supports future recorder and debug tooling.

Alternatives considered:
- Re-enumerating the entire tree on every action: simpler but more expensive and harder to reason about under rapid UI updates.

### 6. Recorder-lite will only emit script lines for supported click cases

Recorder-lite will capture local click events and attempt to map them to:
1. JavaFX selector by `fx:id`
2. JavaFX selector by `type + text`
3. Optional OCR text click expression if structural mapping fails

If no stable mapping exists, the recorder will not emit a misleading script line.

Rationale:
- Prevents generating brittle automation output.
- Matches the limited scope promised for Phase 1.

Alternatives considered:
- Recording raw coordinates: rejected because it conflicts with the framework's reliability goals.

## Risks / Trade-offs

- [Java agent attachment differs across launch modes and JVM configurations] -> Mitigation: define Phase 1 support matrix around attachable local JVMs and test against a reference JavaFX app.
- [JavaFX node identity may become stale after scene updates] -> Mitigation: use refreshable snapshots and re-resolve before action execution when cached nodes are invalid.
- [OCR false positives may click the wrong control] -> Mitigation: include confidence thresholds, bounding-box preview data, and deterministic fallback ordering.
- [Template matching is sensitive to theme and scaling changes] -> Mitigation: keep vision as the last fallback, log confidence, and scope Phase 1 demos to controlled assets.
- [HTTP JSON adds some protocol looseness compared with gRPC] -> Mitigation: define explicit request/response schemas in code and include contract tests between Python and Java.
- [Recorder-lite coverage may disappoint if interpreted as a full recorder] -> Mitigation: document clearly that only selector-stable click capture is supported in Phase 1.

## Migration Plan

This is a new capability set, so there is no production migration from an existing framework. The rollout plan is:

1. Build a reference JavaFX sample app that exposes a login flow and representative controls.
2. Implement Java agent attachment and node enumeration against the sample app.
3. Add Python client connectivity and direct JavaFX actions.
4. Add selector fallback services for OCR and vision behind the same action pipeline.
5. Add observability and demo assertions.
6. Add recorder-lite after the core action path is stable.

Rollback strategy:
- If fallback services destabilize the MVP, ship a JavaFX-only vertical slice first and keep OCR/vision behind feature flags until confidence thresholds are validated.

## Open Questions

- Which Java attach strategy will be the default in development: JVM startup agent, dynamic attach, or both?
- Which OCR engine offers the best Phase 1 trade-off between setup simplicity and multilingual accuracy?
- Which template-matching library best balances packaging simplicity between Python and Java dependencies?
- Should `type()` implicitly target the currently focused element, or require an explicit selector in the stable API?
- How much node metadata should be exposed publicly in `get_nodes()` without leaking JavaFX internals that would be hard to keep compatible later?
