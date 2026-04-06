## Purpose

Define the Python automation API for reading properties of JavaFX `ImageView` nodes through the OmniUI Java agent.

## Requirements

### Requirement: get_image_url reads ImageView URL
The system SHALL allow Python automation scripts to read the URL of the image currently displayed in a JavaFX `ImageView` node. The Java agent SHALL extract `ImageView.getImage().getUrl()` and return it as a string value. If `getImage()` is null or `getUrl()` is null, the agent SHALL return an empty string `""`.

#### Scenario: Read URL of a loaded image
- **WHEN** a `get_image_url` command is sent with a selector matching an `ImageView` whose image URL is `https://example.com/photo.jpg`
- **THEN** the agent returns `ActionResult(ok=True, value="https://example.com/photo.jpg")`

#### Scenario: Read URL when no image is set
- **WHEN** a `get_image_url` command targets an `ImageView` with no image set (`getImage()` returns null)
- **THEN** the agent returns `ActionResult(ok=True, value="")`

#### Scenario: Target node is not an ImageView
- **WHEN** a `get_image_url` command targets a node that is not an `ImageView`
- **THEN** the agent returns `ActionResult(ok=False)` with an error message

### Requirement: is_image_loaded checks image load status
The system SHALL allow Python automation scripts to check whether an `ImageView`'s image has loaded successfully (i.e., no error occurred). The agent SHALL evaluate `!ImageView.getImage().isError()`. If `getImage()` is null, the result SHALL be `false`.

#### Scenario: Image loaded successfully
- **WHEN** `is_image_loaded` is called on an `ImageView` whose image loaded without error
- **THEN** the Python client returns `True`

#### Scenario: Image failed to load (broken URL)
- **WHEN** `is_image_loaded` is called on an `ImageView` whose image URL is invalid and `isError()` returns true
- **THEN** the Python client returns `False`

#### Scenario: No image set
- **WHEN** `is_image_loaded` is called on an `ImageView` with no image set
- **THEN** the Python client returns `False`
