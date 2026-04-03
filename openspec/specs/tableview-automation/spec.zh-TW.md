## 目的

定義 JavaFX TableView 控制項的自動化行為。

## 需求規格

### 需求：TableView 列選取
TableView 自動化實作 SHALL 支援透過比對儲存格值來選取表格列。可使用選用的 `column` 參數將搜尋範圍縮限至特定欄位。

#### 情境：依儲存格值選取列
- **當** 呼叫 `select(id="<id>", value="X")`
- **則** 包含值為 `"X"` 的儲存格的列變為已選取狀態

#### 情境：使用欄位提示選取列
- **當** 呼叫 `select(id="<id>", value="X", column="Name")`
- **則** `Name` 欄位值等於 `"X"` 的列變為已選取狀態

#### 情境：找不到指定值時回傳失敗
- **當** 呼叫 `select`，且指定的值在任何列中均不存在
- **則** 回傳失敗結果，`reason="select_not_supported"`
