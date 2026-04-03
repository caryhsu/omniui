## Context

FusionUI 的 Java agent 透過反射操作 JavaFX 場景圖，已支援基本控制元件與
popup/overlay 類元件（ContextMenu、DatePicker、Dialog 等）。
本次擴充同樣沿用 `ReflectiveJavaFxTarget` 的反射架構，新增以下元件的讀寫支援：
RadioButton / ToggleButton、Slider、Spinner、ProgressBar / ProgressIndicator、
TabPane / Tab。

## Goals / Non-Goals

**Goals:**
- 透過反射新增對上述元件的 action handler（含讀值、設值、切換）
- Python client 對應新增公開方法
- Demo app 新增展示區段，搭配 Python demo 腳本驗證

**Non-Goals:**
- ToggleGroup 的跨元件連動測試（只驗單一 RadioButton 選取）
- Slider 動畫效果驗證
- ProgressBar 動態更新的時序測試
- WebView、HTMLEditor 等複雜 embedded 控制元件

## Decisions

### 1. Slider：以 `setValue(double)` 設值，不模擬拖拉手勢

**選擇**：直接呼叫 `slider.setValue(target)` 反射設值。
**放棄**：模擬 MouseDrag — 需計算像素座標，跨平台 DPI 不一致。
**理由**：自動化目的是驗證狀態，不需模擬真實使用者手勢；`setValue` 會觸發
`valueProperty` 的 ChangeListener，等同真實操作效果。

### 2. Spinner：以 `getValueFactory().setValue()` 設值，另提供 increment/decrement

**選擇**：`spinner.getValueFactory().setValue(val)` 做精確設值；
`spinner.increment(steps)` / `spinner.decrement(steps)` 做步進。
**放棄**：模擬鍵盤 UP/DOWN — 步數不可控且需要 focus。
**理由**：反射呼叫更穩定，且 `increment/decrement` 是 JavaFX 官方 API。

### 3. RadioButton / ToggleButton：以 `setSelected(true)` 切換，不呼叫 `fire()`

**選擇**：`setSelected(true)` 直接設定選取狀態，`ToggleGroup` 會自動處理互斥。
**放棄**：`fire()` — 對 RadioButton 只有在未選取時才切換，邏輯複雜；
且若呼叫 `fire()` 對已選取的 RadioButton 無效。
**理由**：`setSelected` 語意明確，ToggleGroup 的互斥機制仍有效。

### 4. TabPane：以 tab title 字串選取分頁

**選擇**：尋找 `Tab.getText()` 符合目標字串的 Tab，呼叫
`tabPane.getSelectionModel().select(tab)`。
**放棄**：以索引選取 — 腳本可讀性差，且 tab 順序可能變動。
**理由**：title 字串是最穩定的選取方式，符合現有 `id` / `text` 選取慣例。

### 5. ProgressBar / ProgressIndicator：唯讀，只提供 `get_progress()`

**選擇**：反射呼叫 `getProgress()` 回傳 double（0.0–1.0）。
**放棄**：提供 `set_progress()` — 自動化場景中進度值由應用程式邏輯控制，
agent 不應修改。
**理由**：自動化需求是「讀取並驗證」，不是控制進度。

## Risks / Trade-offs

- **`Spinner.getValueFactory()` 回傳型別不確定**：不同 Spinner（Integer、Double、String）
  有不同 `SpinnerValueFactory` 子類，`setValue` 的參數型別需動態比對。
  ↗ 緩解：先呼叫 `getConverter().fromString(input)` 轉換輸入字串，再呼叫 `setValue`。

- **TabPane skin 延遲初始化**：Tab header 區域在 TabPane 首次顯示前可能尚未建立。
  ↗ 緩解：直接使用 `tabPane.getTabs()` API 列舉 Tab 物件，不走 skin tree。

## Open Questions

- 無
