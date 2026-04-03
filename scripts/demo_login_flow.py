from demo.python._runtime import connect_or_exit
from omniui.recorder_lite import RecorderLite


def main() -> None:
    client = connect_or_exit()
    recorder = RecorderLite()

    client.click(id="username")
    client.type("admin", id="username")

    client.click(id="password")
    client.type("1234", id="password")

    client.click(text="Login")

    result = client.verify_text(id="status", expected="Success")
    if not result.ok:
        raise SystemExit(f"Login flow failed: {result.value}")

    print("Login flow succeeded")
    print("Trace history:")
    for entry in client.action_history():
        print(
            f"- {entry.action}: tier={entry.result.trace.resolved_tier}, "
            f"attempted={entry.result.trace.attempted_tiers}"
        )
    script_lines = recorder.generate_script(client.action_history())
    print("Recorder output:")
    for line in script_lines:
        print(f"- {line}")


if __name__ == "__main__":
    main()
