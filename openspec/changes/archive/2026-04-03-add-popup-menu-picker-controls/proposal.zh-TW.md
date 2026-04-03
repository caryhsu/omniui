## Why

目前 OmniUI framework 已支援 in-scene selection control（ComboBox、ListView、TreeView、TableView）的 node discovery 與互動。但 popup-backed 與 overlay-driven control——ContextMenu、MenuBar、DatePicker、Dialog、Alert——會在主要 scene graph 之外以短暫的 overlay window 形式出現，需要獨立的 discovery 與互動處理。補上這一組 control，能覆蓋 JavaFX 桌面應用中最常見的 overlay-driven automation pattern。

## What Changes

- 加入 **ContextMenu** 自動化支援——以右鍵或程式觸發開啟、遍歷單層與多層 menu item 階層，並點擊目標 item。
- 加入 **MenuBar** 自動化支援——啟動頂層 menu header、遍歷單層與多層 submenu 階層，並點擊目標 menu item。
- 加入 **DatePicker** 自動化支援——開啟 calendar popup、導航月份，並選取目標日期格。
- 加入 **Dialog** 自動化支援——偵測已開啟的 modal dialog、讀取標題與內容文字，並點擊指定按鈕。
- 加入 **Alert** 自動化支援——偵測 alert 類型（information、confirmation、warning、error）、讀取 alert 訊息，並點擊指定按鈕。
- 擴充 advanced demo application，為每種新 control 加入專屬 demo scene。
- 擴充 advanced Python demo script，涵蓋每種新 control 的互動情境。
- 擴充 `javafx-automation-core`，支援 popup-backed control 的短暫 overlay node 列舉與互動 dispatch。

## Capabilities

### New Capabilities

- `contextmenu-automation`：自動化 ContextMenu control，涵蓋右鍵觸發、單層與多層 item 遍歷，以及 item 選取。
- `menubar-automation`：自動化 MenuBar 選單，涵蓋頂層 header 啟動、單層與多層 submenu 遍歷，以及 item 選取。
- `datepicker-automation`：自動化 DatePicker control，涵蓋 popup 開啟、月份導航，以及日期格選取。
- `dialog-automation`：自動化 modal Dialog control，涵蓋開啟偵測、標題與內容讀取，以及按鈕互動。
- `alert-automation`：自動化 Alert control，涵蓋類型偵測、訊息讀取，以及按鈕互動。

### Modified Capabilities

- `javafx-automation-core`：擴充 node discovery 與互動 dispatch，以涵蓋短暫 overlay-backed control（popup window、context menu、date picker popup、dialog、alert），這些 control 出現在主要 scene graph 之外。
- `advanced-javafx-demo-scenarios`：加入 ContextMenu、MenuBar、DatePicker、Dialog 與 Alert 的 reference demo scene 及穩定 sample data，以支援新的 automation 驗證。

## Impact

- Java agent 除了主要 scene graph，還必須列舉 overlay `Stage` 與 `PopupWindow` node。
- Python client 新增 action method：`right_click`、`open_menu`、`navigate_menu`、`pick_date`、`get_dialog`、`dismiss_dialog`。
- Demo app（`omniui/demo`）為每個新 control 群組加入新的 JavaFX scene。
- 在現有 advanced demo 旁邊新增 Python demo script。
- 不對現有 selector 或 action signature 造成 breaking change。
