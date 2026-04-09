# OmniUI

英文版檔名：`README.md`

OmniUI 是一個 multi-modal UI automation framework，Phase 1 採用 JavaFX-first 的自動化策略。

目前的解析與操作優先順序是：
- JavaFX scene graph 結構解析與 event-level direct interaction
- OCR 文字辨識 fallback
- Vision template match fallback

這個 repo 目前已經包含：
- Python client: [omniui](omniui)
- 本機 HTTP Java agent: [java-agent](java-agent)
- JavaFX 參考 demo 程式: [demo/java](demo/java) (`core-app`、`input-app`、`advanced-app`、`login-app`)
- demo 與 benchmark script: [scripts](scripts)

## 目前狀態

已完成能力：

**核心基礎設施**
- 透過本機 Java agent 連到 live JavaFX runtime 做 node discovery
- selector fallback chain: `javafx -> ocr -> vision`
- action tracing、refresh 後 retry、action history
- recorder-lite 穩定 click script 產生

**Actions — 基本互動**
- `click`, `right_click`, `double_click`, `type`, `get_text`, `verify_text`, `get_tooltip`, `get_style`, `get_style_class`, `scroll_to`, `scroll_by`
- `press_key(key, **selector)` — 鍵盤快捷鍵，格式：`"Ctrl+C"`、`"Shift+Tab"`、`"Escape"`、`"Control+Shift+Z"`
- `select`（ComboBox / ChoiceBox / ListView）、`get_selected`、`set_selected`（CheckBox / RadioButton / ToggleButton）
- `select_multiple(values, **selector)`、`get_selected_items(**selector)` — ListView / TableView 多選
- `is_visible`、`is_enabled` — 查詢節點可見性 / 啟用狀態
- `wait_for_text`、`wait_for_visible`、`wait_for_enabled`、`wait_for_node`、`wait_for_value` — poll-based 等待條件
- `close_app()` — 觸發 JavaFX 應用程式優雅關閉

**Actions — 選單**
- `open_menu`、`navigate_menu`、`dismiss_menu`、`click_menu_item`（MenuBar + ContextMenu）

**Actions — DatePicker**
- `open_datepicker`、`navigate_month`、`pick_date`、`set_date`

**Actions — 對話框**
- `get_dialog`、`dismiss_dialog`（Alert: information / warning / error / confirmation）

**Actions — 表單控制項**
- `set_slider`、`set_spinner`、`step_spinner`、`get_progress`、`get_value`

**Actions — Tab**
- `get_tabs`、`select_tab`

**Actions — Accordion**
- `expand_pane`、`collapse_pane`、`get_expanded`

**Actions — Hyperlink**
- `get_visited`

**Actions — TreeTableView**
- `select_tree_table_row`、`get_tree_table_cell`
- `expand_tree_table_item`、`collapse_tree_table_item`、`get_tree_table_expanded`

**Actions — ColorPicker**
- `set_color`、`get_color`、`open_colorpicker`、`dismiss_colorpicker`

**Actions — SplitPane**
- `get_divider_positions`、`set_divider_position`

**選擇器（Selectors）**
- 所有 action 均接受 `**selector` 欄位：`id`、`text`、`type`、`index`
- `index=N`（0-based）在符合其他條件的節點中選取第 N 個 — 例如 `click(type="Button", index=1)` 點擊第二個 Button

**Demo suite**（全部通過，執行 `python demo/python/run_all.py`）
- Login、ComboBox、ListView、TableView、TreeView、ContextMenu、MenuBar、DatePicker、Alert
- RadioButton、Slider+Spinner、Progress、Tab、TextArea、PasswordField、Hyperlink
- CheckBox、ChoiceBox、Accordion、TreeTableView、ColorPicker、SplitPane、Node State、Wait Conditions
- Double-Click、Keyboard Shortcuts、Index Selector

尚未完成：
- 對任意第三方 JavaFX process 的動態 JVM attach
- 真正的 OCR 引擎整合，例如 Tesseract / PaddleOCR
- 真正的 vision backend，例如 OpenCV template matching
- 對 fallback bounds 做 OS-level 真實滑鼠點擊

## 專案結構

```text
omniui/
  core/
  selector_engine/
  ocr_module/
  vision_module/
  recorder_lite/
java-agent/
demo/
  java/
    core-app/    ← ComboBox、ListView、TreeView、TableView、GridPane  (port 48100)
    input-app/   ← TextArea、Checkboxes、Slider、ColorPicker、DatePicker …  (port 48101)
    advanced-app/← ContextMenu、MenuBar、Dialog、TabPane、Accordion …       (port 48102)
    login-app/   ← 登入表單（username、password、loginButton、status）       (port 48108)
  python/
    core/        ← core-app demo 腳本
    input/       ← input-app demo 腳本
    advanced/    ← advanced-app demo 腳本
    login/       ← login-app demo 腳本
scripts/
openspec/
```

## 環境需求

- Python 3.11+
- Java 22+
- Maven 3.9+
- 目前 demo app 主要以 Windows 為驗證環境

## 快速開始

1. 建置所有 demo app（Java agent + jlink runtime image）：

```bash
scripts\build_demo_runtime.bat
```

