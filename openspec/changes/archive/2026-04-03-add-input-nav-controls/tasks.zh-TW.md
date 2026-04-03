## 1. Java Agent — Action Handlers

- [ ] 1.1 新增 `get_selected` handler：反射呼叫 RadioButton / ToggleButton / CheckBox 的 `isSelected()`
- [ ] 1.2 新增 `set_selected` handler：反射呼叫節點的 `setSelected(boolean)`
- [ ] 1.3 新增 `set_slider` handler：驗證 min/max 後呼叫 Slider 的 `setValue(double)`
- [ ] 1.4 新增 `set_spinner` handler：呼叫 `getValueFactory().setValue(converted)`
- [ ] 1.5 新增 `step_spinner` handler：呼叫 `increment(n)` 或 `decrement(abs(n))`
- [ ] 1.6 新增 `get_progress` handler：反射呼叫 ProgressBar / ProgressIndicator 的 `getProgress()`
- [ ] 1.7 新增 `get_tabs` handler：遍歷 `tabPane.getTabs()`，回傳 `{text, disabled}` 清單
- [ ] 1.8 新增 `select_tab` handler：以標題在 `getTabs()` 中找分頁，呼叫 `getSelectionModel().select(tab)`
- [ ] 1.9 在 `perform()` switch 中路由所有新 action

## 2. Java Agent — performOnFxThread 整合

- [ ] 2.1 處理 `get_value` action，支援 Slider（`getValue`）與 Spinner（`getValue`）
- [ ] 2.2 確保 `get_selected` 與 `set_selected` 從 `performOnFxThread` 的 `default` 路徑路由

## 3. Python Client

- [ ] 3.1 新增 `get_selected(self, **selector)` 方法
- [ ] 3.2 新增 `set_selected(self, value: bool, **selector)` 方法
- [ ] 3.3 新增 `set_slider(self, value: float, **selector)` 方法
- [ ] 3.4 新增 `set_spinner(self, value: str, **selector)` 方法
- [ ] 3.5 新增 `step_spinner(self, steps: int, **selector)` 方法
- [ ] 3.6 新增 `get_progress(self, **selector)` 方法
- [ ] 3.7 新增 `get_tabs(self, **selector)` 方法
- [ ] 3.8 新增 `select_tab(self, tab: str, **selector)` 方法

## 4. Demo App

- [ ] 4.1 新增 imports：`RadioButton`、`ToggleButton`、`ToggleGroup`、`Slider`、`Spinner`、
      `ProgressBar`、`ProgressIndicator`、`TabPane`、`Tab`
- [ ] 4.2 新增 RadioButton / ToggleButton 區段（`id="radioToggleSection"`），
      包含同一 ToggleGroup 的兩個 RadioButton 與一個 ToggleButton
- [ ] 4.3 新增 Slider / Spinner 區段（`id="sliderSpinnerSection"`），
      包含 `id="demoSlider"` 與 `id="demoSpinner"`
- [ ] 4.4 新增 ProgressBar 區段（`id="progressSection"`），包含
      `id="demoProgressBar"` 與 `id="demoProgressIndicator"`，並設定初始值
- [ ] 4.5 新增 TabPane 區段（`id="tabSection"`），包含 `id="demoTabPane"` 與至少三個分頁

## 5. Python Demo 腳本

- [ ] 5.1 建立 `demo/python/radio_toggle_demo.py`：讀取初始狀態、選取各 RadioButton、切換 ToggleButton
- [ ] 5.2 建立 `demo/python/slider_spinner_demo.py`：設定 Slider 值、使用 step_spinner 步進並驗證
- [ ] 5.3 建立 `demo/python/progress_demo.py`：讀取 ProgressBar 與 ProgressIndicator 值並驗證初始值
- [ ] 5.4 建立 `demo/python/tab_demo.py`：列舉分頁、以標題選取各分頁並驗證
- [ ] 5.5 更新 `demo/python/run_all.py` 加入四支新腳本
