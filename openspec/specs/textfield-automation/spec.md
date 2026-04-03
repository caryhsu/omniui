## Purpose

Define automation behavior for JavaFX TextField controls.

## Requirements

### Requirement: TextField text write
A TextField automation implementation SHALL support setting the text of a TextField by calling `setText(input)`.

#### Scenario: Set text
- **WHEN** `type(id="<id>", input="Hello")` is called
- **THEN** `getText()` returns `"Hello"`

#### Scenario: Clear text
- **WHEN** `type(id="<id>", input="")` is called
- **THEN** `getText()` returns `""`

### Requirement: TextField text read
A TextField automation implementation SHALL support reading the current text of a TextField by calling `getText()`.

#### Scenario: Read text
- **WHEN** `get_text(id="<id>")` is called
- **THEN** returns the current text value of the TextField
