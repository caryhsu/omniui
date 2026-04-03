## Context

Phase 1 已證明 OmniUI 可以列舉 JavaFX node、執行 direct action，並透過本機 HTTP control plane 讓 Python 控制。但目前 reference app 是靠把 OmniUI 類別直接嵌進 target app 來完成，這不適合作為長期整合方式，因為 target app 不該負責啟動 agent server 或主動註冊自己的 JavaFX runtime。

下一步要把 ownership 移回 `java-agent`，並把 target app 視為未修改的 JavaFX app。這是跨模組調整，因為會影響 Java agent lifecycle、JavaFX runtime discovery、demo 啟動方式、Python 連線假設與操作文件。

目前限制：
- Phase 1 仍只支援本機與 JavaFX。
- Python client 仍走 loopback HTTP JSON。
- demo app 需保留兩種模式：
  - plain mode：像一般 JavaFX app 一樣運作
  - agent-enabled mode：暴露 OmniUI control plane
- dynamic attach 可以留到後面，但第一版要先有穩定的 startup-time integration。

## Goals / Non-Goals

**Goals:**
- 從 JavaFX demo app source 移除直接的 OmniUI bootstrap code。
- 支援透過 `-javaagent` 進行標準 JVM agent 啟動。
- 讓 Java agent 自己負責啟動 HTTP server 與發現 JavaFX runtime state。
- 保留現有 Python API 型態與 Phase 1 client protocol 相容性。
- 保持 plain launch 與 agent-enabled launch 為清楚可見的兩種模式。
- 為未來 `agentmain` attach 預留空間，但不強迫在第一版全部完成。

**Non-Goals:**
- 這個 change 不做完整 dynamic attach。
- 不支援非 JavaFX toolkit。
- 不一次解決所有 JavaFX stage discovery edge case。
- 不處理 remote transport、auth、多 app orchestration 等 production-hardening。
- 不要求移除所有 wrapper script；demo 與本機驗證仍需要可操作性。

## Decisions

### 1. 以 `-javaagent` 作為主要整合方式

Java agent 會提供標準 `premain(String, Instrumentation)` 入口，並作為啟用 OmniUI automation 的主要方式。

原因：
- `-javaagent` 是最標準、也最不侵入 JVM app 的整合點。
- 可移除 target app source change。
- 能在 JavaFX app 完全啟動前先建立可預測的 lifecycle。

替代方案：
- 繼續把 agent startup 寫在 app 裡：拒絕，因為會把 OmniUI code 留在 target app。
- 先做 dynamic attach：拒絕，因為 lifecycle 複雜度更高。
- 完全用外部 process 跑 HTTP server：暫不採用，因為 agent 仍需 in-process 讀 JavaFX runtime state。

### 2. 將 agent 啟動拆成 bootstrap、runtime discovery、transport 三層

`java-agent` 會在概念上拆成：
- agent bootstrap：`premain` 與未來的 `agentmain`
- transport：HTTP server lifecycle 與 session management
- runtime discovery：JavaFX runtime detection 與 registration
- target adapters：JavaFX automation target instance

原因：
- 現有程式把 server startup 與 target registration 假設混在一起。
- 拆開後較容易同時支援 startup-time injection 與未來 attach flow。
- plain mode 與 agent-enabled mode 的差異可只留在 JVM 啟動方式，而不是 app code。

替代方案：
- 保留現有 server/registry，只是從 `premain` 呼叫：拒絕，因為 embedded-app 假設仍太重，特別是 static demo fallback。

### 3. 以 agent 端 JavaFX discovery 取代 app 端 `registerStage(...)`

agent 要自己發現 JavaFX runtime，而不是要求 app 主動註冊 `Stage` 或 `Scene`。第一版可透過 instrument JavaFX lifecycle boundary，或在 agent 內枚舉可見 stage/scene 來支援 demo 範圍。

預期做法：
- hook 足夠穩定的 JavaFX 啟動點
- JavaFX live 後由 agent 枚舉 window/stage
- 用 discovered scene 建立 `ReflectiveJavaFxTarget`
- 在 agent 內部 registry 註冊 target

原因：
- 這是移除 app 端 OmniUI 依賴的核心。
- 符合「OmniUI 可自動化未修改 JavaFX app」的產品敘事。
- 也保留了目前 reflective access 的彈性。

替代方案：
- 由 app 在 startup 後手動呼叫 service API：拒絕，仍屬侵入式。
- 依賴 FXML/controller annotation：拒絕，因為太 app-specific。

