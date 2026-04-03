package dev.omniui.agent;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public final class OmniUiJavaAgent {
    private static volatile HttpServer server;

    private OmniUiJavaAgent() {
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        startServer(agentArgs);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        startServer(agentArgs);
    }

    public static synchronized void startServer(String agentArgs) {
        if (server != null) {
            return;
        }
        int port = parsePort(agentArgs);
        try {
            server = OmniUiAgentServer.start(port);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start OmniUI Java agent on port " + port, ex);
        }
    }

    public static synchronized void stopServer() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;
    }

    private static int parsePort(String agentArgs) {
        if (agentArgs == null || agentArgs.isBlank()) {
            return OmniUiAgentServer.DEFAULT_PORT;
        }
        for (String token : agentArgs.split(",")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("port=")) {
                return Integer.parseInt(trimmed.substring("port=".length()));
            }
        }
        return OmniUiAgentServer.DEFAULT_PORT;
    }
}
