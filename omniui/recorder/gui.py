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
        self._save_btn.pack(side="left")

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

        root.geometry("700x480")

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
            self._client = OmniUI(port=agent["port"])
            self._client.start_recording()
        except Exception as exc:
            messagebox.showerror("Error", f"Failed to start recording:\n{exc}")
            self._client = None
            return

        self._record_btn.config(state="disabled")
        self._stop_btn.config(state="normal")
        self._save_btn.config(state="disabled")
        self._script_text.delete("1.0", "end")
        self._status_var.set("● Recording…")

    def _stop_recording(self) -> None:
        if self._client is None:
            return
        try:
            self._script = self._client.stop_recording()
        except Exception as exc:
            messagebox.showerror("Error", f"Failed to stop recording:\n{exc}")
            return
        finally:
            self._client = None

        self._record_btn.config(state="normal")
        self._stop_btn.config(state="disabled")

        n = len(self._script.events)
        self._status_var.set(f"Stopped — {n} event(s) captured")

        self._script_text.delete("1.0", "end")
        self._script_text.insert("1.0", self._script.script)

        if n > 0:
            self._save_btn.config(state="normal")

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