### 4. 保留現有 HTTP JSON protocol，但讓 agent availability 變得明確

Python client 繼續走 loopback HTTP JSON。差異在於 app 是否 agent-enabled：
- plain launch：沒有 agent，沒有 port
- agent-enabled launch：JVM 帶 `-javaagent` 啟動，agent 自己開 HTTP server，Python 可連線

原因：
- 可保留現有 Phase 1 client 與 contract tests。
- 將變動範圍集中在 runtime ownership，而不是同時換 protocol。
- 失敗模式更容易理解：沒有注入 agent 就連不上。

替代方案：
- 同時更換 protocol 與 injection 模型：拒絕，因為一次改動太大。

### 5. 不再將 demo target fallback 視為標準 runtime 行為

`TargetRegistry` 目前有內建 `DemoJavaFxTarget` fallback。新設計不應再把它當作標準 runtime 路徑。

方向：
- demo fallback 僅保留為 test-only 或明確 opt-in
- 真正 runtime registration 來自 agent-side discovery
- target 找不到時要明確失敗，而不是默默退回 demo target

原因：
- silent fallback 會掩蓋整合失誤。
- 會讓 agent-enabled/plain mode 的界線模糊。
- 也會削弱對真實 app automation 的信心。

替代方案：
- 為了方便保留 production fallback：拒絕，因為會讓診斷與行為混亂。

### 6. 保留 wrapper script，但模式由 JVM argument 控制

demo scripts 與 packaged launcher 仍應保持簡單，但模式要透過 JVM launch configuration 表達：
- with-agent wrappers 加上 `-javaagent:...`
- plain wrappers 直接正常啟動 app

原因：
- 可同時提供兩種模式，又不污染 app code。
- README 與 demo flow 也較易理解。
- 也更貼近真實客戶 app 在 dev 或 CI 的啟動方式。

替代方案：
- 移除 wrapper script，要求使用者手打完整 Java command：拒絕，因為 demo 可用性會明顯下降。

## Risks / Trade-offs

- [沒有 app cooperation 的 JavaFX discovery 可能對不同 launch pattern 較脆弱] -> Mitigation: 先把 support matrix 限縮在 reference demo app 與 startup-time `-javaagent`。
- [instrumentation hook 可能依賴 JavaFX internals，穩定性不如 app-side register] -> Mitigation: discovery logic 要隔離在 runtime discovery layer，並盡量沿用 reflective access。
- [移除 default demo fallback 可能打破現有 demo 與 tests 的假設] -> Mitigation: 更新 tests 與 demo scripts，明確要求 agent-enabled launch。
- [packaged runtime launchers 因為要注入 `-javaagent` 而較複雜] -> Mitigation: 保留 dedicated wrapper scripts，並把它們當成正式 operator entrypoint。
- [未來 dynamic attach 與 `premain` 所需 lifecycle 不同] -> Mitigation: bootstrap 與 transport 做成可重用層，之後可加入 `agentmain` 而不重寫 target adapter。

## Migration Plan

1. 在 `java-agent` 增加標準 Java agent bootstrap entrypoints。
2. 重構 server startup，讓 agent 擁有 HTTP transport lifecycle。
3. 針對 demo support matrix 實作 agent-side JavaFX runtime discovery。
4. 從 `demo/javafx-login-app` 移除 embedded OmniUI startup 與 registration code。
5. 將 `with-agent` / `plain` wrapper scripts 改為透過 Java agent injection 與 normal launch 啟動。
6. 更新 Python demos 與文件，讓它們假設 agent availability 取決於 launch mode，而不是 app source。
7. 將仍依賴 `DemoJavaFxTarget` default runtime path 的 tests 移除或隔離。

Rollback strategy：
- 若 agent-side discovery 不穩，可暫時保留 embedded demo flow 在相容分支或暫時相容路徑上。
- 但不要重新把 silent fallback 變回預設行為；失敗必須維持明確。

## Open Questions

- `premain` 下第一個可行的 JavaFX discovery anchor 應該是什麼：`Application.start`、stage/window visibility change，還是 toolkit/window enumeration？
- 若未來有多個 JavaFX window，target selection 應只看 app name，還是也要支援 PID / window title？
- packaged runtime 要如何穩定找到 `-javaagent` 所需的 agent jar？
- 這次 change 要不要順手 scaffold `agentmain`，即使尚未完整接上？
