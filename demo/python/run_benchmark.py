from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
else:
    from . import _bootstrap  # noqa: F401

from scripts.benchmark_phase1 import main


if __name__ == "__main__":
    main()
