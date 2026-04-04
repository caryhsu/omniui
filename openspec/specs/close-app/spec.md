## Purpose

Define the `close_app` agent action and Python client method that trigger graceful JavaFX application shutdown.

## Requirements

### Requirement: close_app triggers graceful JavaFX application shutdown
The system SHALL provide a `close_app` agent action that calls `Platform.exit()` on the JavaFX Application Thread, initiating graceful shutdown of the target application.

#### Scenario: Application exits after close_app
- **WHEN** a Python script calls `client.close_app()` against a running JavaFX application
- **THEN** the agent schedules `Platform.exit()` on the FX thread and returns a success result before the JVM terminates

#### Scenario: Agent returns success before JVM exits
- **WHEN** `close_app` is dispatched
- **THEN** the agent returns an `ActionResult` with `ok=true` before the JVM shutdown sequence completes, allowing the HTTP response to flush to the Python client
