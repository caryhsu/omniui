## 原因

FusionUI 已涵蓋基本輸入與清單類控制元件，但尚未支援日常 UI 中普遍使用的
**數值輸入、單選、切換、進度顯示、以及分頁導覽**等元件。
補全這批控制元件後，自動化腳本即可覆蓋絕大多數桌面應用場景。

## 變更內容

- 新增 `RadioButton` / `ToggleGroup` 自動化：偵測選中狀態、切換選項
- 新增 `ToggleButton` 自動化：偵測 selected 狀態、點擊切換
- 新增 `Slider` 自動化：讀取 / 設定數值
- 新增 `Spinner` 自動化：讀取 / 設定值、increment / decrement
- 新增 `ProgressBar` / `ProgressIndicator` 自動化：讀取進度值
- 新增 `TabPane` / `Tab` 自動化：列出分頁、切換分頁
- Java agent 新增對應 action handler
- Python client 新增對應公開方法
- Demo app 新增展示區段
- 新增 5 支 Python demo 腳本

## 功能範圍

### 新增功能

- `radio-toggle-automation`：RadioButton / ToggleButton / ToggleGroup 的選取偵測與切換
- `slider-spinner-automation`：Slider 數值讀寫、Spinner 讀寫與步進操作
- `progress-automation`：ProgressBar / ProgressIndicator 進度值讀取
- `tabpane-automation`：TabPane 分頁列舉與切換
- `input-nav-demo-scenarios`：Demo app 新增區段與對應 Python demo 腳本

### 修改功能

- `javafx-automation-core`：新增 set_slider、set_spinner、step_spinner、
  get_progress、select_tab 等 action 路由
- `advanced-javafx-demo-scenarios`：擴充 demo app 新增 5 個展示區段

## 影響範圍

- `java-agent/…/ReflectiveJavaFxTarget.java`：新增 action handler 與 helper
- `omniui/core/engine.py`：新增公開方法
- `demo/javafx-login-app/…/LoginDemoApp.java`：新增控制元件區段
- `demo/python/`：新增 5 支 demo 腳本
