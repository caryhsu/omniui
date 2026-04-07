package dev.omniui.agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.omniui.agent.runtime.ActionResult;
import dev.omniui.agent.runtime.AgentSession;
import dev.omniui.agent.runtime.SessionStore;
import dev.omniui.agent.runtime.TargetRegistry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class OmniUiAgentServer {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    public static final int DEFAULT_PORT = 48100;

    private OmniUiAgentServer() {
    }

    public static void main(String[] args) throws IOException {
        OmniUiJavaAgent.startServer(args.length > 0 ? String.join(",", args) : null);
    }

    public static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        SessionStore sessionStore = new SessionStore();

        server.createContext("/health", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                writeError(exchange, 405, "Method not allowed");
                return;
            }

            writeJson(exchange, 200, Map.of(
                "status", "ok",
                "version", "0.1.0",
                "transport", "http-json"
            ));
        });

        server.createContext("/info", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                writeError(exchange, 405, "Method not allowed");
                return;
            }

            String mainClass = Thread.getAllStackTraces().keySet().stream()
                    .filter(t -> "main".equals(t.getName()))
                    .findFirst()
                    .map(t -> {
                        StackTraceElement[] stack = t.getStackTrace();
                        return stack.length > 0 ? stack[stack.length - 1].getClassName() : "unknown";
                    })
                    .orElse("unknown");
            String simpleAppName = mainClass.contains(".")
                    ? mainClass.substring(mainClass.lastIndexOf('.') + 1)
                    : mainClass;

            // Best-effort: get the primary JavaFX stage title (500 ms timeout)
            String windowTitle = null;
            try {
                // Try multiple classloaders to find javafx.application.Platform
                Class<?> platformClass = null;
                Class<?> windowClass   = null;
                for (ClassLoader cl : new ClassLoader[]{
                        Thread.currentThread().getContextClassLoader(),
                        OmniUiAgentServer.class.getClassLoader(),
                        ClassLoader.getSystemClassLoader()}) {
                    if (cl == null) continue;
                    try {
                        platformClass = cl.loadClass("javafx.application.Platform");
                        windowClass   = cl.loadClass("javafx.stage.Window");
                        break;
                    } catch (ClassNotFoundException ignored) {}
                }
                if (platformClass != null) {
                    AtomicReference<String> ref = new AtomicReference<>();
                    CountDownLatch latch = new CountDownLatch(1);
                    final Class<?> wc = windowClass;
                    platformClass.getMethod("runLater", Runnable.class).invoke(null, (Runnable) () -> {
                        try {
                            Object wins = wc.getMethod("getWindows").invoke(null);
                            for (Object w : (java.util.List<?>) wins) {
                                if (!"Stage".equals(w.getClass().getSimpleName())) continue;
                                Object t = w.getClass().getMethod("getTitle").invoke(w);
                                if (t != null && !t.toString().isBlank()) { ref.set(t.toString()); break; }
                            }
                        } catch (Exception ignored) {}
                        latch.countDown();
                    });
                    latch.await(500, TimeUnit.MILLISECONDS);
                    windowTitle = ref.get();
                }
            } catch (Exception ignored) {}

            Map<String, Object> info = new HashMap<>();
            info.put("port",    port);
            info.put("appName", simpleAppName);
            info.put("version", "0.1.0");
            if (windowTitle != null && !windowTitle.isBlank()) {
                info.put("windowTitle", windowTitle);
            }
            writeJson(exchange, 200, info);
        });

        server.createContext("/sessions", new SessionsHandler(sessionStore));
        server.createContext("/sessions/", new SessionActionHandler(sessionStore));
        server.setExecutor(Executors.newCachedThreadPool(new DaemonThreadFactory()));
        server.start();
        System.out.println("OmniUI agent listening on http://127.0.0.1:" + port);
        return server;
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "omniui-http-" + count.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }

    private static final class SessionsHandler implements HttpHandler {
        private final SessionStore sessionStore;

        private SessionsHandler(SessionStore sessionStore) {
            this.sessionStore = sessionStore;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                writeError(exchange, 405, "Method not allowed");
                return;
            }

            JsonObject request = GSON.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject target = request != null && request.has("target") ? request.getAsJsonObject("target") : new JsonObject();
            String appName = target.has("appName") ? target.get("appName").getAsString() : "LoginDemo";

            var targetInstance = TargetRegistry.resolve(appName);
            if (targetInstance == null) {
                writeError(exchange, 404, "Target app not available. Launch the JavaFX app with the OmniUI Java agent enabled.");
                return;
            }

            AgentSession session = sessionStore.createSession(targetInstance);
            writeJson(exchange, 200, Map.of(
                "sessionId", session.sessionId(),
                "appName", session.target().appName(),
                "platform", session.target().platform(),
                "capabilities", session.target().capabilities()
            ));
        }
    }

    private static final class SessionActionHandler implements HttpHandler {
        private final SessionStore sessionStore;

        private SessionActionHandler(SessionStore sessionStore) {
            this.sessionStore = sessionStore;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                writeError(exchange, 404, "Unknown path");
                return;
            }

            AgentSession session = sessionStore.get(parts[2]);
            if (session == null) {
                writeError(exchange, 404, "Unknown session");
                return;
            }

            if (parts.length == 4 && "discover".equals(parts[3]) && "POST".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 200, Map.of("nodes", session.target().discover()));
                return;
            }

            if (parts.length == 4 && "actions".equals(parts[3]) && "POST".equals(exchange.getRequestMethod())) {
                try {
                    JsonObject request = GSON.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    String action = request.get("action").getAsString();
                    JsonElement selectorEl = request.get("selector");
                    JsonObject selector = (selectorEl != null && selectorEl.isJsonObject()) ? selectorEl.getAsJsonObject() : null;
                    JsonObject payload = request.has("payload") && request.get("payload").isJsonObject()
                        ? request.getAsJsonObject("payload") : new JsonObject();
                    ActionResult result = session.target().perform(action, selector, payload);
                    writeJson(exchange, 200, result);
                } catch (Exception ex) {
                    writeError(exchange, 500, ex.getClass().getSimpleName() + ": " + ex.getMessage());
                }
                return;
            }

            if (parts.length == 4 && "screenshot".equals(parts[3]) && "POST".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 200, Map.of(
                    "contentType", "image/png",
                    "encoding", "base64",
                    "data", Base64.getEncoder().encodeToString(session.target().screenshot())
                ));
                return;
            }

            // ── Recorder endpoints ─────────────────────────────────────────
            if (parts.length == 5 && "events".equals(parts[3]) && "start".equals(parts[4])
                    && "POST".equals(exchange.getRequestMethod())) {
                ActionResult result = session.target().startRecording();
                writeJson(exchange, 200, Map.of("ok", result.ok()));
                return;
            }

            if (parts.length == 4 && "events".equals(parts[3])
                    && "DELETE".equals(exchange.getRequestMethod())) {
                List<Map<String, Object>> events = session.target().stopRecordingFlush();
                writeJson(exchange, 200, Map.of("ok", true, "events", events));
                return;
            }

            if (parts.length == 5 && "events".equals(parts[3]) && "pending".equals(parts[4])
                    && "GET".equals(exchange.getRequestMethod())) {
                List<Map<String, Object>> events = session.target().pollEvents();
                writeJson(exchange, 200, Map.of("ok", true, "events", events));
                return;
            }

            if (parts.length == 5 && "events".equals(parts[3]) && "assert-context".equals(parts[4])
                    && "GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                double x = 0, y = 0;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            try {
                                if ("x".equals(kv[0])) x = Double.parseDouble(kv[1]);
                                if ("y".equals(kv[0])) y = Double.parseDouble(kv[1]);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                Map<String, Object> ctx = session.target().assertContext(x, y);
                if (ctx == null) {
                    writeError(exchange, 404, "No node at given coordinates");
                } else {
                    writeJson(exchange, 200, ctx);
                }
                return;
            }

            writeError(exchange, 404, "Unknown path");
        }
    }

    private static void writeJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] response = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private static void writeError(HttpExchange exchange, int statusCode, String message) throws IOException {
        writeJson(exchange, statusCode, Map.of("error", Objects.requireNonNull(message)));
    }
}
