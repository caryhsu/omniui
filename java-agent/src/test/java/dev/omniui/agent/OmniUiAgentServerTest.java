package dev.omniui.agent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import dev.omniui.agent.runtime.ActionResult;
import dev.omniui.agent.runtime.AutomationTarget;
import dev.omniui.agent.runtime.TargetRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmniUiAgentServerTest {
    private static final Gson GSON = new Gson();
    private static final String APP_NAME = "ServerTestApp";

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        TargetRegistry.unregister(APP_NAME);
        TargetRegistry.unregister("MissingApp");
    }

    @Test
    void healthEndpointReturnsOkPayload() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("GET", "/health", null);

        // THEN
        assertEquals(200, response.statusCode());
        Map<String, Object> body = parseBody(response);
        assertEquals("ok", asString(body.get("status")));
        assertEquals("0.1.0", asString(body.get("version")));
        assertEquals("http-json", asString(body.get("transport")));
    }

    @Test
    void healthEndpointRejectsWrongMethod() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/health", "{}");

        // THEN
        assertEquals(405, response.statusCode());
        assertEquals("Method not allowed", asString(parseBody(response).get("error")));
    }

    @Test
    void infoEndpointReturnsBoundPortAndVersion() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("GET", "/info", null);

        // THEN
        assertEquals(200, response.statusCode());
        Map<String, Object> body = parseBody(response);
        assertEquals("0.1.0", asString(body.get("version")));
        assertNotNull(body.get("appName"));
        assertEquals((double) server.getAddress().getPort(), body.get("port"));
    }

    @Test
    void infoEndpointRejectsWrongMethod() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/info", "{}");

        // THEN
        assertEquals(405, response.statusCode());
        assertEquals("Method not allowed", asString(parseBody(response).get("error")));
    }

    @Test
    void sessionsEndpointCreatesSessionForRegisteredTarget() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/sessions", "{\"target\":{\"appName\":\"ServerTestApp\"}}");

        // THEN
        assertEquals(200, response.statusCode());
        Map<String, Object> body = parseBody(response);
        assertTrue(asString(body.get("sessionId")).startsWith("session-"));
        assertEquals(APP_NAME, asString(body.get("appName")));
        assertEquals("javafx", asString(body.get("platform")));
        assertEquals(2, ((List<?>) body.get("capabilities")).size());
    }

    @Test
    void sessionsEndpointReturns404WhenTargetIsMissing() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/sessions", "{\"target\":{\"appName\":\"MissingApp\"}}");

        // THEN
        assertEquals(404, response.statusCode());
        assertTrue(asString(parseBody(response).get("error")).contains("Target app not available"));
    }

    @Test
    void sessionsEndpointReturns400ForMalformedJson() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/sessions", "{");

        // THEN
        assertEquals(400, response.statusCode());
        assertEquals("Malformed JSON", asString(parseBody(response).get("error")));
    }

    @Test
    void discoverEndpointReturnsNodesForSession() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("POST", "/sessions/" + sessionId + "/discover", "{}");

        // THEN
        assertEquals(200, response.statusCode());
        assertEquals(1, ((List<?>) parseBody(response).get("nodes")).size());
    }

    @Test
    void actionsEndpointReturnsActionResultPayload() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send(
            "POST",
            "/sessions/" + sessionId + "/actions",
            "{\"action\":\"click\",\"selector\":{\"id\":\"loginBtn\"},\"payload\":{\"input\":\"demo\"}}"
        );

        // THEN
        assertEquals(200, response.statusCode());
        Map<String, Object> body = parseBody(response);
        assertTrue((Boolean) body.get("ok"));
        assertEquals("clicked:click", asMap(body.get("value")).get("status"));
        assertEquals("loginBtn", asMap(asMap(body.get("resolved")).get("matchedAttributes")).get("selectorId"));
    }

    @Test
    void actionsEndpointReturns500WhenTargetThrows() throws Exception {
        // GIVEN
        TargetRegistry.register(new FailingAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send(
            "POST",
            "/sessions/" + sessionId + "/actions",
            "{\"action\":\"explode\",\"payload\":{}}"
        );

        // THEN
        assertEquals(500, response.statusCode());
        assertTrue(asString(parseBody(response).get("error")).contains("IllegalStateException"));
    }

    @Test
    void actionsEndpointReturns400ForMalformedJson() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("POST", "/sessions/" + sessionId + "/actions", "{");

        // THEN
        assertEquals(400, response.statusCode());
        assertEquals("Malformed JSON", asString(parseBody(response).get("error")));
    }

    @Test
    void actionsEndpointReturns400WhenActionIsMissing() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("POST", "/sessions/" + sessionId + "/actions", "{\"payload\":{}}");

        // THEN
        assertEquals(400, response.statusCode());
        assertEquals("Missing action", asString(parseBody(response).get("error")));
    }

    @Test
    void screenshotEndpointReturnsBase64Payload() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("POST", "/sessions/" + sessionId + "/screenshot", "{}");

        // THEN
        assertEquals(200, response.statusCode());
        Map<String, Object> body = parseBody(response);
        assertEquals("image/png", asString(body.get("contentType")));
        assertEquals("base64", asString(body.get("encoding")));
        assertEquals("cG5nLWRhdGE=", asString(body.get("data")));
    }

    @Test
    void recorderEndpointsReturnTargetRecorderData() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse startResponse = send("POST", "/sessions/" + sessionId + "/events/start", "{}");
        TestHttpResponse pendingResponse = send("GET", "/sessions/" + sessionId + "/events/pending", null);
        TestHttpResponse stopResponse = send("DELETE", "/sessions/" + sessionId + "/events", null);
        TestHttpResponse assertContextResponse = send(
            "GET",
            "/sessions/" + sessionId + "/events/assert-context?x=10.5&y=20.5",
            null
        );

        // THEN
        assertEquals(200, startResponse.statusCode());
        assertTrue((Boolean) parseBody(startResponse).get("ok"));

        assertEquals(200, pendingResponse.statusCode());
        assertEquals(1, ((List<?>) parseBody(pendingResponse).get("events")).size());

        assertEquals(200, stopResponse.statusCode());
        assertEquals(1, ((List<?>) parseBody(stopResponse).get("events")).size());

        assertEquals(200, assertContextResponse.statusCode());
        Map<String, Object> assertContext = parseBody(assertContextResponse);
        assertEquals("statusLabel", asString(assertContext.get("fxId")));
        assertEquals("Status: 10.5,20.5", asString(assertContext.get("currentText")));
    }

    @Test
    void assertContextEndpointReturns400ForMissingCoordinates() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("GET", "/sessions/" + sessionId + "/events/assert-context", null);

        // THEN
        assertEquals(400, response.statusCode());
        assertEquals("Missing coordinates", asString(parseBody(response).get("error")));
    }

    @Test
    void assertContextEndpointReturns400ForInvalidCoordinates() throws Exception {
        // GIVEN
        TargetRegistry.register(new ServerTestAutomationTarget(APP_NAME));
        startServer();
        String sessionId = createSession(APP_NAME);

        // WHEN
        TestHttpResponse response = send("GET", "/sessions/" + sessionId + "/events/assert-context?x=nope&y=20", null);

        // THEN
        assertEquals(400, response.statusCode());
        assertEquals("Invalid coordinates", asString(parseBody(response).get("error")));
    }

    @Test
    void sessionEndpointsReturn404ForUnknownSession() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("POST", "/sessions/missing/discover", "{}");

        // THEN
        assertEquals(404, response.statusCode());
        assertEquals("Unknown session", asString(parseBody(response).get("error")));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        // GIVEN
        startServer();

        // WHEN
        TestHttpResponse response = send("GET", "/sessions/missing/unknown", null);

        // THEN
        assertEquals(404, response.statusCode());
        assertEquals("Unknown session", asString(parseBody(response).get("error")));
    }

    private void startServer() throws IOException {
        server = OmniUiAgentServer.start(0);
    }

    private String createSession(String appName) throws Exception {
        TestHttpResponse response = send("POST", "/sessions", "{\"target\":{\"appName\":\"" + appName + "\"}}");
        assertEquals(200, response.statusCode());
        return asString(parseBody(response).get("sessionId"));
    }

    private TestHttpResponse send(String method, String path, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) uri(path).toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        }
        int statusCode = connection.getResponseCode();
        InputStream stream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String responseBody = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        connection.disconnect();
        return new TestHttpResponse(statusCode, responseBody);
    }

    private URI uri(String path) {
        int port = server.getAddress().getPort();
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private Map<String, Object> parseBody(TestHttpResponse response) {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = GSON.fromJson(response.body(), Map.class);
        assertNotNull(body);
        return body;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private record TestHttpResponse(int statusCode, String body) {
    }

    private static class ServerTestAutomationTarget implements AutomationTarget {
        private final String appName;

        private ServerTestAutomationTarget(String appName) {
            this.appName = appName;
        }

        @Override
        public String appName() {
            return appName;
        }

        @Override
        public String platform() {
            return "javafx";
        }

        @Override
        public List<String> capabilities() {
            return List.of("discover", "action");
        }

        @Override
        public List<Map<String, Object>> discover() {
            return List.of(Map.of(
                "handle", "node-1",
                "fxId", "loginBtn",
                "nodeType", "Button",
                "text", "Login"
            ));
        }

        public ActionResult perform(String action, com.google.gson.JsonObject selector, com.google.gson.JsonObject payload) {
            String selectorId = selector != null && selector.has("id") ? selector.get("id").getAsString() : "";
            return ActionResult.success(
                "javafx",
                "node-1",
                Map.of("selectorId", selectorId),
                Map.of("status", "clicked:" + action)
            );
        }

        @Override
        public byte[] screenshot() {
            return "png-data".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ActionResult startRecording() {
            return ActionResult.success("javafx", "recorder", Map.of(), null);
        }

        @Override
        public List<Map<String, Object>> stopRecordingFlush() {
            return List.of(Map.of("type", "click", "fxId", "loginBtn"));
        }

        @Override
        public List<Map<String, Object>> pollEvents() {
            return List.of(Map.of("type", "click", "fxId", "loginBtn"));
        }

        @Override
        public Map<String, Object> assertContext(double sceneX, double sceneY) {
            return Map.of(
                "fxId", "statusLabel",
                "nodeType", "Label",
                "currentText", "Status: " + sceneX + "," + sceneY
            );
        }
    }

    private static final class FailingAutomationTarget implements AutomationTarget {
        private final String appName;

        private FailingAutomationTarget(String appName) {
            this.appName = appName;
        }

        @Override
        public String appName() {
            return appName;
        }

        @Override
        public String platform() {
            return "javafx";
        }

        @Override
        public List<String> capabilities() {
            return List.of("discover", "action");
        }

        @Override
        public List<Map<String, Object>> discover() {
            return List.of();
        }

        public ActionResult perform(String action, com.google.gson.JsonObject selector, com.google.gson.JsonObject payload) {
            throw new IllegalStateException("kaboom");
        }

        @Override
        public byte[] screenshot() {
            return new byte[0];
        }
    }
}
