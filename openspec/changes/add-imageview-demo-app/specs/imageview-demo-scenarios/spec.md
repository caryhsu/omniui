## ADDED Requirements

### Requirement: image-app provides ImageView automation scenarios
The image-app (port 48105) SHALL provide a standalone JavaFX demo application with the following interactive elements for Python automation testing:
- `imageView1`: an ImageView displaying a valid remote image (loaded state)
- `imageView2`: an ImageView displaying an invalid URL (error/broken state)  
- `switchBtn`: a Button that swaps the images between imageView1 and imageView2
- `urlLabel`: a Label showing the current URL of imageView1
- `statusLabel`: a Label showing load status ("loaded" / "broken")
- `dragImageView`: a draggable ImageView that can be dragged to `dropZone`
- `dropZone`: a labeled pane that accepts dropped ImageView; displays "dropped!" after successful drop
- `dropResult`: a Label showing the drag & drop result text

#### Scenario: Initial state verification
- **WHEN** the image-app starts
- **THEN** `imageView1` displays a valid image (`is_image_loaded` returns True)
- **AND** `imageView2` displays a broken image (`is_image_loaded` returns False)
- **AND** `dropResult` text is empty or "none"

#### Scenario: Switch images updates URL and status
- **WHEN** Python clicks `switchBtn`
- **THEN** `get_image_url(id="imageView1")` returns the previously-broken URL
- **AND** `get_image_url(id="imageView2")` returns the previously-valid URL

#### Scenario: Drag ImageView to drop zone
- **WHEN** Python executes `client.drag(id="dragImageView").to(id="dropZone")`
- **THEN** `dropResult` label text becomes "dropped!"

### Requirement: imageview drag source is interactable via existing drag API
The image-app SHALL register mouse event handlers on `dragImageView` such that OmniUI's existing mouse-based drag API (`MOUSE_PRESSED` → `MOUSE_DRAGGED` → `MOUSE_RELEASED`) correctly triggers the drag & drop operation.

#### Scenario: drag().to() works on ImageView node
- **WHEN** `client.drag(id="dragImageView").to(id="dropZone")` is executed
- **THEN** the drag completes without error
- **AND** the app registers the drop event
