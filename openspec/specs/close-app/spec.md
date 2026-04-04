## Purpose

Define the `close_app` agent action and Python client method that trigger graceful JavaFX application shutdown.

## Requirements

### Requirement: close_app triggers graceful JVM shutdown
The system SHALL provide a `close_app` agent action that schedules `System.exit(0)` on a short-delay daemon thread, ensuring the full JVM — including the HTTP server — terminates cleanly regardless of how the JavaFX application was closed.

#### Scenario: Application and agent exit after close_app
- **WHEN** a Python script calls `client.close_app()` against a running JavaFX application
- **THEN** the agent returns a success result, then calls `System.exit(0)` after a 200 ms flush delay, terminating both the JavaFX runtime and the HTTP server

#### Scenario: Agent returns success before JVM exits
- **WHEN** `close_app` is dispatched
- **THEN** the agent returns an `ActionResult` with `ok=true` before `System.exit(0)` fires, allowing the HTTP response to flush to the Python client

### Requirement: Agent exits when JavaFX window is closed externally
The system SHALL start a daemon monitor thread (`omniui-exit-monitor`) at agent startup that calls `System.exit(0)` as soon as the JavaFX Application Thread exits, ensuring the process terminates cleanly when the user closes the window via the OS (e.g., clicking [x]).

#### Scenario: Closing window via [x] terminates the process
- **WHEN** the user closes the JavaFX application window using the OS window close button
- **THEN** the `omniui-exit-monitor` thread detects the JavaFX Application Thread has stopped and calls `System.exit(0)`, terminating the HTTP server and the JVM without requiring manual intervention

