## Why

Accordion 與 TitledPane 是 JavaFX 中常見的收合式面板控制項，廣泛用於設定頁、側欄與詳細資訊區塊。
目前 automation 框架尚未支援展開／收合操作，也無法讀取面板的展開狀態，是實際應用中明顯的缺口。

## What Changes

- 在 Java agent 新增 `expand_pane` / `collapse_pane` / `get_expanded` action 處理 TitledPane 的展開與收合
- 支援透過 Accordion 的 `expandedPane` 屬性讀取目前展開的面板
- 在 `LoginDemoApp.java` 新增 Accordion demo 區塊（含 3 個 TitledPane）
- 新增 `accordion_demo.py` Python demo script
- 在 `omniui/core/engine.py` 新增 `expand_pane`、`collapse_pane`、`get_expanded` 方法
- 將 demo 加入 `run_all.py`

## Capabilities

### New Capabilities

- `accordion-titledpane-automation`: 定義 JavaFX Accordion 與 TitledPane 的 automation 行為，包含展開、收合與狀態讀取

### Modified Capabilities

- `javafx-automation-core`: 新增 Accordion、TitledPane 為受支援的控制項類型
- `advanced-javafx-demo-scenarios`: 新增 Accordion/TitledPane 的 demo 場景描述

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java`: 新增 3 個 action case 與對應的 handler 方法
- `omniui/core/engine.py`: 新增 3 個 public 方法
- `demo/javafx-login-app/.../LoginDemoApp.java`: 新增 Accordion demo 區塊
- `demo/python/`: 新增 `accordion_demo.py`，更新 `run_all.py`
