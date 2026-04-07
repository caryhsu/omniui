## 1. Java Agent — polling endpoint

- [x] 1.1 在 `ReflectiveJavaFxTarget.java` 加入 `deliveredUpTo` AtomicInteger 欄位（初始值 0）
- [x] 1.2 實作 `pollEvents()` 方法：回傳 `recorderBuffer[deliveredUpTo:]`，並推進 `deliveredUpTo`
- [x] 1.3 在 `startRecording()` 和 `stopRecordingFlush()` 中重設 `deliveredUpTo` 為 0
- [x] 1.4 在 `OmniUiAgentServer.java` 加入 `GET /sessions/{sessionId}/events/pending` handler，呼叫 `session.target().pollEvents()`
- [x] 1.5 在 `AutomationTarget.java` 介面加入 `pollEvents()` 方法宣告

## 2. Build & 驗證

- [x] 2.1 Build agent JAR：`mvn clean package -pl java-agent -am -q`

## 3. Python client — poll_events

- [x] 3.1 在 `omniui/core/engine.py` 的 `OmniUI` 類別加入 `poll_events() -> list[dict]` 方法，呼叫 `GET /events/pending`，未錄製時拋 `RuntimeError`

## 4. Recorder GUI — real-time polling

- [x] 4.1 在 `RecorderApp` 加入 `_poll_thread: Optional[threading.Thread]` 和 `_polling: bool` 欄位
- [x] 4.2 實作 `_polling_loop()`：每 500ms 呼叫 `poll_events()`，將新 events 轉成 partial script lines，透過 `root.after(0, ...)` append 到 `_script_text` widget
- [x] 4.3 在 `_start_recording()` 中啟動 polling thread
- [x] 4.4 在 `_stop_recording()` 中先停止 polling thread，再呼叫 `stop_recording()`，最後用完整腳本取代預覽內容（確保一致性）

## 5. Unit tests

- [x] 5.1 在 `tests/test_recorder.py` 新增 `PollEventsTests`：mock `GET /events/pending`，驗證 `poll_events()` 回傳增量事件、未錄製時拋 `RuntimeError`
- [x] 5.2 執行 `pytest tests/ -q` 確認全部通過

## 6. Commit & PR

- [x] 6.1 `git checkout -b feat/recorder-realtime-feedback`
- [x] 6.2 Commit：`feat: add real-time recording feedback via polling`
- [x] 6.3 Push + open PR → merge to main
- [x] 6.4 更新 ROADMAP — 標記 `Real-time recording feedback` 為 ✅
