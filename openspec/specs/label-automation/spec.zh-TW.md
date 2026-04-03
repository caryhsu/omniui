## 目的

定義 JavaFX Label 控制項的自動化行為。

## 需求規格

### 需求：Label 文字讀取
Label 自動化實作 SHALL 支援透過呼叫 `getText()` 來讀取 Label 目前的文字內容。

#### 情境：讀取標籤文字
- **當** 呼叫 `get_text(id="<id>")`
- **則** 回傳 Label 目前的文字

#### 情境：空標籤回傳空字串
- **當** 對未設定文字的 Label 呼叫 `get_text`
- **則** 回傳空字串或 null
