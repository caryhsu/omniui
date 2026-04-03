module dev.omniui.agent {
    requires com.google.gson;
    requires jdk.httpserver;
    requires java.instrument;

    exports dev.omniui.agent;
    exports dev.omniui.agent.runtime;
}
