## 1. GUI — Run All button

- [x] 1.1 在 `RecorderApp.__init__` 加入 `_run_thread: Optional[threading.Thread]` 和 `_status_var: tk.StringVar` 欄位
- [x] 1.2 在工具列加入 **Run All** 按鈕（`ttk.Button`），綁定 `_run_all()`
- [x] 1.3 實作 `_run_all()`：取 script preview 全文，啟動背景 thread 執行 `_exec_script(code)`
- [x] 1.4 實作 `_exec_script(code: str)`：`exec(code, {"client": self.client})`；成功設 status `✅ Passed`，失敗設 `❌ Failed: <exc>`；結束後透過 `root.after(0, ...)` 重新啟用按鈕

## 2. GUI — Run Selection button

- [x] 2.1 在工具列加入 **Run Selection** 按鈕（`ttk.Button`），綁定 `_run_selection()`
- [x] 2.2 實作 `_run_selection()`：用 `widget.get(tk.SEL_FIRST, tk.SEL_LAST)` 取選取文字；若無選取則 status 設 `❌ No text selected` 並 return；否則啟動背景 thread 執行 `_exec_script(code)`

## 3. GUI — Status bar

- [x] 3.1 在 script preview 下方加入 `tk.Label`，textvariable 綁 `_status_var`，依狀態套色（idle=gray / running=orange / passed=green / failed=red）
- [x] 3.2 `_start_recording()` 與 `_stop_recording()` 呼叫時重設 status 為空字串

## 4. GUI — Button state management

- [x] 4.1 執行中停用 Run All、Run Selection（`state=tk.DISABLED`）
- [x] 4.2 執行完成（pass 或 fail）後重新啟用兩個按鈕（透過 `root.after(0, ...)`）

## 5. Tests

- [x] 5.1 在 `tests/test_recorder.py` 新增 `RecorderRunTests`：mock `_exec_script`，驗證 Run All 取全文並呼叫；Run Selection 取選取文字並呼叫；無選取時不呼叫並顯示警告訊息
- [x] 5.2 執行 `pytest tests/ -q` 確認全部通過

## 6. Commit & PR

- [x] 6.1 `git checkout -b feat/recorder-run`
- [x] 6.2 Commit：`feat: add Run All / Run Selection to Recorder GUI`
- [x] 6.3 Push + open PR → merge to main
- [x] 6.4 更新 ROADMAP — 標記 `Record & Run` 為 ✅
