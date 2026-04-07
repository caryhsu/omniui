"""OmniUI Recorder — tkinter GUI tool.

Launch with:
    python -m omniui.recorder
"""
from __future__ import annotations

import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import threading
import urllib.request
import urllib.error
import json
from typing import Optional

from omniui import OmniUI
from omniui.core.models import RecordedScript

_SCAN_PORTS = range(48100, 48200)
_SCAN_TIMEOUT = 0.3


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

        self._build_ui()
        self._refresh_agents()

    # ── UI construction ────────────────────────────────────────────────────

    def _build_ui(self) -> None:
        root = self.root
        root.columnconfigure(0, weight=1)
        root.rowconfigure(2, weight=1)

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

        self._save_btn = tk.Button(ctrl, text="💾 Save", width=12,
                                   state="disabled", command=self._save_script)
        self._save_btn.pack(side="left", padx=(0, 12))

        self._run_all_btn = ttk.Button(ctrl, text="▶ Run All", width=12,
                                       command=self._run_all)
        self._run_all_btn.pack(side="left", padx=(0, 4))

        self._run_sel_btn = ttk.Button(ctrl, text="▶ Run Selection", width=14,
                                       command=self._run_selection)
        self._run_sel_btn.pack(side="left", padx=(0, 12))

        self._wait_injection_var = tk.BooleanVar(value=False)
        tk.Checkbutton(ctrl, text="Insert wait_for_*",
                       variable=self._wait_injection_var).pack(side="left")

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
                                    insertbackground="white")
        self._script_text.grid(row=0, column=0, sticky="nsew")

        vsb = ttk.Scrollbar(script_frame, orient="vertical",
                             command=self._script_text.yview)
        vsb.grid(row=0, column=1, sticky="ns")
        hsb = ttk.Scrollbar(script_frame, orient="horizontal",
                             command=self._script_text.xview)
        hsb.grid(row=1, column=0, sticky="ew")
        self._script_text.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)

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

        labels = [f"{a['port']} — {a.get('appName', 'unknown')}" for a in agents]
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
        self._run_all_btn.config(state="disabled")
        self._run_sel_btn.config(state="disabled")
        self._script_text.delete("1.0", "end")
        self._status_var.set("● Recording…")
        self._run_status_var.set("")

        self._polling = True
        self._poll_thread = threading.Thread(target=self._polling_loop, daemon=True)
        self._poll_thread.start()

    def _polling_loop(self) -> None:
        """Background thread: poll agent every 500 ms and append new script lines."""
        import time
        from ..recorder.selector_inference import infer_selector
        from ..recorder.script_gen import generate_script
        from ..core.models import RecordedEvent

        while self._polling:
            time.sleep(0.5)
            if not self._polling:
                break
            client = self._client
            if client is None:
                break
            try:
                raw_events = client.poll_events()
            except Exception:
                continue
            if not raw_events:
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
                for e in raw_events
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
        self._run_all_btn.config(state="normal")
        self._run_sel_btn.config(state="normal")

        n = len(self._script.events)
        self._status_var.set(f"Stopped — {n} event(s) captured")
        self._run_status_var.set("")

        # Replace incremental preview with the authoritative final script
        self._script_text.delete("1.0", "end")
        self._script_text.insert("1.0", self._script.script)

        if n > 0:
            self._save_btn.config(state="normal")

    # ── Run All / Run Selection ────────────────────────────────────────────

    def _set_run_status(self, text: str, color: str = "gray") -> None:
        self._run_status_var.set(text)
        self._run_status_lbl.config(fg=color)

    def _set_run_buttons(self, enabled: bool) -> None:
        state = "normal" if enabled else "disabled"
        self._run_all_btn.config(state=state)
        self._run_sel_btn.config(state=state)

    def _exec_script(self, code: str) -> None:
        """Execute *code* in a background thread with client in scope.

        A progress proxy wraps the client so that every action updates the
        status bar with the action name.  ``OmniUI.connect()`` inside the
        generated script returns the proxy so the script sees the same object.
        """
        self.root.after(0, self._set_run_status, "Running…", "orange")
        self.root.after(0, self._set_run_buttons, False)

        existing_client = self._client  # capture reference before thread starts

        try:
            replay_delay = float(self._replay_delay_var.get())
        except ValueError:
            replay_delay = 0.30

        # Strip the auto-generated header so that "from omniui import OmniUI"
        # never overwrites the _OmniUIStubWithProxy injected into exec globals,
        # and "client = OmniUI.connect(...)" is skipped (proxy is already bound).
        _header_patterns = (
            r"^\s*#\s*Generated by OmniUI Recorder[^\n]*\n",
            r"^\s*from\s+omniui\s+import\s+OmniUI[^\n]*\n",
            r"^\s*client\s*=\s*OmniUI\.connect\([^)]*\)\s*\n",
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

    def _save_script(self) -> None:
        if self._script is None:
            return
        path = filedialog.asksaveasfilename(
            defaultextension=".py",
            filetypes=[("Python files", "*.py"), ("All files", "*.*")],
            title="Save recorded script",
        )
        if not path:
            return
        try:
            self._script.save(path)
            self._status_var.set(f"Saved → {path}")
        except Exception as exc:
            messagebox.showerror("Save error", str(exc))


def main() -> None:
    root = tk.Tk()
    RecorderApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()
