## MODIFIED Requirements

### Requirement: Advanced JavaFX demo coverage
系統必須提供 reference JavaFX demo scenario，涵蓋 basic login flow 以外的進階 control pattern，包含 `ComboBox`、`ListView`、`TreeView`、`TableView`、`ContextMenu`、`MenuBar`、`DatePicker`、`Dialog` 與 `Alert`。

#### Scenario: 開啟 advanced demo scenario
- **WHEN** 使用者啟動 reference JavaFX demo application
- **THEN** application 暴露所有進階 control 的可識別 scenario，包含 ContextMenu、MenuBar、DatePicker、Dialog 與 Alert，而不只是 basic login flow 與 selection control

## ADDED Requirements

### Requirement: Popup 與 overlay control demo scene
系統必須為每種 popup-backed 與 overlay-driven control type——ContextMenu、MenuBar、DatePicker、Dialog 與 Alert——提供專屬 demo scene，並使用穩定、唯一命名的 node 與確定性 sample data。

#### Scenario: 開啟 ContextMenu demo scene
- **WHEN** 使用者導航至 ContextMenu demo scene
- **THEN** scene 呈現一個帶有已註冊 ContextMenu 的 labeled node，ContextMenu 包含穩定的單層 item 與至少一條唯一命名的多層 submenu 路徑

#### Scenario: 開啟 MenuBar demo scene
- **WHEN** 使用者導航至 MenuBar demo scene
- **THEN** scene 呈現一個 MenuBar，包含至少兩個頂層選單，各自包含穩定的單層 item 與至少一條唯一命名的多層 submenu 路徑

#### Scenario: 開啟 DatePicker demo scene
- **WHEN** 使用者導航至 DatePicker demo scene
- **THEN** scene 呈現一個具有穩定初始日期值的 DatePicker，以及一個會更新以反映已選日期的 labeled result node

#### Scenario: 開啟 Dialog demo scene
- **WHEN** 使用者導航至 Dialog demo scene
- **THEN** scene 呈現一個觸發控制，可開啟具有穩定 title、header text、content text，以及已命名 OK 與 Cancel 按鈕的標準 JavaFX Dialog

#### Scenario: 開啟 Alert demo scene
- **WHEN** 使用者導航至 Alert demo scene
- **THEN** scene 呈現各 AlertType（INFORMATION、CONFIRMATION、WARNING、ERROR）的觸發控制，每個都產生具有穩定、可唯一識別 content message 的 Alert

### Requirement: Popup 與 overlay control 的穩定 demo data
系統必須在所有 popup 與 overlay demo scene 中使用確定性、人類可讀的 sample data，使 structural discovery 與 automation assertion 維持可解讀性與可重現性。

#### Scenario: 檢查 popup 與 overlay demo dataset
- **WHEN** 使用者或 Python client 檢查 popup 與 overlay demo scene
- **THEN** 所有 menu item label、dialog text、alert message 與日期值都是穩定、唯一命名，且適合 selector-based automation 使用

### Requirement: 可執行的 popup 與 overlay Python demo
系統必須提供可執行的 Python demo script，涵蓋 ContextMenu、MenuBar、DatePicker、Dialog 與 Alert 的自動化，並輸出足夠的資訊供驗證 discovery 與互動行為。

#### Scenario: 執行 popup 與 overlay control demo script
- **WHEN** 使用者對 agent-enabled application 執行 popup 與 overlay control 的 Python demo
- **THEN** script 涵蓋右鍵與選單 item 選取、MenuBar 導航、日期選取、dialog 關閉與 alert 互動，並回報每種 control type 的 action trace
