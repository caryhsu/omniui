## Context

`TitledPane` 有一個 `expanded` 屬性（`isExpanded()` / `setExpanded(boolean)`），可直接透過反射存取。
`Accordion` 有 `getExpandedPane()` / `setExpandedPane(TitledPane)` 方法，以及 `getPanes()` 回傳所有子面板清單。

目前 Java agent 沒有任何對應的 action，Python engine 也沒有相關方法。
需要新增三個 action：`expand_pane`、`collapse_pane`、`get_expanded`。

## Goals / Non-Goals

**Goals:**
- 支援對 TitledPane 執行展開（`expand_pane`）與收合（`collapse_pane`）
- 支援讀取 TitledPane 的目前展開狀態（`get_expanded`）
- 支援透過標題文字在 Accordion 中展開指定面板
- 提供 Python engine 對應方法
- 提供 demo app 區塊與 Python demo script

**Non-Goals:**
- Accordion 動畫時序的等待（Phase 1 直接設值，不等動畫完成）
- 巢狀 Accordion（Accordion 內含 Accordion）

## Decisions

### 決策 1：action 設計
- `expand_pane(selector)` — 對 TitledPane 設 `setExpanded(true)`
- `collapse_pane(selector)` — 對 TitledPane 設 `setExpanded(false)`
- `get_expanded(selector)` — 對 TitledPane 回傳 `isExpanded()` boolean

Accordion 可透過 `get_nodes()` 列舉子 TitledPane 的 `fx:id`，再以上述 action 操作。不需要單獨的 "open Accordion" action。

### 決策 2：selector 策略
TitledPane 在 LoginDemoApp 中以 `fx:id` 識別（`pane1`, `pane2`, `pane3`）。
Python demo 以 `id=` 選取，與其他控制項一致。

### 決策 3：Accordion 只有一個 pane 可展開
JavaFX Accordion 的特性：展開一個 TitledPane 時，其他 pane 自動收合。
Demo 中利用此特性驗證互斥行為。

## Risks / Trade-offs

- **[風險] setExpanded 在 FX 執行緒外呼叫** → 所有 action 已透過 `onFxThread()` 包裝，無此問題
- **[風險] Accordion 展開狀態與 TitledPane 的 expanded 屬性不同步** → 一律透過 TitledPane 的 `setExpanded` 操作，與 JavaFX 規範一致
