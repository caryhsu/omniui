# Spec: scene-graph-diff

## Purpose

Defines the `client.diff(before, after)` API that compares two `UISnapshot` objects and returns a `UIDiff` describing added, removed, and changed nodes.

---

## Requirements

### Requirement: diff returns added, removed, and changed nodes between two snapshots

The system SHALL provide `client.diff(before, after)` that accepts two `UISnapshot` objects and returns a `UIDiff` with three lists: `added`, `removed`, `changed`.

Node identity is determined by `fxId` when non-empty, otherwise by `handle`.

#### Scenario: Node added between snapshots
- **WHEN** `after` contains a node whose identity key is absent in `before`
- **THEN** that node appears in `UIDiff.added`

#### Scenario: Node removed between snapshots
- **WHEN** `before` contains a node whose identity key is absent in `after`
- **THEN** that node appears in `UIDiff.removed`

#### Scenario: Node attribute changed between snapshots
- **WHEN** a node is present in both snapshots but has a different `text`, `enabled`, `visible`, `value`, or `nodeType`
- **THEN** an entry `{"before": <before_node>, "after": <after_node>}` appears in `UIDiff.changed`

#### Scenario: No changes between identical snapshots
- **WHEN** `before.nodes` and `after.nodes` are identical
- **THEN** `UIDiff.added`, `UIDiff.removed`, and `UIDiff.changed` are all empty lists

#### Scenario: diff is a pure function
- **WHEN** `client.diff(before, after)` is called
- **THEN** neither `before` nor `after` is mutated
