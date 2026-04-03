# JavaFX Login Demo

Reference app for OmniUI Phase 1.

What it does:
- remains a plain JavaFX app without direct OmniUI bootstrap code
- exposes a simple username/password/login/status workflow for automation

## Development Run

Choose one startup mode.

### Plain development mode

Command Prompt:

```bat
demo\javafx-login-app\run-dev-plain.bat
```

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-plain.ps1
```

Git Bash on Windows:

```bash
./demo/javafx-login-app/run-dev-plain.sh
```

### Agent-enabled development mode

This mode launches the app with `-javaagent` and allows Python automation.

Command Prompt:

```bat
demo\javafx-login-app\run-dev-with-agent.bat
```

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-with-agent.ps1
```

Git Bash on Windows:

```bash
./demo/javafx-login-app/run-dev-with-agent.sh
```

In agent-enabled mode, the Java agent listens on `http://127.0.0.1:48100`.

## Packaged Runtime

### Step 1. Build the runtime image

Choose one shell for the build step.

#### Option A: PowerShell (`.ps1`)

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build_demo_runtime.ps1
```

#### Option B: Command Prompt (`.bat`)

```bat
scripts\build_demo_runtime.bat
```

#### Option C: Git Bash on Windows (`.sh`)

```bash
./scripts/build_demo_runtime.sh
```

After the build completes, the script prints the supported packaged launchers for both modes.

#### Manual Maven commands

```bash
mvn -pl java-agent -am clean install
mvn -f demo/javafx-login-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
```

### Step 2. Start the packaged demo app

Choose one startup mode.

#### Mode A: With OmniUI agent

This mode opens the local automation port and allows Python control.

Command Prompt:

```bat
demo\javafx-login-app\run-with-agent.bat
```

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-with-agent.ps1
```

Git Bash on Windows:

```bash
./demo/javafx-login-app/run-with-agent.sh
```

#### Mode B: Plain app

This mode behaves more like production. It does not open the OmniUI HTTP port, and Python automation cannot attach.

Command Prompt:

```bat
demo\javafx-login-app\run-plain.bat
```

PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-plain.ps1
```

Git Bash on Windows:

```bash
./demo/javafx-login-app/run-plain.sh
```

### Step 3. Run Python demos against the app

This step only applies when you started the app in `with-agent` mode.

Once the demo app window is open, start a Python demo in another terminal:

```bash
python scripts/run_demo.py
```

or:

```bash
python scripts/demo_login_flow.py
```

Notes:
- This is the recommended packaged startup path because it avoids maintaining a long `java --module-path ... -cp ...` command by hand.
- The first command installs the local `omniui-java-agent` dependency into the Maven repository so the demo app can resolve it during `jlink`.
- The generated runtime image contains the resolved Java runtime and JavaFX modules needed by the demo.
- If you need a distributable archive, `javafx:jlink` also produces a zip under `target/`.
- The `run-dev-with-agent.*` and `run-dev-plain.*` scripts are the supported development-time launchers.
- On the current Windows build, `jlink` generates a Windows launcher such as `omniui-login-demo.bat`. It does not generate `.ps1` or `.sh` launchers.
- The `run-with-agent.*` scripts directly invoke the packaged runtime `java` executable with `-javaagent:<repo>/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar`.
- The `run-plain.*` scripts directly invoke the same packaged runtime without Java agent injection.
- The `.sh` helpers are provided for Git Bash usage on Windows; they convert relevant paths to Windows form before invoking Java. They should not be read as a claim that native Linux or macOS packaging has been validated.
