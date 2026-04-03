# JavaFX Login Demo

英文版檔名：`README.md`

這是 OmniUI Phase 1 的參考登入程式。

它會做的事情：
- 保持為純 JavaFX app，不直接包含 OmniUI bootstrap code
- 提供一個可供自動化驗證的 username / password / login / status 流程

## 開發期執行

可依需求選擇其中一種模式。

### Plain development mode

Command Prompt：

```bat
demo\javafx-login-app\run-dev-plain.bat
```

PowerShell：

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-plain.ps1
```

Windows 上的 Git Bash：

```bash
./demo/javafx-login-app/run-dev-plain.sh
```

### Agent-enabled development mode

這個模式會以 `-javaagent` 啟動 app，並允許 Python automation。

Command Prompt：

```bat
demo\javafx-login-app\run-dev-with-agent.bat
```

PowerShell：

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-with-agent.ps1
```

Windows 上的 Git Bash：

```bash
./demo/javafx-login-app/run-dev-with-agent.sh
```

啟用 agent 後，Java agent 會監聽 `http://127.0.0.1:48100`。

## 打包版 Runtime

### Step 1. 先建置 runtime image

建置步驟可依你使用的 shell 選一種方式。

#### 方案 A: PowerShell (`.ps1`)

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build_demo_runtime.ps1
```

#### 方案 B: Command Prompt (`.bat`)

```bat
scripts\build_demo_runtime.bat
```

#### 方案 C: Windows 上的 Git Bash (`.sh`)

```bash
./scripts/build_demo_runtime.sh
```

建置完成後，腳本會直接列出 packaged runtime 在 `with-agent` 與 `plain` 兩種模式下可用的 launcher。

#### 手動 Maven 指令

```bash
mvn -pl java-agent -am clean install
mvn -f demo/javafx-login-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
```

### Step 2. 啟動打包後的 demo app

可依需求選擇其中一種模式。

#### Mode A: 啟用 OmniUI agent

這個模式會打開本機 automation port，可供 Python 控制。

Command Prompt：

```bat
demo\javafx-login-app\run-with-agent.bat
```

PowerShell：

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-with-agent.ps1
```

Windows 上的 Git Bash：

```bash
./demo/javafx-login-app/run-with-agent.sh
```

#### Mode B: Plain app

這個模式比較接近 production，不會打開 OmniUI HTTP port，Python automation 也無法附掛。

Command Prompt：

```bat
demo\javafx-login-app\run-plain.bat
```

PowerShell：

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-plain.ps1
```

Windows 上的 Git Bash：

```bash
./demo/javafx-login-app/run-plain.sh
```

### Step 3. 在另一個 terminal 執行 Python demo

這一步只適用於你用 `with-agent` mode 啟動 app 的情況。

當 demo app 視窗已經開啟後，再在另一個 terminal 執行：

```bash
python scripts/run_demo.py
```

或：

```bash
python scripts/demo_login_flow.py
```

說明：
- 這是目前比較推薦的打包後啟動方式，因為不需要手動維護一長串 `java --module-path ... -cp ...` 指令。
- 第一個指令會先把本地的 `omniui-java-agent` 安裝到 Maven repository，讓 demo app 在 `jlink` 時能正確解析它。
- 產生出的 runtime image 會包含這個 demo 所需的 Java runtime 與 JavaFX module。
- 若需要可分發的壓縮檔，`javafx:jlink` 也會在 `target/` 下產生 zip。
- `run-dev-with-agent.*` 與 `run-dev-plain.*` 是目前支援的開發期啟動腳本。
- 在目前的 Windows 建置下，`jlink` 會產生像 `omniui-login-demo.bat` 這樣的 Windows launcher，不會自動產生 `.ps1` 或 `.sh` launcher。
- `run-with-agent.*` 會直接呼叫 packaged runtime 內的 `java`，並加上 `-javaagent:<repo>/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar`。
- `run-plain.*` 則會直接呼叫同一個 packaged runtime，但不注入 Java agent。
- `.sh` 輔助腳本主要是提供 Windows 上的 Git Bash 使用，且在呼叫 Java 前會把相關路徑轉成 Windows 形式；這不代表目前已驗證原生 Linux 或 macOS 的打包流程。
