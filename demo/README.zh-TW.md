# OmniUI Demo

本目錄收集 OmniUI Phase 1 的可執行 demo。

## JavaFX target apps

`demo/java/` 底下提供三個專注的 demo app：

| App | Port | 內容 |
|-----|------|------|
| [core-app](java/core-app/) | 48100 | Login flow、ComboBox、ListView、TreeView、TableView、GridPane |
| [input-app](java/input-app/) | 48101 | TextArea、PasswordField、Hyperlink、CheckBox、ChoiceBox、RadioButton、Slider、Spinner、ColorPicker、DatePicker |
| [advanced-app](java/advanced-app/) | 48102 | ContextMenu、MenuBar、Dialog、Alert、TabPane、Accordion、TreeTableView、SplitPane、ProgressBar、NodeState、ScrollPane、Tooltip |

啟動你想測試的 app：

```bash
demo/java/core-app/run-dev-with-agent.bat
demo/java/input-app/run-dev-with-agent.bat
demo/java/advanced-app/run-dev-with-agent.bat
```

每個 app 支援四種啟動模式：`run-dev-with-agent`、`run-dev-plain`、`run-with-agent`（jlink）、`run-plain`（jlink）。

`scripts/` 目錄也提供輔助腳本：
- `build_demo_runtime.ps1`
- `build_demo_runtime.bat`
- `build_demo_runtime.sh`

補充：`.sh` 輔助腳本目前主要用於 Windows 上的 Git Bash。

## Python demo

Python demo 腳本依對應的 app 分別存放在 `demo/python/` 的子目錄：

| 子目錄 | 對應 App | Port |
|--------|---------|------|
| `demo/python/core/` | core-app | 48100 |
| `demo/python/input/` | input-app | 48101 |
| `demo/python/advanced/` | advanced-app | 48102 |

### 一次執行全部 demo

```bash
python demo/python/run_all.py
```

依序執行三個 app 的 demo。需要三個 app 都在 with-agent mode 下執行。

也可以使用：

```bash
python -m demo.python.run_all
python scripts/run_demo.py
```

### 執行特定群組

```bash
# Core demo（需要 core-app 在 port 48100 執行）
python demo/python/core/login_direct.py
python demo/python/core/select_combo_role.py
python demo/python/core/discover_nodes.py

# Input demo（需要 input-app 在 port 48101 執行）
python demo/python/input/text_area_demo.py
python demo/python/input/date_picker_demo.py

# Advanced demo（需要 advanced-app 在 port 48102 執行）
python demo/python/advanced/context_menu_demo.py
python demo/python/advanced/discover_advanced_controls.py
```

### Benchmark

```bash
python demo/python/run_benchmark.py
```

執行 Phase 1 benchmark，輸出 JavaFX node query 與 OCR parsing 的耗時結果。
