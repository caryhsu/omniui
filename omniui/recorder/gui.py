"""OmniUI Recorder — tkinter GUI tool.

Launch with:
    python -m omniui.recorder
"""
from __future__ import annotations

import atexit
import os
import signal
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog, ttk
import threading
import urllib.request
import urllib.error
import json
from typing import Optional

from omniui import OmniUI
from omniui.core.models import RecordedScript

_SCAN_PORTS = range(48100, 48200)
_SCAN_TIMEOUT = 0.3


def _activate_window(title: str) -> bool:
    """Best-effort: bring the window whose title contains *title* to the foreground.

    Returns True if the window was found and activated, False otherwise.
    Works without any third-party packages:
    - Windows: ctypes (win32 API)
    - macOS: osascript (AppleScript)
    - Linux: wmctrl (if installed)
    """
    import sys
    if sys.platform == "win32":
        try:
            import ctypes
            import ctypes.wintypes
            found: list = []
            WNDENUMPROC = ctypes.WINFUNCTYPE(
                ctypes.wintypes.BOOL,
                ctypes.wintypes.HWND,
                ctypes.wintypes.LPARAM,
            )

            def _cb(hwnd, _):
                length = ctypes.windll.user32.GetWindowTextLengthW(hwnd)
                if length == 0:
                    return True
                buf = ctypes.create_unicode_buffer(length + 1)
                ctypes.windll.user32.GetWindowTextW(hwnd, buf, length + 1)
                if title.lower() in buf.value.lower():
                    found.append(hwnd)
                return True

            ctypes.windll.user32.EnumWindows(WNDENUMPROC(_cb), 0)
            if found:
                hwnd = found[0]
                ctypes.windll.user32.ShowWindow(hwnd, 9)  # SW_RESTORE
                ctypes.windll.user32.SetForegroundWindow(hwnd)
                return True
        except Exception:
            pass
    elif sys.platform == "darwin":
        try:
            import subprocess
            script = (
                'tell application "System Events"\n'
                '  set procs to every process whose '
                f'(exists (windows whose name contains "{title}"))\n'
                '  if procs is not {} then\n'
                '    set frontmost of item 1 of procs to true\n'
                '  end if\n'
                'end tell'
            )
            subprocess.run(["osascript", "-e", script],
                           capture_output=True, timeout=2)
            return True
        except Exception:
            pass
    else:  # Linux
        try:
            import subprocess
            subprocess.run(["wmctrl", "-a", title],
                           capture_output=True, timeout=2)
            return True
        except Exception:
            pass
    return False


def _probe_port(port: int) -> Optional[dict]:
    """Return info dict if an OmniUI agent is listening on *port*, else None."""
    try:
        url = f"http://127.0.0.1:{port}/health"
        with urllib.request.urlopen(url, timeout=_SCAN_TIMEOUT) as resp:
            data = json.loads(resp.read())
            if data.get("status") != "ok":
                return None
    except Exception:
        return None

    try:
        url = f"http://127.0.0.1:{port}/info"
        with urllib.request.urlopen(url, timeout=_SCAN_TIMEOUT) as resp:
            return json.loads(resp.read())
    except Exception:
        return {"port": port, "appName": f"port {port}"}


def _scan_agents() -> list[dict]:
    """Scan localhost ports in parallel and return discovered agents."""
    from concurrent.futures import ThreadPoolExecutor
    with ThreadPoolExecutor(max_workers=20) as ex:
        results = list(ex.map(_probe_port, _SCAN_PORTS))
    return sorted(
        (r for r in results if r is not None),
        key=lambda r: r["port"],
    )


