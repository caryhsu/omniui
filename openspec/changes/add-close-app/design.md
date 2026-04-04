## Context

The Java agent handles actions via `ReflectiveJavaFxTarget.perform()`. Overlay/window-level actions (not tied to a specific node) live in the top-level `perform()` switch; node-specific actions fall through to `performOnFxThread()`. `Platform.exit()` is a static JavaFX API that exits the application on the FX thread.

The agent uses `ReflectiveJavaFxSupport.onFxThread()` to schedule work on the JavaFX Application Thread and block until completion. All JavaFX APIs including `Platform.exit()` must be called this way.

## Goals / Non-Goals

**Goals:**
- `close_app()` triggers `Platform.exit()` from the Java agent
- Python client sends `"close_app"` action, gets success before the JVM exits
- Handles the case where the agent's HTTP response may not reach the client (JVM exits during response)

**Non-Goals:**
- Graceful wait/drain (no delay parameter for now)
- Multi-window / multi-stage targeting
- Verifying the app actually closed on the Python side

## Decisions

### Decision: Top-level perform() switch, not performOnFxThread()
**Choice:** Add `case "close_app" -> doCloseApp();` in the top-level `perform()` switch (alongside `dismiss_dialog`, `dismiss_colorpicker`).  
**Rationale:** `close_app` is a window/process-level action with no node selector. Top-level switch handles overlay and window-level actions; `performOnFxThread` handles node-targeted actions.

### Decision: Wrap Platform.exit() in onFxThread()
**Choice:** `doCloseApp()` calls `ReflectiveJavaFxSupport.onFxThread(() -> { Platform.exit(); return success; })`.  
**Rationale:** Consistent with all other JavaFX thread-sensitive operations in the codebase. `Platform.exit()` must be called on or scheduled from the FX thread.

### Decision: Invoke Platform.exit() via reflection
**Choice:** Load `javafx.application.Platform` via `ReflectiveJavaFxSupport.loadClass()` and call `exit()` reflectively.  
**Rationale:** The agent uses the app classloader for all JavaFX access. Direct class reference would bind to the agent's own classloader, which may not have the app's Platform class.

### Decision: Return success before exit completes
**Choice:** Return `ActionResult.success(...)` immediately after scheduling exit, not after confirming shutdown.  
**Rationale:** `Platform.exit()` is asynchronous — the JVM begins shutdown after the current FX pulse. The HTTP response must be sent before the server shuts down. The agent will close with the JVM regardless.

## Risks / Trade-offs

- [Risk] HTTP response may not reach Python client if JVM exits too quickly → Mitigation: `Platform.exit()` schedules shutdown; the current FX thread completes first, giving the HTTP response time to flush.
- [Risk] Client calling methods after `close_app()` will get connection errors → Mitigation: document that `close_app()` should be the last call in a session; Python-side `TimeoutError`/`ConnectionError` is expected.
