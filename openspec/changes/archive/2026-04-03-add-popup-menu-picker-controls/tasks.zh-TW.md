## 1. Java agent overlay 基礎

- [x] 1.1 擴充 Java agent，使用 `Window.getWindows()` 列舉所有開啟中的 overlay window（`PopupWindow`、Dialog `Stage`）
- [x] 1.2 實作 wait-for-overlay 機制：觸發 popup 動作後以短輪詢等待指定類型的 overlay window 出現，並支援可設定的 timeout
- [x] 1.3 擴充 `get_nodes()` endpoint，在 overlay 可見時將 overlay window 的 node 納入回傳結果
- [x] 1.4 加入 overlay node record，標示所屬 window 類型（primary、popup、dialog）

## 2. ContextMenu automation

- [x] 2.1 實作 `right_click` action：對 resolved node dispatch right-click event，並等待 ContextMenu popup 可見
- [x] 2.2 實作單層 ContextMenu item 選取：以 label text 或 `fx:id` 匹配並 dispatch click
- [x] 2.3 實作多層 ContextMenu 遍歷：依 path 逐步 hover 中間 item，等待各層 submenu popup 出現後繼續
- [x] 2.4 實作 `dismiss_menu`：dispatch Escape key event 並等待 ContextMenu popup 關閉

## 3. MenuBar automation

- [x] 3.1 實作 `open_menu` action：以 label text 或 `fx:id` 找到頂層 Menu item，dispatch click 並等待選單 popup 可見
- [x] 3.2 實作單層 MenuBar item 選取：在開啟的選單 popup 中以 label text 或 `fx:id` 匹配並 dispatch click
- [x] 3.3 實作 `navigate_menu`：依 path 從頂層 menu header 逐步 hover 啟動各層 submenu，最後 click 目標 item
- [x] 3.4 擴充 `dismiss_menu` 支援 MenuBar 選單：dispatch Escape key event 並等待選單 popup 關閉

## 4. DatePicker automation

- [x] 4.1 實作 `open_datepicker` action：dispatch click 至 DatePicker calendar toggle button，等待 calendar popup 可見
- [x] 4.2 實作 `navigate_month`：dispatch click 至 forward 或 backward 導航按鈕，等待 calendar grid 更新
- [x] 4.3 實作 `pick_date`：以 `LocalDate` item property 識別目標日期格，視需要自動導航月份，最後 dispatch click

## 5. Dialog automation

- [x] 5.1 實作 `get_dialog`：掃描 `Window.getWindows()` 找到 root 為 `DialogPane` 的 Stage，回傳 title、header text、content text 與 button label 清單
- [x] 5.2 實作 `dismiss_dialog`：支援以 label text 或 `ButtonData` type 匹配 DialogPane ButtonBar 中的按鈕並 dispatch click
- [x] 5.3 實作找不到按鈕時的錯誤回報，列出可用 button label

## 6. Alert automation

- [x] 6.1 擴充 `get_dialog` 以識別 Alert：讀取 `AlertType` 並加入 `alert_type` 欄位至回傳 descriptor
- [x] 6.2 確認四種 AlertType（INFORMATION、CONFIRMATION、WARNING、ERROR）皆可正確偵測與回傳
- [x] 6.3 確認 `dismiss_dialog` 的 label text 與 `ButtonData` 匹配邏輯在 Alert 上同樣適用

## 7. Python client 新增 API

- [x] 7.1 新增 `right_click(selector)` method
- [x] 7.2 新增 `open_menu(text=None, id=None)` 與 `click_menu_item(text=None, id=None, path=None)` method
- [x] 7.3 新增 `navigate_menu(path)` method
- [x] 7.4 新增 `dismiss_menu()` method
- [x] 7.5 新增 `open_datepicker(selector)`、`navigate_month(direction)` 與 `pick_date(date)` method
- [x] 7.6 新增 `get_dialog()` 與 `dismiss_dialog(button=None, button_type=None)` method
- [x] 7.7 確認所有新 method 的 action result 包含 control type、resolution tier 與 action trace

## 8. Demo app 擴充

- [x] 8.1 加入 ContextMenu demo scene：一個 labeled node 帶有單層與多層 ContextMenu，item label 穩定唯一
- [x] 8.2 加入 MenuBar demo scene：至少兩個頂層選單，各含單層 item 與多層 submenu 路徑，label 穩定唯一
- [x] 8.3 加入 DatePicker demo scene：一個 DatePicker 帶有穩定初始日期，以及反映所選日期的 result label
- [x] 8.4 加入 Dialog demo scene：一個觸發按鈕，開啟含穩定 title、header、content 與 OK/Cancel 按鈕的 Dialog
- [x] 8.5 加入 Alert demo scene：四個觸發按鈕各對應一種 AlertType，每個 Alert 有穩定且可識別的 content message

## 9. Python demo script

- [x] 9.1 新增 ContextMenu demo script：執行右鍵觸發、單層 item 選取、多層遍歷，並輸出 action trace
- [x] 9.2 新增 MenuBar demo script：執行頂層選單開啟、單層 item 選取、多層 navigate_menu，並輸出 action trace
- [x] 9.3 新增 DatePicker demo script：執行 popup 開啟、月份導航、日期選取，並輸出 action trace
- [x] 9.4 新增 Dialog demo script：執行 dialog 偵測、內容讀取、以 label 與 ButtonData 各自 dismiss，並輸出 action trace
- [x] 9.5 新增 Alert demo script：偵測四種 AlertType、讀取訊息、dismiss，並輸出 action trace
