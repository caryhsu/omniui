## Purpose

Define automation behavior for JavaFX PasswordField controls.

## Requirements

# password-field-automation

## Overview
Automate read and write operations on JavaFX `PasswordField` nodes using the existing
`get_text` and `type` actions. The masked display is visual-only; automation reads and
writes the underlying plain text value.

## Requirements

### REQ-PF-01: Write password text
The `type` action on a `PasswordField` node MUST set its text content to the provided
plain string.

### REQ-PF-02: Read password as plain text
The `get_text` action on a `PasswordField` node MUST return the unmasked plain text
value (not the bullet/mask characters shown in the UI).

### REQ-PF-03: Clear password
Calling `type` with an empty string MUST clear the `PasswordField` content.

### REQ-PF-04: Selector targeting
A `PasswordField` node MUST be selectable by its `fx:id` using the `#id` selector syntax.