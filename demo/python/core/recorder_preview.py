from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit
from omniui.recorder_lite import RecorderLite


def main() -> None:
    client = connect_or_exit()
    recorder = RecorderLite()

    client.click(id="regionField")
    client.click(id="auditEnabled")

    print("Recorder output:")
    for line in recorder.generate_script(client.action_history()):
        print(line)


if __name__ == "__main__":
    main()
