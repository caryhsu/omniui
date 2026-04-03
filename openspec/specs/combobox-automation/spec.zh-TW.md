## 目的

定義 JavaFX ComboBox 控制項的自動化行為。

## 需求規格

### 需求：ComboBox 項目選取
ComboBox 自動化實作 SHALL 支援透過值字串從 ComboBox 的項目清單中選取項目。

#### 情境：選取已存在的項目
- **當** 呼叫 `select(id="<id>", value="X")`，且 `"X"` 存在於 ComboBox 項目中
- **則** `getValue()` 回傳 `"X"`

#### 情境：選取不存在的項目時回傳 item_not_found
- **當** 呼叫 `select`，且指定的值不在 ComboBox 項目清單中
- **則** 回傳失敗結果，`reason="item_not_found"`

### 需求：ComboBox 值讀取
ComboBox 自動化實作 SHALL 支援透過呼叫 `getValue()` 來讀取目前選取的值。

#### 情境：讀取已選取的值
- **當** 呼叫 `get_value(id="<id>")`
- **則** 以字串形式回傳目前選取的項目
