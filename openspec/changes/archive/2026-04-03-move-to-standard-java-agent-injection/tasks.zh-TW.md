## 1. Java agent bootstrap 與 runtime ownership

- [x] 1.1 在 `java-agent` 增加標準 Java agent entrypoints，支援 startup-time injection（`premain`），必要時可一併 scaffold 未來 `agentmain`
- [x] 1.2 重構 agent server startup，讓 HTTP transport lifecycle 由 Java agent bootstrap 擁有，而不是 target app code
- [x] 1.3 移除正常 agent-enabled 執行路徑對 built-in demo target fallback 的依賴

## 2. JavaFX runtime discovery without app cooperation

- [x] 2.1 針對目前支援的 demo 範圍實作 agent-side JavaFX runtime discovery
- [x] 2.2 從 agent 內部將 discovered JavaFX stages/scenes 註冊為 automation target
- [x] 2.3 在 target 由 injected agent 發現後，維持 scene graph enumeration 與 direct JavaFX actions 可正常使用

## 3. Demo app 去侵入化與 launch modes

- [x] 3.1 從 `demo/javafx-login-app` 移除直接的 OmniUI bootstrap classes 與 registration calls
- [x] 3.2 更新開發期啟動方式，讓 agent-enabled mode 使用 JVM injection，plain mode 則不帶 OmniUI control
- [x] 3.3 更新 packaged runtime wrapper scripts，讓 `with-agent` 透過 `-javaagent` 啟動，`plain` 則不注入 agent

## 4. Python client 與 demo flow 對齊

- [x] 4.1 調整 Python demo scripts 與 connection handling，讓它們假設 agent availability 取決於 launch mode，而非 app-embedded startup
- [x] 4.2 確保對 plain mode 的連線失敗是明確可見的，而不是默默連到 fallback target
- [x] 4.3 更新 demo 文件，說明 plain 與 agent-enabled 兩種啟動模式及對應的 Python control flow

## 5. 驗證與回歸覆蓋

- [x] 5.1 新增或更新 Java / Python tests，覆蓋 startup-time agent injection、plain-mode isolation 與 target resolution failure
- [x] 5.2 驗證在 target app source 不含 OmniUI bootstrap code 的前提下，agent-enabled login flow 仍可 end-to-end 跑通
- [x] 5.3 重新驗證 packaged runtime 與 demo scripts，確認 operator workflow 在新整合模型下仍可使用
