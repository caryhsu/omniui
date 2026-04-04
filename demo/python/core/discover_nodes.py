from __future__ import annotations

import json

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()
    nodes = client.get_nodes()
    print(json.dumps(nodes, indent=2))


if __name__ == "__main__":
    main()
