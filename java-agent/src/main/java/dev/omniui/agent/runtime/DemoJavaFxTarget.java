package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

public final class DemoJavaFxTarget implements AutomationTarget {
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final Map<String, String> textValues = new LinkedHashMap<>();

    public DemoJavaFxTarget() {
        nodes.add(node("node-username", "username", "TextField", "/Scene/VBox/TextField[1]", "", true, true));
        nodes.add(node("node-password", "password", "PasswordField", "/Scene/VBox/PasswordField[1]", "", true, true));
        nodes.add(node("node-login", "loginButton", "Button", "/Scene/VBox/Button[1]", "Login", true, true));
        nodes.add(node("node-status", "status", "Label", "/Scene/VBox/Label[1]", "Idle", true, true));

        textValues.put("username", "");
        textValues.put("password", "");
        textValues.put("status", "Idle");
    }

    @Override
    public String appName() {
        return "LoginDemo";
    }

    @Override
    public String platform() {
        return "javafx";
    }

    @Override
    public List<String> capabilities() {
        return List.of("discover", "action", "screenshot");
    }

    @Override
    public List<Map<String, Object>> discover() {
        syncNodeText("username");
        syncNodeText("password");
        syncNodeText("status");
        return nodes;
    }

    @Override
    public ActionResult perform(String action, JsonObject selector, JsonObject payload) {
        Optional<Map<String, Object>> match = resolve(selector);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }

        Map<String, Object> node = match.get();
        String fxId = (String) node.get("fxId");

        return switch (action) {
            case "click" -> handleClick(node, fxId);
            case "type" -> handleType(node, fxId, payload);
            case "get_text" -> ActionResult.success("javafx", (String) node.get("handle"), Map.of("fxId", fxId), textValues.getOrDefault(fxId, Objects.toString(node.get("text"), "")));
            default -> ActionResult.failure(List.of("javafx"), Map.of("reason", "unsupported_action", "action", action));
        };
    }

    @Override
    public byte[] screenshot() {
        StringBuilder builder = new StringBuilder();
        int y = 20;
        for (Map<String, Object> node : discover()) {
            Object text = node.get("text");
            if (text instanceof String value && !value.isBlank()) {
                builder.append(value).append("|0.99|10|").append(y).append("|120|24").append('\n');
                y += 28;
            }
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ActionResult handleClick(Map<String, Object> node, String fxId) {
        if ("loginButton".equals(fxId)) {
            String status = "admin".equals(textValues.get("username")) && "1234".equals(textValues.get("password"))
                ? "Success"
                : "Failed";
            textValues.put("status", status);
            syncNodeText("status");
        }

        return ActionResult.success("javafx", (String) node.get("handle"), Map.of("fxId", fxId), null);
    }

    private ActionResult handleType(Map<String, Object> node, String fxId, JsonObject payload) {
        String input = payload.has("input") ? payload.get("input").getAsString() : "";
        textValues.put(fxId, input);
        syncNodeText(fxId);
        return ActionResult.success("javafx", (String) node.get("handle"), Map.of("fxId", fxId), input);
    }

    private Optional<Map<String, Object>> resolve(JsonObject selector) {
        if (selector != null && selector.has("id") && !selector.get("id").isJsonNull()) {
            String requestedId = selector.get("id").getAsString();
            return nodes.stream().filter(node -> requestedId.equals(node.get("fxId"))).findFirst();
        }

        if (selector != null
            && selector.has("text") && !selector.get("text").isJsonNull()
            && selector.has("type") && !selector.get("type").isJsonNull()) {
            String requestedText = selector.get("text").getAsString();
            String requestedType = selector.get("type").getAsString();
            return nodes.stream()
                .filter(node -> requestedType.equals(node.get("nodeType")) && requestedText.equals(node.get("text")))
                .findFirst();
        }

        return Optional.empty();
    }

    private Map<String, Object> node(String handle, String fxId, String nodeType, String hierarchyPath, String text, boolean visible, boolean enabled) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("handle", handle);
        node.put("fxId", fxId);
        node.put("nodeType", nodeType);
        node.put("text", text);
        node.put("hierarchyPath", hierarchyPath);
        node.put("visible", visible);
        node.put("enabled", enabled);
        return node;
    }

    private void syncNodeText(String fxId) {
        nodes.stream()
            .filter(node -> fxId.equals(node.get("fxId")))
            .findFirst()
            .ifPresent(node -> node.put("text", textValues.get(fxId)));
    }
}
