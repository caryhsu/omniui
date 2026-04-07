## Why

After recording a script, users must manually copy the code into a Python file or REPL to verify it works. Adding Run controls directly to the Recorder GUI removes this friction and lets users immediately validate what they just recorded — similar to how SQL tools let you run all or just the selected statements.

## What Changes

- Add **Run All** button to Recorder GUI: executes the entire recorded script in a background thread against the connected agent
- Add **Run Selection** button: executes only the text selected in the script preview widget
- Display run status in the GUI (running / passed / failed) with a brief error message on failure
- Recorder GUI connects to the agent for playback using the same `OmniUIClient` session already used for recording

## Capabilities

### New Capabilities
- `recorder-run-all`: Run All button executes the full recorded script via `exec()` against a live `OmniUIClient`; shows pass/fail status in the GUI
- `recorder-run-selection`: Run Selection executes only the highlighted text in the script preview; parses the selection as a standalone Python snippet

### Modified Capabilities
- `recorder-lite`: Recorder GUI gains two new toolbar buttons (Run All, Run Selection) and a status bar area

## Impact

- `omniui/recorder/gui.py` — add Run All / Run Selection buttons, status bar, execution thread
- No changes to Java agent, engine.py, or script_gen.py
- No new dependencies (uses stdlib `exec` + `threading`)
