## Purpose

Define automation behavior for JavaFX TextArea controls.

## Requirements

# text-area-automation

## Overview
Automate read and write operations on JavaFX `TextArea` nodes using the existing
`get_text` and `type` actions.

## Requirements

### REQ-TA-01: Write multi-line text
The `type` action on a `TextArea` node MUST set its full content to the provided
string, including newline characters (`\n`).

### REQ-TA-02: Read multi-line text
The `get_text` action on a `TextArea` node MUST return the current full text content,
preserving newlines.

### REQ-TA-03: Clear text
Calling `type` with an empty string MUST clear the `TextArea` content.

### REQ-TA-04: Selector targeting
A `TextArea` node MUST be selectable by its `fx:id` using the `#id` selector syntax.