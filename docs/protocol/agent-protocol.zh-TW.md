# OmniUI 本機 Agent Protocol

Phase 1 在 Python client 與 Java agent 之間使用只綁定 loopback 的 HTTP JSON protocol。

## Transport

- Base URL: `http://127.0.0.1:48100`
- Content type: `application/json`
- Authentication: Phase 1 本機開發模式下不啟用
- Session model: Python client 在 discovery 或 action 之前建立或恢復 session

## Endpoints

### `GET /health`

回傳 agent 存活狀態與 protocol 版本資訊。

Response shape:

```json
{
  "status": "ok",
  "version": "0.1.0",
  "transport": "http-json"
}
```

### `POST /sessions`

建立一個綁定本機 JavaFX target 的 automation session。

Request shape:

```json
{
  "target": {
    "appName": "LoginDemo",
    "pid": 12345
  }
}
```

Response shape:

```json
{
  "sessionId": "session-local-001",
  "appName": "LoginDemo",
  "platform": "javafx",
  "capabilities": ["discover", "action", "screenshot"]
}
```

### `POST /sessions/{sessionId}/discover`

回傳 JavaFX node snapshot。

Request shape:

```json
{
  "includeHidden": false,
  "maxDepth": 8
}
```

Response shape:

```json
{
  "nodes": [
    {
      "handle": "node-001",
      "fxId": "loginButton",
      "nodeType": "Button",
      "text": "Login",
      "hierarchyPath": "/Scene/VBox/Button[1]",
      "visible": true,
      "enabled": true
    }
  ]
}
```

### `POST /sessions/{sessionId}/actions`

執行標準化後的 action request。

Request shape:

```json
{
  "action": "click",
  "selector": {
    "id": "loginButton"
  },
  "payload": {}
}
```

Response shape:

```json
{
  "ok": true,
  "resolved": {
    "tier": "javafx",
    "targetRef": "node-001",
    "matchedAttributes": {
      "fxId": "loginButton"
    },
    "confidence": null
  },
  "trace": {
    "attemptedTiers": ["javafx"],
    "resolvedTier": "javafx"
  },
  "value": null
}
```

### `POST /sessions/{sessionId}/screenshot`

回傳目前 target 的影像 payload 或其參考資訊。

Request shape:

```json
{
  "format": "png"
}
```

Response shape:

```json
{
  "contentType": "image/png",
  "encoding": "base64",
  "data": "<base64>"
}
```

### `POST /sessions/{sessionId}/events/start`

在 Scene 掛 JavaFX `EventFilter`，開始擷取 `MOUSE_CLICKED` 和 `KEY_TYPED` 事件。同一節點上連續的 KEY_TYPED 事件會在短時間內聚合為單一 `type` 記錄。

Request shape: `{}` （空 body）

Response shape:

```json
{ "ok": true }
```

### `DELETE /sessions/{sessionId}/events`

移除 EventFilter 並 flush 所有緩衝事件。回傳已擷取的事件列表。

Request shape: `{}` （空 body）

Response shape:

```json
{
  "ok": true,
  "events": [
    {
      "type": "click",
      "fxId": "tbNew",
      "text": "",
      "nodeType": "Button",
      "nodeIndex": 0,
      "timestamp": 1712345678.123
    },
    {
      "type": "type",
      "fxId": "inputField",
      "text": "hello",
      "nodeType": "TextField",
      "nodeIndex": 0,
      "timestamp": 1712345679.456
    }
  ]
}
```

## Resolution semantics

- 解析順序固定為：JavaFX structural lookup、OCR text lookup、vision template match
- OCR 與 vision 回應必須包含 `confidence`
- 當 structural node 可用時，JavaFX direct action 不可依賴 screen coordinates