class RecorderApp:
    def __init__(self, root: tk.Tk) -> None:
        self.root = root
        self.root.title("OmniUI Recorder")
        self.root.resizable(True, True)

        self._client: Optional[OmniUI] = None
        self._script: Optional[RecordedScript] = None
        self._agents: list[dict] = []
        self._poll_thread: Optional[threading.Thread] = None
        self._polling: bool = False
        self._run_thread: Optional[threading.Thread] = None
        self._status_var: tk.StringVar  # initialised in _build_ui

        # File & dirty-tracking state
        self._current_file: Optional[str] = None
        self._dirty: bool = False

        self._build_ui()
        self._refresh_agents()
        self.root.protocol("WM_DELETE_WINDOW", self._on_close)

    # ── UI construction ────────────────────────────────────────────────────

    def _build_ui(self) -> None:
        root = self.root
        root.columnconfigure(0, weight=1)
        root.rowconfigure(2, weight=1)

        # ── Menubar ────────────────────────────────────────────────────────
        menubar = tk.Menu(root)
        filemenu = tk.Menu(menubar, tearoff=0)
        filemenu.add_command(label="Open…",    accelerator="Ctrl+O",       command=self._open_file)
        filemenu.add_command(label="Save",     accelerator="Ctrl+S",       command=self._save_script)
        filemenu.add_command(label="Save As…", accelerator="Ctrl+Shift+S", command=self._save_as_script)
        filemenu.add_separator()
        filemenu.add_command(label="Exit", command=self._on_close)
        menubar.add_cascade(label="File", menu=filemenu)

        editmenu = tk.Menu(menubar, tearoff=0)
        editmenu.add_command(label="Undo",
                             accelerator="Ctrl+Z",
                             command=lambda: self._script_text.edit_undo())
        editmenu.add_command(label="Redo",
                             accelerator="Ctrl+Y",
                             command=lambda: self._script_text.edit_redo())
        editmenu.add_separator()
        editmenu.add_command(label="Insert Screenshot",
                             accelerator="Ctrl+Shift+P",
                             command=self._insert_screenshot)
        editmenu.add_command(label="Insert Wait…",
                             accelerator="Ctrl+Shift+W",
                             command=self._insert_wait)
        menubar.add_cascade(label="Edit", menu=editmenu)

        self._runmenu = tk.Menu(menubar, tearoff=0)
        self._runmenu.add_command(label="Record",
                                  accelerator="F7",
                                  command=self._start_recording)
        self._runmenu.add_command(label="Stop",
                                  accelerator="F8",
                                  state="disabled",
                                  command=self._stop_recording)
        self._runmenu.add_separator()
        self._runmenu.add_command(label="Run All",
                                  accelerator="F5",
                                  command=self._run_all)
        self._runmenu.add_command(label="Run Selection",
                                  accelerator="F6",
                                  command=self._run_selection)
        menubar.add_cascade(label="Run", menu=self._runmenu)

        self._theme_var = tk.StringVar(value="dark")
        viewmenu = tk.Menu(menubar, tearoff=0)
        viewmenu.add_command(label="Font Size…",       command=self._change_font_size)
        viewmenu.add_command(label="Increase Font Size", accelerator="Ctrl+=",
                             command=self._increase_font_size)
        viewmenu.add_command(label="Decrease Font Size", accelerator="Ctrl+-",
                             command=self._decrease_font_size)
        viewmenu.add_separator()
        viewmenu.add_radiobutton(label="Dark Theme",  variable=self._theme_var,
                                 value="dark",  command=self._apply_theme)
        viewmenu.add_radiobutton(label="Light Theme", variable=self._theme_var,
                                 value="light", command=self._apply_theme)
        menubar.add_cascade(label="View", menu=viewmenu)

        root.config(menu=menubar)
        root.bind_all("<Control-o>", lambda e: self._open_file())
        root.bind_all("<Control-s>", lambda e: self._save_script())
        root.bind_all("<Control-S>", lambda e: self._save_as_script())
        root.bind_all("<Control-P>", lambda e: self._insert_screenshot())
        root.bind_all("<Control-W>", lambda e: self._insert_wait())
        root.bind_all("<Control-z>", lambda e: self._script_text.edit_undo())
        root.bind_all("<Control-y>", lambda e: self._script_text.edit_redo())
        root.bind_all("<Control-equal>", lambda e: self._increase_font_size())
        root.bind_all("<Control-minus>",  lambda e: self._decrease_font_size())
        root.bind_all("<F5>", lambda e: self._run_all())
        root.bind_all("<F6>", lambda e: self._run_selection())
        root.bind_all("<F7>", lambda e: self._start_recording())
        root.bind_all("<F8>", lambda e: self._stop_recording())

        self._font_size = 10

        # ── Row 0: App selector ────────────────────────────────────────────
        top = tk.Frame(root, padx=8, pady=6)
        top.grid(row=0, column=0, sticky="ew")
        top.columnconfigure(1, weight=1)

        tk.Label(top, text="App:").grid(row=0, column=0, sticky="w")
        self._app_var = tk.StringVar()
        self._app_combo = ttk.Combobox(top, textvariable=self._app_var,
                                       state="readonly", width=40)
        self._app_combo.grid(row=0, column=1, sticky="ew", padx=(6, 4))
        self._refresh_btn = tk.Button(top, text="⟳ Refresh", command=self._refresh_agents)
        self._refresh_btn.grid(row=0, column=2)

        # ── Row 1: Control buttons ─────────────────────────────────────────
        ctrl = tk.Frame(root, padx=8, pady=4)
        ctrl.grid(row=1, column=0, sticky="ew")

        self._record_btn = tk.Button(ctrl, text="⏺ Record", width=12,
                                     bg="#d9534f", fg="white",
                                     command=self._start_recording)
        self._record_btn.pack(side="left", padx=(0, 6))

        self._stop_btn = tk.Button(ctrl, text="⏹ Stop", width=12,
                                   state="disabled", command=self._stop_recording)
        self._stop_btn.pack(side="left", padx=(0, 6))

        # Open / Save buttons removed from toolbar — use File menu instead.
        # _open_btn and _save_btn kept as hidden widgets so state-tracking calls work.
        self._open_btn = tk.Button(ctrl, text="📂 Open", width=10,
                                   command=self._open_file)

        self._save_btn = tk.Button(ctrl, text="💾 Save", width=10,
                                   state="disabled", command=self._save_script)

        self._run_all_btn = ttk.Button(ctrl, text="▶ Run All", width=12,
                                       state="disabled", command=self._run_all)
        self._run_all_btn.pack(side="left", padx=(0, 4))

        self._run_sel_btn = ttk.Button(ctrl, text="▶ Run Selection", width=14,
                                       state="disabled", command=self._run_selection)
        self._run_sel_btn.pack(side="left", padx=(0, 12))

        self._wait_injection_var = tk.BooleanVar(value=False)
        tk.Checkbutton(ctrl, text="Insert wait_for_*",
                       variable=self._wait_injection_var).pack(side="left")

        self._activate_window_var = tk.BooleanVar(value=True)
        tk.Checkbutton(ctrl, text="Activate app window",
                       variable=self._activate_window_var).pack(side="left", padx=(8, 0))

        tk.Label(ctrl, text="  Delay:").pack(side="left")
        self._replay_delay_var = tk.StringVar(value="0.30")
        ttk.Spinbox(ctrl, textvariable=self._replay_delay_var,
                    from_=0.0, to=5.0, increment=0.1, width=5,
                    format="%.2f").pack(side="left")
        tk.Label(ctrl, text="s").pack(side="left")

        # ── Row 1 right: status label ──────────────────────────────────────
        self._status_var = tk.StringVar(value="Ready")
        tk.Label(ctrl, textvariable=self._status_var, anchor="e").pack(
            side="right", padx=8)

        # ── Row 2: Script output ───────────────────────────────────────────
        script_frame = tk.Frame(root, padx=8, pady=4)
        script_frame.grid(row=2, column=0, sticky="nsew")
        script_frame.columnconfigure(0, weight=1)
        script_frame.rowconfigure(0, weight=1)

        self._script_text = tk.Text(script_frame, wrap="none",
                                    font=("Courier New", 10),
                                    bg="#1e1e1e", fg="#d4d4d4",
                                    insertbackground="white",
                                    undo=True)
        self._script_text.grid(row=0, column=0, sticky="nsew")

        vsb = ttk.Scrollbar(script_frame, orient="vertical",
                             command=self._script_text.yview)
        vsb.grid(row=0, column=1, sticky="ns")
        hsb = ttk.Scrollbar(script_frame, orient="horizontal",
                             command=self._script_text.xview)
        hsb.grid(row=1, column=0, sticky="ew")
        self._script_text.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)

        # Track modifications for dirty flag and dynamic button states
        self._script_text.bind("<<Modified>>",  self._on_text_modified)
        self._script_text.bind("<<Selection>>", lambda e: self._update_run_buttons())

        # ── Row 3: Run status bar ──────────────────────────────────────────
        self._run_status_var = tk.StringVar(value="")
        self._run_status_lbl = tk.Label(root, textvariable=self._run_status_var,
                                        anchor="w", padx=10, fg="gray")
        self._run_status_lbl.grid(row=3, column=0, sticky="ew")

        root.geometry("700x500")

    # ── Agent discovery ────────────────────────────────────────────────────

    def _refresh_agents(self) -> None:
        self._status_var.set("Scanning…")
        self._refresh_btn.config(state="disabled")

        def scan():
            agents = _scan_agents()
            self.root.after(0, self._on_agents_found, agents)

        threading.Thread(target=scan, daemon=True).start()

    def _on_agents_found(self, agents: list[dict]) -> None:
        self._agents = agents
        self._refresh_btn.config(state="normal")

        if not agents:
            self._app_combo["values"] = []
            self._app_var.set("")
            self._status_var.set("No OmniUI apps found")
            return

        labels = [
            f"{a['port']} — {a.get('windowTitle') or a.get('appName', 'unknown')}"
            for a in agents
        ]
        self._app_combo["values"] = labels
        self._app_combo.current(0)
        self._status_var.set(f"{len(agents)} app(s) found")

    def _selected_agent(self) -> Optional[dict]:
        idx = self._app_combo.current()
        if idx < 0 or idx >= len(self._agents):
            return None
        return self._agents[idx]

    # ── Recording control ──────────────────────────────────────────────────

    def _start_recording(self) -> None:
        agent = self._selected_agent()
        if agent is None:
            messagebox.showwarning("No app selected",
                                   "Please select an app from the dropdown first.")
            return

        # Always activate the target app window before recording starts
        win_title = agent.get("windowTitle") or agent.get("appName", "")
        if win_title:
            _activate_window(win_title)

        try:
            self._client = OmniUI.connect(port=agent["port"])
            self._client.start_recording(
                wait_injection=self._wait_injection_var.get()
            )
        except Exception as exc:
            messagebox.showerror("Error", f"Failed to start recording:\n{exc}")
            self._client = None
            return

        self._record_btn.config(state="disabled")
        self._stop_btn.config(state="normal")
        self._save_btn.config(state="disabled")
        self._script_text.delete("1.0", "end")
        self._dirty = False
        self._script_text.edit_modified(False)
        self._status_var.set("● Recording…")
        self._run_status_var.set("")
        self._update_run_buttons()

        self._polling = True
        self._poll_thread = threading.Thread(target=self._polling_loop, daemon=True)
        self._poll_thread.start()

    def _polling_loop(self) -> None:
        """Background thread: poll agent every 500 ms and append new script lines."""
        import time
        from ..recorder.selector_inference import infer_selector
        from ..recorder.script_gen import generate_script
        from ..core.models import RecordedEvent

        consecutive_errors = 0
        _MAX_ERRORS = 3

        while self._polling:
            time.sleep(0.5)
            if not self._polling:
                break
            client = self._client
            if client is None:
                break
            try:
                raw_events = client.poll_events()
                consecutive_errors = 0
            except Exception:
                consecutive_errors += 1
                if consecutive_errors >= _MAX_ERRORS:
                    self.root.after(0, self._on_remote_app_closed)
                    return
                continue
            if not raw_events:
                continue

            right_click_events = [e for e in raw_events if e.get("type") == "right_click"]
            action_events_raw = [e for e in raw_events if e.get("type") != "right_click"]

            # Handle right-click events → show assertion menu on main thread (node info embedded in event)
            for rc in right_click_events:
                available = rc.get("availableAssertions") or []
                if available:
                    self.root.after(0, self._show_assertion_menu_from_event, rc, available)

            if not action_events_raw:
                continue

            events = [
                RecordedEvent(
                    event_type=e.get("type", ""),
                    fx_id=e.get("fxId", ""),
                    text=e.get("text", ""),
                    node_type=e.get("nodeType", ""),
                    node_index=int(e.get("nodeIndex", 0)),
                    timestamp=float(e.get("timestamp", 0)),
                    to_fx_id=e.get("toFxId", ""),
                    to_text=e.get("toText", ""),
                    to_node_type=e.get("toNodeType", ""),
                    to_node_index=int(e.get("toNodeIndex", 0)),
                    color=e.get("color", ""),
                )
                for e in action_events_raw
            ]
            partial = generate_script(
                events,
                wait_injection=client._wait_injection,
                wait_timeout=client._wait_timeout,
                skip_header=True,
            )
            if partial.strip():
                self.root.after(0, self._append_script_lines, partial)

    def _append_script_lines(self, text: str) -> None:
        self._script_text.insert("end", text + "\n")
        self._script_text.see("end")
        self._dirty = True
        self._update_window_title()

    def _fetch_and_show_assertion_menu(self, scene_x: float, scene_y: float) -> None:
        """Called on main thread: fetch assert-context from agent and show menu."""
        client = self._client
        if client is None:
            return
        try:
            session_id = client.session_id
            url = f"{client.base_url}/sessions/{session_id}/events/assert-context?x={scene_x}&y={scene_y}"
            with urllib.request.urlopen(url, timeout=1.0) as resp:
                ctx = json.loads(resp.read())
        except Exception:
            return  # 靜默略過（node 不存在或連線失敗）

        available = ctx.get("availableAssertions", [])
        if not available:
            return

        # 取得目前螢幕滑鼠位置以彈出選單
        try:
            mx = self.root.winfo_pointerx()
            my = self.root.winfo_pointery()
        except Exception:
            mx, my = 0, 0

        self._show_assertion_menu(ctx, available, mx, my)

    def _show_assertion_menu_from_event(self, rc: dict, available: list) -> None:
        """Called on main thread with embedded node info from right_click event."""
        mx = self.root.winfo_pointerx()
        my = self.root.winfo_pointery()
        self._show_assertion_menu(rc, available, mx, my)

    def _show_assertion_menu(self, ctx: dict, available: list[str], screen_x: int, screen_y: int) -> None:
        """Popup assertion menu at (screen_x, screen_y)."""
        _LABELS = {
            "verify_text":    "驗證文字",
            "verify_visible": "驗證可見",
            "verify_enabled": "驗證啟用",
        }
        menu = tk.Menu(self.root, tearoff=0)
        for assertion_type in available:
            label = _LABELS.get(assertion_type, assertion_type)
            menu.add_command(
                label=label,
                command=lambda at=assertion_type: self._insert_assertion(ctx, at),
            )
        try:
            menu.tk_popup(screen_x, screen_y)
        finally:
            menu.grab_release()

    def _insert_assertion(self, ctx: dict, assertion_type: str) -> None:
        """Insert an assertion line into the script preview at the cursor position."""
        from omniui.core.models import RecordedEvent
        from ..recorder.script_gen import generate_script

        fx_id = ctx.get("fxId") or ""
        current_text = ctx.get("currentText") or ""
        expected = current_text if assertion_type == "verify_text" else ""

        event = RecordedEvent(
            event_type="assertion",
            fx_id=fx_id,
            text="",
            node_type=ctx.get("nodeType", ""),
            node_index=0,
            timestamp=0.0,
            assertion_type=assertion_type,
            expected=expected,
        )
        line = generate_script([event], skip_header=True).strip()
        if not line:
            return

        # 插入到游標所在行之後（若有）或末尾
        try:
            cursor = self._script_text.index(tk.INSERT)
            row, _ = map(int, cursor.split("."))
            insert_pos = f"{row}.end"
        except Exception:
            insert_pos = "end"

        self._script_text.insert(insert_pos, "\n" + line)
        self._script_text.see("end")
        self._dirty = True
        self._update_window_title()
        self._update_run_buttons()

    def _stop_recording(self) -> None:
        # Stop polling thread first
        self._polling = False
        if self._poll_thread is not None:
            self._poll_thread.join(timeout=1.0)
            self._poll_thread = None

        if self._client is None:
            return
        try:
            self._script = self._client.stop_recording()
        except Exception as exc:
            messagebox.showerror("Error", f"Failed to stop recording:\n{exc}")
            return
        # Keep self._client alive for Run All / Run Selection after recording

        self._record_btn.config(state="normal")
        self._stop_btn.config(state="disabled")

        n = len(self._script.events)
        self._status_var.set(f"Stopped — {n} event(s) captured")
        self._run_status_var.set("")

        # Replace incremental preview with the authoritative final script
        self._script_text.delete("1.0", "end")
        self._script_text.insert("1.0", self._script.script)
        self._script_text.edit_modified(False)

        if n > 0:
            self._dirty = True
            self._save_btn.config(state="normal")
        self._update_run_buttons()
        self._update_window_title()

    # ── Run All / Run Selection ────────────────────────────────────────────

    def _set_run_status(self, text: str, color: str = "gray") -> None:
        self._run_status_var.set(text)
        self._run_status_lbl.config(fg=color)

    def _set_run_buttons(self, enabled: bool) -> None:
        if enabled:
            self._update_run_buttons()
        else:
            self._run_all_btn.config(state="disabled")
            self._run_sel_btn.config(state="disabled")
            self._runmenu.entryconfig("Run All", state="disabled")
            self._runmenu.entryconfig("Run Selection", state="disabled")

    def _exec_script(self, code: str) -> None:
        """Execute *code* in a background thread with client in scope.

        A progress proxy wraps the client so that every action updates the
        status bar with the action name.  ``OmniUI.connect()`` inside the
        generated script returns the proxy so the script sees the same object.
        """
        self.root.after(0, self._set_run_status, "Running…", "orange")
        self.root.after(0, self._set_run_buttons, False)

        existing_client = self._client  # capture reference before thread starts

        # If no client yet, auto-connect to the selected app without recording
        if existing_client is None:
            agent = self._selected_agent()
            if agent is None:
                self.root.after(0, self._set_run_status,
                                "No app selected — choose an app from the dropdown first.", "red")
                self.root.after(0, self._set_run_buttons, True)
                return
            try:
                self._client = OmniUI.connect(port=agent["port"])
                existing_client = self._client
                self.root.after(0, self._status_var.set,
                                f"Connected to {agent.get('appName', agent['port'])}")
            except Exception as exc:
                self.root.after(0, self._set_run_status, f"Connect failed: {exc}", "red")
                self.root.after(0, self._set_run_buttons, True)
                return

        # Activate target app window before running (if option is enabled)
        if self._activate_window_var.get():
            agent = self._selected_agent()
            if agent:
                win_title = agent.get("windowTitle") or agent.get("appName", "")
                if win_title:
                    _activate_window(win_title)

        try:
            replay_delay = float(self._replay_delay_var.get())
        except ValueError:
            replay_delay = 0.30

        # Strip the auto-generated header so that "from omniui import OmniUI"
        # never overwrites the _OmniUIStubWithProxy injected into exec globals.
        # NOTE: "client = OmniUI.connect(...)" is NOT stripped so the stub's
        # connect() is called and any kwargs (e.g. screenshot_mode) are applied.
        _header_patterns = (
            r"^\s*#\s*Generated by OmniUI Recorder[^\n]*\n",
            r"^\s*from\s+omniui\s+import\s+OmniUI[^\n]*\n",
        )
        import re as _re
        for _pat in _header_patterns:
            code = _re.sub(_pat, "", code, flags=_re.MULTILINE)

        def run():
            prev_delay = existing_client.step_delay
            existing_client.step_delay = replay_delay

            # Wrap client to show per-action progress in the status bar
            set_status = lambda msg: self.root.after(0, self._set_run_status, msg, "orange")

            def _wait_for_dialog_button(text: str, timeout: float = 5.0) -> None:
                """Wait until a visible dialog button with ``text`` appears."""
                import time as _t
                deadline = _t.monotonic() + timeout
                while _t.monotonic() < deadline:
                    try:
                        nodes = existing_client.get_nodes()
                        if any(n.get("text") == text or n.get("label") == text for n in nodes):
                            return
                    except Exception:
                        return
                    _t.sleep(0.2)

            class _ProgressProxy:
                """Forwards all attribute access to the real client; logs each call.

                Before node-targeting actions (click/type/set_date) the proxy calls
                ``wait_for_node`` so the action doesn't fire before the UI is ready
                (e.g. before a dialog finishes opening), avoiding slow OCR/vision
                fallback triggered by transient ``selector_not_found`` errors.
                """
                def __getattr__(self, name):
                    orig = getattr(existing_client, name)
                    if not callable(orig):
                        return orig
                    def wrapper(*args, **kwargs):
                        label = f"Running: {name}({', '.join(str(a) for a in args)}{', '.join(f'{k}={v}' for k,v in kwargs.items())})"
                        set_status(label[:80])
                        node_id = kwargs.get("id")
                        if name in ("click", "type", "set_date", "clear", "get_text") and node_id:
                            try:
                                existing_client.wait_for_node(id=node_id, timeout=5.0)
                            except TimeoutError:
                                pass  # Let the action itself report a clear error
                        elif name == "dismiss_dialog":
                            btn_text = kwargs.get("button") or (args[0] if args else None)
                            if btn_text:
                                _wait_for_dialog_button(btn_text, timeout=5.0)
                        return orig(*args, **kwargs)
                    return wrapper

            proxy = _ProgressProxy()

            class _OmniUIStubWithProxy:
                @staticmethod
                def connect(*args, **kwargs):
                    # Apply screenshot settings from the script's connect() call
                    if "screenshot_mode" in kwargs:
                        existing_client.screenshot_mode = kwargs["screenshot_mode"]
                    if "screenshot_dir" in kwargs:
                        existing_client.screenshot_dir = kwargs["screenshot_dir"]
                    return proxy

            try:
                exec(code, {"client": proxy, "OmniUI": _OmniUIStubWithProxy})  # noqa: S102
                self.root.after(0, self._set_run_status, "✅ Passed", "green")
            except Exception as exc:
                msg = str(exc).split("\n")[0]
                self.root.after(0, self._set_run_status, f"❌ Failed: {msg}", "red")
            finally:
                existing_client.step_delay = prev_delay
                self.root.after(0, self._set_run_buttons, True)

        self._run_thread = threading.Thread(target=run, daemon=True)
        self._run_thread.start()

    def _run_all(self) -> None:
        code = self._script_text.get("1.0", "end-1c")
        if not code.strip():
            self._set_run_status("❌ No script to run", "red")
            return
        self._exec_script(code)

    def _run_selection(self) -> None:
        try:
            code = self._script_text.get(tk.SEL_FIRST, tk.SEL_LAST)
        except tk.TclError:
            self._set_run_status("❌ No text selected", "red")
            return
        if not code.strip():
            self._set_run_status("❌ No text selected", "red")
            return
        self._exec_script(code)

    def _save_script(self) -> bool:
        """Save to current file path; prompt via Save As if no path is set.

        Returns True if the file was saved, False if the user cancelled.
        """
        if self._current_file:
            return self._do_save(self._current_file)
        return self._save_as_script()

    def _save_as_script(self) -> bool:
        """Always prompt for a file path. Returns True if saved."""
        path = filedialog.asksaveasfilename(
            defaultextension=".py",
            filetypes=[("Python files", "*.py"), ("All files", "*.*")],
            title="Save script as",
            initialfile=os.path.basename(self._current_file) if self._current_file else "",
        )
        if not path:
            return False
        return self._do_save(path)

    def _do_save(self, path: str) -> bool:
        """Write text widget content to *path*. Returns True on success."""
        try:
            content = self._script_text.get("1.0", "end-1c")
            with open(path, "w", encoding="utf-8") as f:
                f.write(content)
            self._current_file = path
            self._dirty = False
            self._script_text.edit_modified(False)
            self._status_var.set(f"Saved → {path}")
            self._update_window_title()
            return True
        except Exception as exc:
            messagebox.showerror("Save error", str(exc))
            return False

    def _open_file(self) -> None:
        """Open a Python script file into the editor."""
        if self._dirty:
            answer = messagebox.askyesnocancel(
                "Unsaved changes",
                "The current script has unsaved changes.\nSave before opening?",
            )
            if answer is None:
                return
            if answer and not self._save_script():
                return

        path = filedialog.askopenfilename(
            filetypes=[("Python files", "*.py"), ("All files", "*.*")],
            title="Open script",
        )
        if not path:
            return
        try:
            with open(path, encoding="utf-8") as f:
                content = f.read()
        except Exception as exc:
            messagebox.showerror("Open error", str(exc))
            return

        self._script_text.delete("1.0", "end")
        self._script_text.insert("1.0", content)
        self._script_text.edit_modified(False)
        self._current_file = path
        self._dirty = False
        self._status_var.set(f"Opened: {path}")
        self._save_btn.config(state="normal")
        self._update_run_buttons()
        self._update_window_title()

    # ── Text modification callbacks ────────────────────────────────────────

    def _on_text_modified(self, event=None) -> None:
        """Called when the Text widget fires <<Modified>>."""
        if self._script_text.edit_modified():
            self._dirty = True
            self._script_text.edit_modified(False)
            has_text = bool(self._script_text.get("1.0", "end-1c").strip())
            self._save_btn.config(state="normal" if has_text else "disabled")
            self._update_window_title()
        self._update_run_buttons()

    def _update_run_buttons(self, event=None) -> None:
        """Enable/disable Run buttons based on recording state and content."""
        if self._polling:
            self._run_all_btn.config(state="disabled")
            self._run_sel_btn.config(state="disabled")
            self._runmenu.entryconfig("Run All", state="disabled")
            self._runmenu.entryconfig("Run Selection", state="disabled")
            return
        has_text = bool(self._script_text.get("1.0", "end-1c").strip())
        has_sel = False
        try:
            has_sel = bool(self._script_text.get(tk.SEL_FIRST, tk.SEL_LAST).strip())
        except tk.TclError:
            pass
        self._run_all_btn.config(state="normal" if has_text else "disabled")
        self._run_sel_btn.config(state="normal" if has_sel else "disabled")
        self._runmenu.entryconfig("Run All", state="normal" if has_text else "disabled")
        self._runmenu.entryconfig("Run Selection", state="normal" if has_sel else "disabled")

    def _update_window_title(self) -> None:
        """Reflect current file and dirty state in the window title."""
        if self._current_file:
            name = os.path.basename(self._current_file)
            prefix = "* " if self._dirty else ""
            self.root.title(f"{prefix}OmniUI Recorder — {name}")
        else:
            prefix = "* " if self._dirty else ""
            self.root.title(f"{prefix}OmniUI Recorder")

    # ── Remote app disconnection ───────────────────────────────────────────

    def _on_remote_app_closed(self) -> None:
        """Called when the recorded app stops responding (3 consecutive errors)."""
        self._polling = False
        self._record_btn.config(state="normal")
        self._stop_btn.config(state="disabled")
        self._status_var.set("⚠ App disconnected")
        self._update_run_buttons()
        messagebox.showwarning(
            "App disconnected",
            "The recorded app appears to have closed.\n"
            "Recording has been stopped automatically.\n"
            "The partial script is preserved in the editor.",
        )

    # ── Edit menu actions ─────────────────────────────────────────────────

    def _insert_screenshot(self) -> None:
        """Insert a client.save_screenshot() line at the current cursor position."""
        line = "client.save_screenshot()\n"
        try:
            insert_pos = self._script_text.index("insert")
            # Insert at start of current line so it lands cleanly between steps
            line_start = f"{insert_pos.split('.')[0]}.0"
            self._script_text.insert(line_start, line)
        except Exception:
            self._script_text.insert("end", line)
        self._dirty = True
        self._save_btn.config(state="normal")

    def _insert_wait(self) -> None:
        """Prompt for a duration and insert a time.sleep() line at the cursor."""
        seconds = simpledialog.askfloat(
            "Insert Wait",
            "Wait duration (seconds):",
            initialvalue=1.0,
            minvalue=0.1,
            maxvalue=60.0,
            parent=self.root,
        )
        if seconds is None:
            return  # cancelled
        # Format: drop trailing zero for whole numbers (1.0 → 1, 1.5 → 1.5)
        formatted = f"{seconds:g}"
        line = f"import time; time.sleep({formatted})\n"
        try:
            insert_pos = self._script_text.index("insert")
            line_start = f"{insert_pos.split('.')[0]}.0"
            self._script_text.insert(line_start, line)
        except Exception:
            self._script_text.insert("end", line)
        self._dirty = True
        self._save_btn.config(state="normal")

    def _change_font_size(self) -> None:
        """Prompt for a new font size and apply it to the script editor."""
        size = simpledialog.askinteger(
            "Font Size",
            "Editor font size (pt):",
            initialvalue=self._font_size,
            minvalue=6,
            maxvalue=36,
            parent=self.root,
        )
        if size is None:
            return
        self._font_size = size
        self._script_text.configure(font=("Courier New", size))

    def _increase_font_size(self) -> None:
        if self._font_size < 36:
            self._font_size += 1
            self._script_text.configure(font=("Courier New", self._font_size))

    def _decrease_font_size(self) -> None:
        if self._font_size > 6:
            self._font_size -= 1
            self._script_text.configure(font=("Courier New", self._font_size))

    _THEMES = {
        "dark":  {"bg": "#1e1e1e", "fg": "#d4d4d4", "insertbackground": "white",
                  "selectbackground": "#264f78", "selectforeground": "#d4d4d4"},
        "light": {"bg": "#ffffff", "fg": "#1e1e1e", "insertbackground": "black",
                  "selectbackground": "#add6ff", "selectforeground": "#1e1e1e"},
    }

    def _apply_theme(self) -> None:
        """Apply the selected theme to the script editor."""
        cfg = self._THEMES[self._theme_var.get()]
        self._script_text.configure(**cfg)

    # ── Window close ──────────────────────────────────────────────────────

    def _on_close(self) -> None:
        """Intercept window close; prompt when recording is active or there are unsaved changes."""
        if self._polling:
            answer = messagebox.askyesno(
                "Recording in progress",
                "Recording is still in progress. Stop recording and close?",
            )
            if not answer:
                return
            self._stop_recording()

        if self._dirty:
            answer = messagebox.askyesnocancel(
                "Unsaved changes",
                "The script has unsaved changes. Save before closing?",
            )
            if answer is None:      # Cancel
                return
            if answer:              # Yes — save
                if not self._save_script():
                    return          # User cancelled the Save As dialog
        self.root.destroy()


def main() -> None:
    root = tk.Tk()
    app = RecorderApp(root)

    # Best-effort cleanup on unexpected exit (Ctrl+C, atexit, SIGTERM).
    # Does not cover SIGKILL / force-kill — use Java watchdog (ROADMAP) for that.
    def _emergency_stop() -> None:
        if app._polling and app._client is not None:
            try:
                app._client.stop_recording()
            except Exception:
                pass

    atexit.register(_emergency_stop)

    def _signal_handler(sig, frame) -> None:
        _emergency_stop()
        raise SystemExit(0)

    signal.signal(signal.SIGINT,  _signal_handler)
    signal.signal(signal.SIGTERM, _signal_handler)

    root.mainloop()


if __name__ == "__main__":
    main()
