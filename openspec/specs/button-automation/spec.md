## Purpose

Define automation behavior for JavaFX Button controls.

## Requirements

### Requirement: Button click
A Button automation implementation SHALL support triggering a Button by calling its `fire()` method.

#### Scenario: Click a Button
- **WHEN** `click(id="<buttonId>")` is called
- **THEN** `fire()` is invoked on the targeted Button and the button's action handler executes

#### Scenario: Click fires only the targeted Button
- **WHEN** multiple buttons exist and `click` is called on one button
- **THEN** only that button's action handler fires
