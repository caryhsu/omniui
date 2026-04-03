## 目的

定義 JavaFX Button 控制項的自動化行為。

## 需求規格

### 需求：Button 點擊
Button 自動化實作 SHALL 支援透過呼叫 `fire()` 方法來觸發 Button。

#### 情境：點擊 Button
- **當** 呼叫 `click(id="<buttonId>")`
- **則** 對目標 Button 呼叫 `fire()`，且該 Button 的動作處理器執行

#### 情境：點擊僅觸發目標 Button
- **當** 頁面存在多個 Button，且對其中一個呼叫 `click`
- **則** 僅有該 Button 的動作處理器被觸發
