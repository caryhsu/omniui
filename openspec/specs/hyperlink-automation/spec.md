## Purpose

Define automation behavior for JavaFX Hyperlink controls.

## Requirements

# hyperlink-automation

## Overview
Automate click interactions on JavaFX `Hyperlink` nodes and verify their visited state.

## Requirements

### REQ-HL-01: Click hyperlink
The `click` action on a `Hyperlink` node MUST trigger its `onAction` handler (via `fire()`).

### REQ-HL-02: Visited state after click
After a `click` action, the `Hyperlink` node's `isVisited()` property MUST return `true`.
This value MUST be readable via `get_value`.

### REQ-HL-03: Initial visited state
Before any click, `get_value` on a `Hyperlink` MUST return `false` for `isVisited()`.

### REQ-HL-04: Selector targeting
A `Hyperlink` node MUST be selectable by its `fx:id` using the `#id` selector syntax.