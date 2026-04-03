## 目的

定義 JavaFX TextField 控制項的自動化行為。

## 需求規格

### 需求：TextField 文字寫入
TextField 自動化實作 SHALL 支援透過呼叫 `setText(input)` 來設定 TextField 的文字內容。

#### 情境：設定文字
- **當** 呼叫 `type(id="<id>", input="Hello")`
- **則** `getText()` 回傳 `"Hello"`

#### 情境：清除文字
- **當** 呼叫 `type(id="<id>", input="")`
- **則** `getText()` 回傳 `""`

### 需求：TextField 文字讀取
TextField 自動化實作 SHALL 支援透過呼叫 `getText()` 來讀取 TextField 目前的文字內容。

#### 情境：讀取文字
- **當** 呼叫 `get_text(id="<id>")`
- **則** 回傳 TextField 目前的文字值
