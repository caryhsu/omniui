## 1. Java Agent — ColorPicker Actions

- [x] 1.1 Add to `perform()` top-level switch in `ReflectiveJavaFxTarget.java`:
  - `case "open_colorpicker" -> performWithOverlayWait(() -> doOpenColorPicker(selector), this::isColorPickerWindow);`
  - `case "set_color"        -> doSetColor(selector, payload);`
  - `case "dismiss_colorpicker" -> doDismissColorPicker();`
  (Place after `"open_datepicker"` / `"dismiss_menu"` group)

- [x] 1.2 Add `case "get_color"` to `performOnFxThread()` switch:
  - Call `getValue()` on the node, format r/g/b components as `#RRGGBB`
  - Return `ActionResult.success` with hex string value

- [x] 1.3 Implement `doSetColor(selector, payload)`:
  - Resolve node via selector; fail with `"set_color_failed"` if not found
  - Parse `payload.get("color").getAsString()` into `Color.web(hex)`
  - Call `setValue(color)` on node via `ReflectiveJavaFxSupport.invoke`
  - Return success or failure with appropriate reason

- [x] 1.4 Implement `doOpenColorPicker(selector)`:
  - Resolve node, call `show()` via reflection (similar to `doOpenDatePicker`)
  - Return `ActionResult.success`

- [x] 1.5 Implement `doDismissColorPicker()`:
  - Press Escape on the focused window (same pattern as `doDismissMenu`)

- [x] 1.6 Implement `isColorPickerWindow(Object window)`:
  - Get scene root class name, check if it contains `"ColorPalette"` or `"ColorPicker"`
  - Model after `isDatePickerWindow`

## 2. Python Engine

- [x] 2.1 Add to `omniui/core/engine.py` (after `get_image_url` / `is_image_loaded`):
  - `set_color(self, color: str, **selector) -> ActionResult`
  - `get_color(self, **selector) -> ActionResult`
  - `open_colorpicker(self, **selector) -> ActionResult`
  - `dismiss_colorpicker(self) -> ActionResult`

- [x] 2.2 Expose the 4 methods on `OmniUIClient` in `omniui/locator.py` (delegate to `_engine`)

## 3. Java Demo App — color-app

- [x] 3.1 Create `demo/java/color-app/pom.xml` (port 48106, artifact `omniui-color-demo`, parent `omniui-demos`)

- [x] 3.2 Create `demo/java/color-app/src/main/java/module-info.java`

- [x] 3.3 Implement `demo/java/color-app/src/main/java/dev/omniui/demo/color/ColorDemoApp.java`:
  - `demoPicker` (`ColorPicker`, default white)
  - `colorResult` (`Label`, id `"colorResult"`, updates to `"Selected: #RRGGBB"` on picker change)
  - `resetColorButton` (`Button`, id `"resetColorButton"`, resets picker to `#ffffff` and clears `colorResult`)

- [x] 3.4 Create `demo/java/color-app/run-dev-with-agent.bat` (copy from image-app, change port to 48106 and artifact name)

- [x] 3.5 Confirm `mvn compile` succeeds

## 4. Python Demo Script

- [x] 4.1 Create `demo/python/color/__init__.py`, `_bootstrap.py`, `_runtime.py` (port 48106)

- [x] 4.2 Implement `demo/python/color/color_demo.py`:
  - `set_color("#ff5733", id="demoPicker")` → assert `colorResult` contains `"ff5733"`
  - `get_color(id="demoPicker")` → assert value is `"#ff5733"`
  - Click `resetColorButton` → assert `colorResult` text is `""`
  - Print `"color_demo succeeded (ok)"`

## 5. Integration

- [x] 5.1 Add color-app import and section to `demo/python/run_all.py` (port 48106)

- [x] 5.2 Run `python -m pytest tests/ -q` — confirm baseline tests still pass

  - `case "open_colorpicker" -> performWithOverlayWait(() -> doOpenColorPicker(selector), this::isColorPickerWindow);`
  - `case "set_color"        -> doSetColor(selector, payload);`
  - `case "dismiss_colorpicker" -> doDismissColorPicker();`
  (Place after `"open_datepicker"` / `"dismiss_menu"` group)

- [ ] 1.2 Add `case "get_color"` to `performOnFxThread()` switch:
  - Call `getValue()` on the node, format r/g/b components as `#RRGGBB`
  - Return `ActionResult.success` with hex string value

- [ ] 1.3 Implement `doSetColor(selector, payload)`:
  - Resolve node via selector; fail with `"set_color_failed"` if not found
  - Parse `payload.get("color").getAsString()` into `Color.web(hex)`
  - Call `setValue(color)` on node via `ReflectiveJavaFxSupport.invoke`
  - Return success or failure with appropriate reason

- [ ] 1.4 Implement `doOpenColorPicker(selector)`:
  - Resolve node, call `show()` via reflection (similar to `doOpenDatePicker`)
  - Return `ActionResult.success`

- [ ] 1.5 Implement `doDismissColorPicker()`:
  - Press Escape on the focused window (same pattern as `doDismissMenu`)

- [ ] 1.6 Implement `isColorPickerWindow(Object window)`:
  - Get scene root class name, check if it contains `"ColorPalette"` or `"ColorPicker"`
  - Model after `isDatePickerWindow`

## 2. Python Engine

- [ ] 2.1 Add to `omniui/core/engine.py` (after `get_image_url` / `is_image_loaded`):
  - `set_color(self, color: str, **selector) -> ActionResult`
  - `get_color(self, **selector) -> ActionResult`
  - `open_colorpicker(self, **selector) -> ActionResult`
  - `dismiss_colorpicker(self) -> ActionResult`

- [ ] 2.2 Expose the 4 methods on `OmniUIClient` in `omniui/locator.py` (delegate to `_engine`)

## 3. Java Demo App — color-app

- [ ] 3.1 Create `demo/java/color-app/pom.xml` (port 48106, artifact `omniui-color-demo`, parent `omniui-demos`)

- [ ] 3.2 Create `demo/java/color-app/src/main/java/module-info.java`

- [ ] 3.3 Implement `demo/java/color-app/src/main/java/dev/omniui/demo/color/ColorDemoApp.java`:
  - `demoPicker` (`ColorPicker`, default white)
  - `colorResult` (`Label`, id `"colorResult"`, updates to `"Selected: #RRGGBB"` on picker change)
  - `resetColorButton` (`Button`, id `"resetColorButton"`, resets picker to `#ffffff` and clears `colorResult`)

- [ ] 3.4 Create `demo/java/color-app/run-dev-with-agent.bat` (copy from image-app, change port to 48106 and artifact name)

- [ ] 3.5 Confirm `mvn compile` succeeds

## 4. Python Demo Script

- [ ] 4.1 Create `demo/python/color/__init__.py`, `_bootstrap.py`, `_runtime.py` (port 48106)

- [ ] 4.2 Implement `demo/python/color/color_demo.py`:
  - `set_color("#ff5733", id="demoPicker")` → assert `colorResult` contains `"ff5733"`
  - `get_color(id="demoPicker")` → assert value is `"#ff5733"`
  - Click `resetColorButton` → assert `colorResult` text is `""`
  - Print `"color_demo succeeded (ok)"`

## 5. Integration

- [ ] 5.1 Add color-app import and section to `demo/python/run_all.py` (port 48106)

- [ ] 5.2 Run `python -m pytest tests/ -q` — confirm baseline tests still pass
