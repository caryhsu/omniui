## Purpose

Define automation behavior for JavaFX Label controls.

## Requirements

### Requirement: Label text read
A Label automation implementation SHALL support reading the current text of a Label by calling `getText()`.

#### Scenario: Read label text
- **WHEN** `get_text(id="<id>")` is called
- **THEN** returns the Label's current text

#### Scenario: Empty label returns empty string
- **WHEN** `get_text` is called on a Label with no text set
- **THEN** returns an empty string or null
