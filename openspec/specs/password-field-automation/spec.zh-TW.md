## Purpose

定義 JavaFX PasswordField 控制項的 automation 行為。

## Requirements

# password-field-automation

## Overview
使用現有的 `get_text` 與 `type` action，自動化對 JavaFX `PasswordField` node 的讀寫操作。
遮罩顯示僅為視覺效果；automation 讀寫的是底層明文值。

## Requirements

### REQ-PF-01: Write password text
對 `PasswordField` node 執行 `type` action，MUST 將其文字內容設定為所提供的明文字串。

### REQ-PF-02: Read password as plain text
對 `PasswordField` node 執行 `get_text` action，MUST 回傳未遮罩的明文值（而非 UI 中顯示的圓點或遮罩字元）。

### REQ-PF-03: Clear password
以空字串呼叫 `type`，MUST 清除 `PasswordField` 的內容。

### REQ-PF-04: Selector targeting
`PasswordField` node MUST 可透過 `#id` selector 語法以其 `fx:id` 定位。