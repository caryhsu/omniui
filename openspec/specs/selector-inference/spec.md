# selector-inference

## Purpose

Derive the most stable selector from recorded event metadata, following a priority order that maximises test reliability.

## Requirements

### Requirement: Selector inference derives the most stable selector from event metadata

The system SHALL provide a `infer_selector(event: RecordedEvent) -> dict` function that returns the most stable selector dict for use in generated scripts, following priority order: `fxId` → `text` → `nodeType+index`.

#### Scenario: Node has fxId
- **WHEN** `event.fx_id` is a non-empty string
- **THEN** `infer_selector` returns `{"id": event.fx_id}`

#### Scenario: Node has no fxId but has short text
- **WHEN** `event.fx_id` is empty and `event.text` is non-empty and ≤ 40 characters
- **THEN** `infer_selector` returns `{"text": event.text}`

#### Scenario: Node has no fxId and no text — fallback to type+index
- **WHEN** `event.fx_id` is empty and `event.text` is empty or > 40 characters
- **THEN** `infer_selector` returns `{"type": event.node_type, "index": event.node_index}`
- **THEN** the result is marked fragile via a `"_fragile": True` flag
