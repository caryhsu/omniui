# Spec: scene-graph-snapshot

## Purpose

Defines the `client.snapshot()` API that captures the full scene graph node state as a structured list with a timestamp.

---

## Requirements

### Requirement: snapshot returns a UISnapshot with all current nodes

The system SHALL provide `client.snapshot()` on `OmniUIClient` that returns a `UISnapshot` containing a `nodes` list (all current scene graph nodes) and a `timestamp` (float, seconds since epoch).

#### Scenario: Basic snapshot
- **WHEN** `client.snapshot()` is called
- **THEN** the result is a `UISnapshot` instance
- **THEN** `result.nodes` is a non-empty list of node dicts
- **THEN** `result.timestamp` is a positive float

#### Scenario: Snapshot captures node attributes
- **WHEN** `client.snapshot()` is called
- **THEN** each node dict contains at least `fxId`, `text`, `nodeType`, `enabled`, `visible` keys

### Requirement: UISnapshot supports save and load

The system SHALL provide `UISnapshot.save(path)` to serialize a snapshot to a JSON file, and `UISnapshot.load(path)` as a class method to deserialize it back.

#### Scenario: Round-trip save and load
- **WHEN** `snap.save(path)` is called and then `UISnapshot.load(path)` is called
- **THEN** the loaded snapshot has identical `nodes` and `timestamp` as the original
