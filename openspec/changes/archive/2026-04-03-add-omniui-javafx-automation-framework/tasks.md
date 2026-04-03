## 1. Project and protocol foundation

- [x] 1.1 Create the OmniUI module structure for core engine, Python client, Java agent, selector engine, OCR, vision, and recorder-lite components
- [x] 1.2 Define the local agent protocol for node discovery, action execution, screenshot retrieval, and health/session management
- [x] 1.3 Add contract tests or fixtures that validate request and response payloads between the Python client and Java agent

## 2. JavaFX automation core

- [x] 2.1 Implement Java agent attachment and JavaFX runtime hook for a supported local target application
- [x] 2.2 Implement scene graph enumeration that returns `fx:id`, node type, text content, hierarchy path, and relevant state metadata
- [x] 2.3 Implement direct JavaFX action execution for `click`, `type`, and `get_text` on resolved nodes
- [x] 2.4 Implement Python client high-level APIs for `connect`, `get_nodes`, `click`, `type`, `get_text`, and `verify_text`

## 3. Selector resolution and fallback chain

- [x] 3.1 Implement selector normalization and priority-based resolution for `fx:id` followed by `type + text`
- [x] 3.2 Implement OCR fallback using screenshot capture plus text-region lookup
- [x] 3.3 Implement vision template matching as the final fallback after JavaFX and OCR resolution fail
- [x] 3.4 Implement a normalized resolved-element result model that carries selector source, tier, target reference, and confidence metadata

## 4. Observability and reliability

- [x] 4.1 Add action tracing that logs selector input, resolution tier, attempted fallbacks, and confidence for OCR or vision paths
- [x] 4.2 Add retry or re-resolution handling for stale JavaFX node references after scene updates
- [x] 4.3 Measure and tune JavaFX node query and OCR fallback performance against the Phase 1 targets

## 5. Recorder-lite and demo validation

- [x] 5.1 Implement recorder-lite click capture and selector mapping for JavaFX-backed interactions
- [x] 5.2 Generate simple script output only for clicks that can be expressed as stable selectors
- [x] 5.3 Build or wire a reference JavaFX login application for end-to-end validation
- [x] 5.4 Add an automated demo script that executes the login flow and proves JavaFX direct interaction plus OCR fallback behavior
