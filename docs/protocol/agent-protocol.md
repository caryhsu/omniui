# OmniUI Local Agent Protocol

Phase 1 uses a loopback-only HTTP JSON protocol between the Python client and the Java agent.

## Transport

- Base URL: `http://127.0.0.1:48100`
- Content type: `application/json`
- Authentication: none for Phase 1 local-only development
- Session model: Python client creates or resumes a session before discovery or action calls

## Endpoints

### `GET /health`

Returns agent liveness and protocol version information.

Response shape:

```json
{
  "status": "ok",
  "version": "0.1.0",
  "transport": "http-json"
}
```

### `POST /sessions`

Creates a new automation session bound to a local JavaFX target.

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

Returns a JavaFX node snapshot.

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

Executes a normalized action request.

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

Returns the current target image payload or a reference to it.

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

## Resolution semantics

- Resolution order is fixed: JavaFX structural lookup, OCR text lookup, vision template match.
- OCR and vision responses must include `confidence`.
- Direct JavaFX actions must not depend on screen coordinates when a structural node is available.
