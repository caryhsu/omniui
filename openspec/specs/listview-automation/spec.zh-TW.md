## 目的

定義 JavaFX ListView 控制項的自動化行為。

## 需求規格

### 需求：ListView 項目選取
ListView 自動化實作 SHALL 支援透過字串值選取 ListView 中的項目，並同時支援單選與多選模式。

#### 情境：選取已存在的項目
- **當** 呼叫 `select(id="<id>", value="X")`，且 `"X"` 存在於 ListView 項目中
- **則** 項目 `"X"` 變為已選取狀態

#### 情境：選取不存在的項目時回傳 item_not_found
- **當** 呼叫 `select`，且指定的值不在 ListView 項目清單中
- **則** 回傳失敗結果，`reason="item_not_found"`
