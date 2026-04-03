## Purpose

定義 JavaFX TextArea 控制項的 automation 行為。

## Requirements

# text-area-automation

## Overview
使用現有的 `get_text` 與 `type` action，自動化對 JavaFX `TextArea` node 的讀寫操作。

## Requirements

### REQ-TA-01: Write multi-line text
對 `TextArea` node 執行 `type` action，MUST 將其完整內容設定為所提供的字串，包含換行字元（`\n`）。

### REQ-TA-02: Read multi-line text
對 `TextArea` node 執行 `get_text` action，MUST 回傳目前的完整文字內容，並保留換行。

### REQ-TA-03: Clear text
以空字串呼叫 `type`，MUST 清除 `TextArea` 的內容。

### REQ-TA-04: Selector targeting
`TextArea` node MUST 可透過 `#id` selector 語法以其 `fx:id` 定位。