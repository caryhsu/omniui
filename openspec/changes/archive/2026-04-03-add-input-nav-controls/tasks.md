## 1. Java Agent — Action Handlers

- [x] 1.1 Add `get_selected` handler: reflect `isSelected()` on RadioButton / ToggleButton / CheckBox
- [x] 1.2 Add `set_selected` handler: reflect `setSelected(boolean)` on the matched node
- [x] 1.3 Add `set_slider` handler: validate min/max then call `setValue(double)` on Slider
- [x] 1.4 Add `set_spinner` handler: call `getValueFactory().setValue(converted)` on Spinner
- [x] 1.5 Add `step_spinner` handler: call `increment(n)` or `decrement(abs(n))` on Spinner
- [x] 1.6 Add `get_progress` handler: reflect `getProgress()` on ProgressBar / ProgressIndicator
- [x] 1.7 Add `get_tabs` handler: iterate `tabPane.getTabs()`, return list of `{text, disabled}`
- [x] 1.8 Add `select_tab` handler: find tab by title in `getTabs()`, call `getSelectionModel().select(tab)`
- [x] 1.9 Route all new actions in `perform()` switch statement

## 2. Java Agent — performOnFxThread Integration

- [x] 2.1 Handle `get_value` action to support both Slider (`getValue`) and Spinner (`getValue`)
- [x] 2.2 Ensure `get_selected` and `set_selected` are routed from the `default` path in `performOnFxThread`

## 3. Python Client

- [x] 3.1 Add `get_selected(self, **selector)` method
- [x] 3.2 Add `set_selected(self, value: bool, **selector)` method
- [x] 3.3 Add `set_slider(self, value: float, **selector)` method
- [x] 3.4 Add `set_spinner(self, value: str, **selector)` method
- [x] 3.5 Add `step_spinner(self, steps: int, **selector)` method
- [x] 3.6 Add `get_progress(self, **selector)` method
- [x] 3.7 Add `get_tabs(self, **selector)` method
- [x] 3.8 Add `select_tab(self, tab: str, **selector)` method

## 4. Demo App

- [x] 4.1 Add imports: `RadioButton`, `ToggleButton`, `ToggleGroup`, `Slider`, `Spinner`,
      `ProgressBar`, `ProgressIndicator`, `TabPane`, `Tab`
- [x] 4.2 Add RadioButton / ToggleButton section (`id="radioToggleSection"`) with two
      RadioButtons in a ToggleGroup and one ToggleButton
- [x] 4.3 Add Slider / Spinner section (`id="sliderSpinnerSection"`) with
      `id="demoSlider"` and `id="demoSpinner"`
- [x] 4.4 Add ProgressBar section (`id="progressSection"`) with
      `id="demoProgressBar"` and `id="demoProgressIndicator"`, both with fixed initial values
- [x] 4.5 Add TabPane section (`id="tabSection"`) with `id="demoTabPane"` and at least three tabs

## 5. Python Demo Scripts

- [x] 5.1 Create `demo/python/radio_toggle_demo.py`: read initial state, select each RadioButton,
      toggle ToggleButton, verify results
- [x] 5.2 Create `demo/python/slider_spinner_demo.py`: set Slider to specific value, use
      step_spinner to increment and decrement, verify values
- [x] 5.3 Create `demo/python/progress_demo.py`: read ProgressBar and ProgressIndicator values,
      assert expected initial values
- [x] 5.4 Create `demo/python/tab_demo.py`: list tabs, select each tab by title, verify active tab
- [x] 5.5 Update `demo/python/run_all.py` to include all four new scripts
