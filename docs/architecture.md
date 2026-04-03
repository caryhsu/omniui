# OmniUI Architecture

This page collects the current Phase 1 architecture diagrams for OmniUI.

## System Architecture

```mermaid
flowchart TD
    User[Python Script / Test]
    Client[OmniUI Python Client]
    Engine[Automation Engine]
    Selector[Selector Resolution]
    Agent[Local Java Agent]
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

## Login Flow Sequence

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
    Client-->>Script: verified
```

## Selector Resolution Flow

```mermaid
flowchart TD
    Start[Action Request] --> Normalize[Normalize Selector]
    Normalize --> JavaFX[Try JavaFX Resolution]
    JavaFX -->|Resolved| Done[Return ActionResult / ResolvedElement]
    JavaFX -->|selector_not_found| Refresh[Refresh Node Snapshot]
    Refresh --> Retry[Retry JavaFX Resolution]
    Retry -->|Resolved| Done
    Retry -->|Not Resolved| OCR[Try OCR Text Match]
    OCR -->|Resolved| OCRDone[Return OCR-based Result]
    OCR -->|Not Resolved| Vision[Try Vision Template Match]
    Vision -->|Resolved| VisionDone[Return Vision-based Result]
    Vision -->|Not Resolved| Fail[Return Unresolved ActionResult]
```

## Recorder-lite Flow

```mermaid
flowchart TD
    Action[Executed Action] --> History[Append to action_history]
    History --> Recorder[RecorderLite reads ActionLogEntry]
    Recorder --> CheckClick{Action is successful click?}
    CheckClick -->|No| Skip[Skip entry]
    CheckClick -->|Yes| CheckId{Selector has id?}
    CheckId -->|Yes| EmitId[Emit click(id="...")]
    CheckId -->|No| CheckTypeText{Selector has text and type?}
    CheckTypeText -->|Yes| EmitTypeText[Emit click(text="...", type="...")]
    CheckTypeText -->|No| CheckOcr{Resolved via OCR and selector has text?}
    CheckOcr -->|Yes| EmitText[Emit click(text="...")]
    CheckOcr -->|No| SkipStable[Skip unstable interaction]
```

## Notes

- The current fallback path records OCR or vision resolution but does not yet dispatch a real OS-level click for fallback bounds.
- The JavaFX path is the primary supported execution path in Phase 1.
- Recorder-lite is generated from action history after execution rather than full low-level desktop recording.
