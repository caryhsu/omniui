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
        startJvmExitMonitor();
    }

    /**
     * Monitors the JavaFX Application Thread. When it exits (user closes window,
     * Platform.exit(), etc.) the JDK HttpServer's internal Dispatcher thread —
     * which is always non-daemon — would keep the JVM alive indefinitely.
     * This monitor calls System.exit(0) as soon as JavaFX stops, ensuring a
     * clean process exit regardless of how the application was closed.
     */
    private static void startJvmExitMonitor() {
        Thread monitor = new Thread(() -> {
            // Wait for the JavaFX Application Thread to appear (app may not have
            // fully initialised yet when the agent premain runs).
            while (!isFxThreadAlive()) {
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
            // Now wait for it to finish.
            while (isFxThreadAlive()) {
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
            // JavaFX has exited — shut the JVM down cleanly.
            System.exit(0);
        }, "omniui-exit-monitor");
        monitor.setDaemon(true);
        monitor.start();
    }

    private static boolean isFxThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(t -> "JavaFX Application Thread".equals(t.getName()) && t.isAlive());
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
