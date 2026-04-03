from __future__ import annotations

import json
import sys
from pathlib import Path
from statistics import mean
from time import perf_counter

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from omniui import OmniUI
from demo.python._runtime import WITH_AGENT_HINT


def _measure(func, iterations: int = 5) -> list[float]:
    durations: list[float] = []
    for _ in range(iterations):
        started = perf_counter()
        func()
        durations.append((perf_counter() - started) * 1000.0)
    return durations


def run_benchmark() -> dict:
    client = OmniUI.connect(app_name="LoginDemo")
    node_query = _measure(client.get_nodes)
    screenshot = client.screenshot()
    ocr_fallback = _measure(lambda: client.ocr(screenshot))

    return {
        "javafx_node_query_ms": {
            "samples": node_query,
            "average": mean(node_query),
            "target": 200.0,
            "pass": mean(node_query) < 200.0,
        },
        "ocr_fallback_ms": {
            "samples": ocr_fallback,
            "average": mean(ocr_fallback),
            "target": 1000.0,
            "pass": mean(ocr_fallback) < 1000.0,
        },
    }


def main() -> None:
    try:
        result = run_benchmark()
    except Exception as exc:
        raise SystemExit(
            f"{WITH_AGENT_HINT}\n\nBenchmark also requires the OmniUI agent endpoint on "
            "http://127.0.0.1:48100"
        ) from exc
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
