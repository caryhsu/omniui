## 1. Java Agent — ColorPicker Actions

- [x] 1.1 Add `case "set_color"` to top-level `perform()` switch calling `doSetColor(selector, payload)`
- [x] 1.2 Implement `doSetColor`: resolve node by selector, call `Color.web(hexStr)`, then `setValue(color)`; return `set_color_failed` on exception
- [x] 1.3 Add `case "get_color"` to `performOnFxThread` switch: call `getValue()`, convert to `#RRGGBB` hex string
- [x] 1.4 Add `case "open_colorpicker"` to top-level switch using `performWithOverlayWait` with a color palette overlay predicate
- [x] 1.5 Add `case "dismiss_colorpicker"` to top-level switch; press Escape or click outside to close the popup

## 2. Java Agent — SplitPane Actions

- [x] 2.1 Add `case "get_divider_positions"` to `performOnFxThread` switch: call `getDividerPositions()`, return as JSON array
- [x] 2.2 Add `case "set_divider_position"` to `performOnFxThread` switch: parse index and position from payload, validate index bounds, call `setDividerPosition(index, position)`

## 3. Python Engine

- [x] 3.1 Add `set_color(color: str, **selector)` method to `engine.py` (uses `_perform`)
- [x] 3.2 Add `get_color(**selector)` method to `engine.py`
- [x] 3.3 Add `open_colorpicker(**selector)` method (uses `_perform`)
- [x] 3.4 Add `dismiss_colorpicker()` method (uses `_direct_action`)
- [x] 3.5 Add `get_divider_positions(**selector)` method to `engine.py`
- [x] 3.6 Add `set_divider_position(index: int, position: float, **selector)` method to `engine.py`

## 4. Demo App UI

- [x] 4.1 Add ColorPicker demo section to `LoginDemoApp.java`: `ColorPicker` with `fx:id="demoPicker2"`, label `fx:id="colorResult"`, listener that updates label to `Selected: #RRGGBB`, and a Reset button
- [x] 4.2 Add SplitPane demo section: `SplitPane` with `fx:id="demoSplitPane"` containing two labeled panels, label `fx:id="dividerResult"` updated by a divider position listener

## 5. Python Demo Scripts

- [x] 5.1 Create `demo/python/color_picker_demo.py`: call `set_color`, verify `colorResult` label, call `get_color`, print success
- [x] 5.2 Create `demo/python/split_pane_demo.py`: call `get_divider_positions`, call `set_divider_position(0, 0.3)`, verify approximate divider position, print success

## 6. Integration

- [x] 6.1 Add `color_picker_demo.py` and `split_pane_demo.py` to `demo/python/run_all.py`
- [x] 6.2 Rebuild jlink image: `mvn clean install -f java-agent/pom.xml` then `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`
- [x] 6.3 Run `python demo/python/color_picker_demo.py` and `split_pane_demo.py` — both pass ✓
