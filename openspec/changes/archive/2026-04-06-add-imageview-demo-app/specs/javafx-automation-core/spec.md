## ADDED Requirements

### Requirement: Java agent reads ImageView node properties
The Java agent SHALL recognize `javafx.scene.image.ImageView` nodes and extract the following properties when responding to attribute-read commands:
- `imageUrl`: the string returned by `ImageView.getImage().getUrl()`; returns `""` if `getImage()` is null or `getUrl()` is null
- `isLoaded`: `true` if `getImage() != null && !getImage().isError()`; `false` otherwise

#### Scenario: Agent returns imageUrl for a loaded ImageView
- **WHEN** the agent processes a `get_image_url` command targeting a node of type `ImageView` with a loaded image
- **THEN** the agent returns the image URL string as the action result value

#### Scenario: Agent returns isLoaded=false for broken image
- **WHEN** the agent processes an `is_image_loaded` command targeting an `ImageView` whose `Image.isError()` is true
- **THEN** the agent returns `false` as the action result value
