## Purpose

定義 JavaFX Hyperlink 控制項的 automation 行為。

## Requirements

# hyperlink-automation

## Overview
自動化對 JavaFX `Hyperlink` node 的點擊互動，並驗證其 visited 狀態。

## Requirements

### REQ-HL-01: Click hyperlink
對 `Hyperlink` node 執行 `click` action，MUST 透過 `fire()` 觸發其 `onAction` handler。

### REQ-HL-02: Visited state after click
執行 `click` action 後，`Hyperlink` node 的 `isVisited()` 屬性 MUST 回傳 `true`。
此值 MUST 可透過 `get_value` 讀取。

### REQ-HL-03: Initial visited state
在任何點擊之前，對 `Hyperlink` 執行 `get_value` MUST 回傳 `false`（即 `isVisited()` 為 false）。

### REQ-HL-04: Selector targeting
`Hyperlink` node MUST 可透過 `#id` selector 語法以其 `fx:id` 定位。