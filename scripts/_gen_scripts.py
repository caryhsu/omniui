import os

M2 = r"%USERPROFILE%\.m2\repository"
JFX = "26"
GSON = "2.11.0"
AGENT = r"java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"

def jfx_jar(artifact):
    return rf"{M2}\org\openjfx\{artifact}\{JFX}\{artifact}-{JFX}-win.jar"

def std_mp(app_dir, extra_jfx=()):
    parts = [
        rf"demo\java\{app_dir}\target\classes",
        AGENT,
        jfx_jar("javafx-controls"),
        jfx_jar("javafx-graphics"),
        jfx_jar("javafx-base"),
        rf"{M2}\com\google\code\gson\gson\{GSON}\gson-{GSON}.jar",
    ]
    for j in extra_jfx:
        parts.append(jfx_jar(j))
    return ";".join(parts)

APPS = [
    ("core-app",         "dev.omniui.demo.core",         "dev.omniui.demo.core.CoreDemoApp",                 "48100", ()),
    ("input-app",        "dev.omniui.demo.input",        "dev.omniui.demo.input.InputDemoApp",               "48101", ()),
    ("advanced-app",     "dev.omniui.demo.advanced",     "dev.omniui.demo.advanced.AdvancedDemoApp",         "48102", ()),
    ("drag-app",         "dev.omniui.demo.drag",         "dev.omniui.demo.drag.DragDemoApp",                 "48103", ()),
    ("progress-app",     "dev.omniui.demo.progress",     "dev.omniui.demo.progress.ProgressDemoApp",         "48104", ()),
    ("image-app",        "dev.omniui.demo.image",        "dev.omniui.demo.image.ImageDemoApp",               "48105", ()),
    ("color-app",        "dev.omniui.demo.color",        "dev.omniui.demo.color.ColorDemoApp",               "48106", ()),
    ("todo-app",         "dev.omniui.demo.todo",         "dev.omniui.demo.todo.TodoDemoApp",                 "48107", ()),
    ("login-app",        "dev.omniui.demo.login",        "dev.omniui.demo.login.LoginApp",                   "48108", ()),
    ("user-search-app",  "dev.omniui.demo.usersearch",   "dev.omniui.demo.usersearch.UserSearchApp",         "48109", ()),
    ("dynamic-fxml-app", "dev.omniui.demo.dynamicfxml",  "dev.omniui.demo.dynamicfxml.DynamicFxmlApp",       "48110", ("javafx-fxml",)),
    ("explorer-app",     "dev.omniui.demo.explorer",     "dev.omniui.demo.explorer.ExplorerApp",             "48111", ()),
    ("settings-app",     "dev.omniui.demo.settings",     "dev.omniui.demo.settings.SettingsApp",             "48112", ()),
]

def write(path, content):
    with open(path, "w", newline="\r\n") as f:
        f.write(content)
    print("created:", path)

for app_dir, module, main, port, extra in APPS:
    base = rf"demo\java\{app_dir}"

    # build.bat
    write(rf"{base}\build.bat",
        "@echo off\r\nsetlocal\r\n"
        "set ROOT=%~dp0\\..\\..\\..\r\n"
        "pushd \"%ROOT%\"\r\n"
        "echo [build] Building agent...\r\n"
        "call mvn -pl java-agent -am package -q\r\n"
        "if errorlevel 1 goto :fail\r\n"
        f"echo [build] Building {app_dir}...\r\n"
        f"call mvn -f demo\\java\\{app_dir}\\pom.xml package -q\r\n"
        "if errorlevel 1 goto :fail\r\n"
        "echo [build] Done.\r\n"
        "popd\r\nexit /b 0\r\n"
        ":fail\r\necho [build] FAILED.\r\npopd\r\nexit /b 1\r\n"
    )

    # run-agent.bat
    mp = std_mp(app_dir, extra)
    write(rf"{base}\run-agent.bat",
        "@echo off\r\nsetlocal\r\n"
        "set ROOT=%~dp0\\..\\..\\..\r\n"
        "pushd \"%ROOT%\"\r\n"
        f"java -javaagent:{AGENT}=port={port} ^\r\n"
        f"  -p {mp} ^\r\n"
        f"  -m {module}/{main} %*\r\n"
        "popd\r\nexit /b 0\r\n"
    )

