## Why

The current JavaFX demo app proves the Phase 1 architecture, but it does so by embedding OmniUI-specific classes directly into the target application. That makes the sample invasive, weakens the product story for real applications, and hides the harder integration problem that OmniUI must solve next: controlling an unmodified JavaFX app through a standard Java agent boundary.

## What Changes

- Move JavaFX runtime discovery and agent startup out of the target app and into the `java-agent` module.
- Introduce standard Java agent entrypoints so OmniUI can be loaded with `-javaagent` and prepare for future dynamic attach support.
- Remove direct `OmniUiAgentServer`, `JavaFxRuntimeBridge`, and related OmniUI bootstrap code from the demo JavaFX application.
- Add explicit launch modes and documentation for running the demo app with or without agent injection.
- Define how the Python client connects when the Java agent is present, and how the app behaves when it is not.

## Capabilities

### New Capabilities

- `standard-java-agent-injection`: Standard JVM agent startup and JavaFX runtime discovery without requiring OmniUI code inside the target app.

### Modified Capabilities

- `javafx-automation-core`: JavaFX automation must work when the target app is launched with a standard Java agent instead of app-embedded bootstrap code.
- `python-automation-client`: Client connection behavior must clearly distinguish between agent-enabled and plain app launch modes.

## Impact

- Affected code: `java-agent/`, `demo/javafx-login-app/`, demo launch scripts, Python demo scripts, and related documentation.
- Affected runtime model: the Java agent becomes the owner of agent HTTP startup and JavaFX runtime attachment.
- Affected operator workflow: demo and future target apps must support agent-enabled launch without modifying app source.
