# OmniUI 架構圖

本頁整理 OmniUI Phase 1 目前的架構圖。

## 系統架構圖

```mermaid
flowchart TD
    User[Python Script / Test]
    Client[OmniUI Python Client]
    Engine[Automation Engine]
    Selector[Selector Resolution]
    Agent[本機 Java Agent]
    JavaFX[JavaFX Runtime Adapter]
    OCR[OCR Fallback]
    Vision[Vision Fallback]
    App[JavaFX Target App]
    Recorder[Recorder-lite]
    Trace[Action Trace / History]

    User --> Client
    Client --> Engine
    Engine --> Selector
    Selector --> Agent
    Agent --> JavaFX
    JavaFX --> App

    Selector --> OCR
    Selector --> Vision

    Engine --> Recorder
    Engine --> Trace
```

## Login Flow Sequence Diagram

```mermaid
sequenceDiagram
    participant Script as Python Script
    participant Client as OmniUI Client
    participant Agent as Local Java Agent
    participant App as JavaFX App
    participant OCR as OCR Engine

    Script->>Client: connect(app_name="LoginDemo")
    Client->>Agent: GET /health
    Client->>Agent: POST /sessions
    Agent-->>Client: sessionId

    Script->>Client: click(id="username")
    Client->>Agent: POST /actions (click, id=username)
    Agent->>App: direct JavaFX click
    Agent-->>Client: ActionResult(javafx)

    Script->>Client: type("admin", id="username")
    Client->>Agent: POST /actions (type)
    Agent->>App: setText("admin")
    Agent-->>Client: ActionResult(javafx)

    Script->>Client: click(text="Login")
    Client->>Agent: POST /actions (click, text=Login)
    Agent-->>Client: selector_not_found
    Client->>Agent: POST /discover
    Client->>Agent: POST /actions (retry)
    Agent-->>Client: selector_not_found
    Client->>Agent: POST /screenshot
    Agent-->>Client: screenshot payload
    Client->>OCR: read(image)
    OCR-->>Client: text match + confidence
    Client-->>Script: ActionResult(ocr)

    Script->>Client: verify_text(id="status", expected="Success")
    Client->>Agent: POST /actions (get_text)
    Agent->>App: read label text
    Agent-->>Client: "Success"
    Client-->>Script: 驗證成功
```

## Selector Resolution Flow

```mermaid
flowchart TD
    Start[收到 Action Request] --> Normalize[正規化 Selector]
    Normalize --> JavaFX[嘗試 JavaFX Resolution]
    JavaFX -->|Resolved| Done[回傳 ActionResult / ResolvedElement]
    JavaFX -->|selector_not_found| Refresh[Refresh Node Snapshot]
    Refresh --> Retry[重新嘗試 JavaFX Resolution]
    Retry -->|Resolved| Done
    Retry -->|未解析| OCR[嘗試 OCR Text Match]
    OCR -->|Resolved| OCRDone[回傳 OCR-based Result]
    OCR -->|未解析| Vision[嘗試 Vision Template Match]
    Vision -->|Resolved| VisionDone[回傳 Vision-based Result]
    Vision -->|未解析| Fail[回傳 Unresolved ActionResult]
```

## Recorder-lite Flow

```mermaid
flowchart TD
    Action[Action 已執行] --> History[寫入 action_history]
    History --> Recorder[RecorderLite 讀取 ActionLogEntry]
    Recorder --> CheckClick{是否為成功 click?}
    CheckClick -->|否| Skip[略過此 entry]
    CheckClick -->|是| CheckId{Selector 是否有 id?}
    CheckId -->|是| EmitId[輸出 click(id="...")]
    CheckId -->|否| CheckTypeText{Selector 是否有 text 與 type?}
    CheckTypeText -->|是| EmitTypeText[輸出 click(text="...", type="...")]
    CheckTypeText -->|否| CheckOcr{是否為 OCR resolve 且 selector 有 text?}
    CheckOcr -->|是| EmitText[輸出 click(text="...")]
    CheckOcr -->|否| SkipStable[略過不穩定互動]
```

## 補充說明

- 目前 fallback path 會記錄 OCR / vision resolution，但尚未對 fallback bounds 發出真正的 OS-level click。
- Phase 1 的主執行路徑仍然是 JavaFX direct interaction。
- Recorder-lite 是根據 action history 產生，而不是完整的低階桌面錄製。