2. 啟動 JavaFX demo app（以 core-app 為例，port 48100）：

```bash
demo\java\core-app\run-dev-with-agent.bat
```

這會開啟 demo 視窗，並以 OmniUI Java agent enabled 模式啟動，預設監聽 `http://127.0.0.1:48100`。

3. 在另一個 terminal 執行 Python demo flow：

```bash
python scripts/demo_login_flow.py
```

目前 demo flow 會展示：
- `username` / `password` 使用 JavaFX direct interaction
- `loginButton` 用 `text="Login"` 觸發 OCR fallback
- 最後驗證 `status == "Success"`

4. 若 demo app 仍在執行，可另外跑 benchmark：

```bash
python scripts/benchmark_phase1.py
```

這會輸出：
- JavaFX node query 平均耗時
- OCR fallback 平均耗時

更多可直接執行的 demo：
- [demo/README.zh-TW.md](demo/README.zh-TW.md)

單一指令 demo 入口：

```bash
python scripts/run_demo.py
```

打包版 demo runtime 輔助腳本：
- `scripts/build_demo_runtime.ps1`
- `scripts/build_demo_runtime.bat`
- `scripts/build_demo_runtime.sh`

建置完成後，這些 helper 會直接列出下一步可用的 packaged `with-agent` 與 `plain` launcher。

其中 `.sh` 輔助腳本目前主要給 Windows 上的 Git Bash 使用。demo app 與打包後 launcher 流程仍以 Windows 為主要文件與驗證環境。

## Python API 範例

文件入口：

- [docs/index.zh-TW.md](docs/index.zh-TW.md)
- [架構圖](docs/architecture.zh-TW.md)
- [Manual smoke checklist](docs/manual-smoke.zh-TW.md)

完整 API 文件：

- [docs/api/python-client.zh-TW.md](docs/api/python-client.zh-TW.md)

最小範例：

```python
from omniui import OmniUI

client = OmniUI.connect(app_name="LoginDemo")

client.click(id="username")
client.type("admin", id="username")

client.click(id="password")
client.type("1234", id="password")

client.click(text="Login")
client.verify_text("Success", id="status")                                 # exact (default)
client.verify_text("Suc", match="contains", id="status")                  # contains
client.verify_text(r"^Succ", match="regex", id="status")                  # regex
```

參數、回傳 model、fallback 行為與欄位定義，請以完整 API 文件為準，不建議把 README 當成正式 API reference。

Demo / 展示模式 — 用 `step_delay` 放慢執行速度：

```python
# 每個 action 後等 0.5 秒（全域設定）
client = OmniUI.connect(port=48102, step_delay=0.5)

# 或單次覆蓋
client.click(id="tbNew", delay=1.0)
```

## Recorder-lite

目前 recorder-lite 是從 client 的 action history 產生 script，只輸出穩定的 click selector，不會輸出 raw coordinate。

範例輸出：

```text
click(id="username")
click(text="Login")
```

不穩定或無法安全表達的互動會直接略過，不會硬產生脆弱腳本。

## 測試

OmniUI 測試分兩層：

| 指令 | 執行內容 | 啟動 UI？ | 速度 |
|------|----------|-----------|------|
| `pytest` 或 `pytest tests/` | 純單元測試（mock，無真實 app） | ❌ | 快（~0.5 秒）|
| `pytest tests/integration/` | 整合測試（啟動真實 JavaFX app） | ✅ | 慢 |

執行單元測試：

```bash
python -m pytest tests/
```

執行整合測試（需先建置 Java agent 與 demo app）：

```bash
python -m pytest tests/integration/
```

建置 Java agent 與 demo app：

```bash
# 先將 agent module 安裝到本機 Maven repo
mvn install -f java-agent/pom.xml
# 再建置 jlink runtime image（以 core-app 為例）
mvn package javafx:jlink -f demo/java/core-app/pom.xml
```

> **注意：** jlink runtime image 將 `dev.omniui.agent` 內嵌為 module。
> 重建 jlink image 前務必先執行 `mvn install -f java-agent/pom.xml`，
> 否則 agent 的修改不會生效。

檢查 Markdown 國際化一致性：

```bash
python scripts/check_markdown_i18n.py
```

## OpenSpec

這個實作是透過 OpenSpec 流程開發，artifact 位於：

[openspec/changes/add-omniui-javafx-automation-framework](openspec/changes/add-omniui-javafx-automation-framework)

主要文件：
- [proposal.zh-TW.md](openspec/changes/add-omniui-javafx-automation-framework/proposal.zh-TW.md)
- [design.zh-TW.md](openspec/changes/add-omniui-javafx-automation-framework/design.zh-TW.md)
- [tasks.zh-TW.md](openspec/changes/add-omniui-javafx-automation-framework/tasks.zh-TW.md)

## 補充說明

- agent protocol 定義在 [agent-protocol.zh-TW.md](docs/protocol/agent-protocol.zh-TW.md)
- 目前 demo support path 由 Java agent 擁有 HTTP startup 與 JavaFX runtime discovery
- repo 內的 OCR / vision engine 目前是 deterministic placeholder implementation，目的是先把 Phase 1 架構與資料流跑通，之後再換成正式套件
