package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.nio.charset.StandardCharsets;

public final class ReflectiveJavaFxTarget implements AutomationTarget {
    private final String appName;
    private final Supplier<Object> sceneSupplier;

    public ReflectiveJavaFxTarget(String appName, Supplier<Object> sceneSupplier) {
        this.appName = Objects.requireNonNull(appName, "appName");
        this.sceneSupplier = Objects.requireNonNull(sceneSupplier, "sceneSupplier");
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
        return List.of("discover", "action", "screenshot", "javafx-bridge");
    }

    @Override
    public List<Map<String, Object>> discover() {
        return ReflectiveJavaFxSupport.onFxThread(this::discoverOnFxThread);
    }

    @Override
    public ActionResult perform(String action, JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> performOnFxThread(action, selector, payload));
    }

    @Override
    public byte[] screenshot() {
        return ReflectiveJavaFxSupport.onFxThread(this::screenshotOnFxThread);
    }

    private List<Map<String, Object>> discoverOnFxThread() {
        Object scene = sceneSupplier.get();
        if (scene == null) {
            return List.of();
        }

        Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
        if (root == null) {
            return List.of();
        }

        IdentityHashMap<Object, String> handles = new IdentityHashMap<>();
        AtomicInteger counter = new AtomicInteger();
        List<Map<String, Object>> result = new ArrayList<>();
        walk(root, "/Scene", handles, counter, result);
        return result;
    }

    private byte[] screenshotOnFxThread() {
        List<Map<String, Object>> nodes = discoverOnFxThread();
        StringBuilder builder = new StringBuilder();
        int y = 20;
        for (Map<String, Object> node : nodes) {
            Object text = node.get("text");
            if (text instanceof String value && !value.isBlank()) {
                builder.append(value).append("|0.99|10|").append(y).append("|160|24").append('\n');
                y += 28;
            }
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ActionResult performOnFxThread(String action, JsonObject selector, JsonObject payload) {
        DiscoverySnapshot snapshot = snapshot();
        Optional<NodeRef> match = resolve(selector, snapshot);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }

        NodeRef nodeRef = match.get();
        Object node = nodeRef.node();
        String fxId = asString(nodeRef.metadata().get("fxId"));
        String handle = asString(nodeRef.metadata().get("handle"));

        return switch (action) {
            case "click" -> handleClick(node, fxId, handle);
            case "select" -> handleSelect(node, fxId, handle, payload);
            case "type" -> handleType(node, fxId, handle, payload);
            case "get_text" -> ActionResult.success("javafx", handle, Map.of("fxId", fxId), ReflectiveJavaFxSupport.textOf(node));
            default -> ActionResult.failure(List.of("javafx"), Map.of("reason", "unsupported_action", "action", action));
        };
    }

    private ActionResult handleClick(Object node, String fxId, String handle) {
        try {
            ReflectiveJavaFxSupport.invoke(node, "fire");
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
        } catch (IllegalStateException ex) {
            try {
                ReflectiveJavaFxSupport.invoke(node, "requestFocus");
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
            } catch (IllegalStateException inner) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "click_not_supported", "fxId", fxId));
            }
        }
    }

    private ActionResult handleType(Object node, String fxId, String handle, JsonObject payload) {
        String input = payload != null && payload.has("input") ? payload.get("input").getAsString() : "";
        try {
            ReflectiveJavaFxSupport.invoke(node, "setText", input);
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), input);
        } catch (IllegalStateException ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "type_not_supported", "fxId", fxId));
        }
    }

    private ActionResult handleSelect(Object node, String fxId, String handle, JsonObject payload) {
        String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
        String column = payload != null && payload.has("column") ? payload.get("column").getAsString() : null;

        try {
            if (selectComboBoxItem(node, value)
                || selectListItem(node, value)
                || selectTreeItem(node, value)
                || selectTableRow(node, value, column)) {
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "value", value), value);
            }
        } catch (RuntimeException ex) {
            return ActionResult.failure(
                List.of("javafx"),
                Map.of(
                    "reason", "select_failed",
                    "fxId", fxId,
                    "value", value,
                    "error", ex.getClass().getSimpleName(),
                    "message", ex.getMessage() == null ? "" : ex.getMessage()
                )
            );
        }

        return ActionResult.failure(List.of("javafx"), Map.of("reason", "select_not_supported", "fxId", fxId, "value", value));
    }

    private boolean selectComboBoxItem(Object node, String value) {
        if (!"ComboBox".equals(node.getClass().getSimpleName())) {
            return false;
        }
        Integer index = findItemIndex(ReflectiveJavaFxSupport.invoke(node, "getItems"), value);
        if (index == null) {
            return false;
        }
        ReflectiveJavaFxSupport.invoke(node, "setValue", value);
        Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
        ReflectiveJavaFxSupport.invokeOnType(
            selectionModel,
            "javafx.scene.control.SingleSelectionModel",
            "select",
            new Class<?>[] {int.class},
            index
        );
        return true;
    }

    private boolean selectListItem(Object node, String value) {
        if (!"ListView".equals(node.getClass().getSimpleName())) {
            return false;
        }
        Integer index = findItemIndex(ReflectiveJavaFxSupport.invoke(node, "getItems"), value);
        if (index == null) {
            return false;
        }
        Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
        ReflectiveJavaFxSupport.invokeOnType(
            selectionModel,
            "javafx.scene.control.MultipleSelectionModel",
            "select",
            new Class<?>[] {int.class},
            index
        );
        return true;
    }

    private boolean selectTreeItem(Object node, String value) {
        if (!"TreeView".equals(node.getClass().getSimpleName())) {
            return false;
        }
        Object root = ReflectiveJavaFxSupport.invoke(node, "getRoot");
        Object match = findTreeItem(root, value);
        if (match == null) {
            return false;
        }
        try {
            ReflectiveJavaFxSupport.invoke(match, "setExpanded", true);
        } catch (IllegalStateException ex) {
            // Best effort.
        }
        Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
        ReflectiveJavaFxSupport.invokeOnType(
            selectionModel,
            "javafx.scene.control.MultipleSelectionModel",
            "select",
            new Class<?>[] {Object.class},
            match
        );
        return true;
    }

    private Integer findItemIndex(Object items, String value) {
        if (!(items instanceof List<?> list)) {
            return null;
        }
        for (int index = 0; index < list.size(); index++) {
            Object item = list.get(index);
            if (value.equals(item == null ? null : item.toString())) {
                return index;
            }
        }
        return null;
    }

    private Object findTreeItem(Object treeItem, String value) {
        if (treeItem == null) {
            return null;
        }
        Object itemValue = ReflectiveJavaFxSupport.invoke(treeItem, "getValue");
        if (value.equals(itemValue == null ? null : itemValue.toString())) {
            return treeItem;
        }
        Object children = ReflectiveJavaFxSupport.invoke(treeItem, "getChildren");
        if (!(children instanceof Collection<?> collection)) {
            return null;
        }
        for (Object child : collection) {
            Object match = findTreeItem(child, value);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private boolean selectTableRow(Object node, String value, String column) {
        if (!"TableView".equals(node.getClass().getSimpleName())) {
            return false;
        }
        Object items = ReflectiveJavaFxSupport.invoke(node, "getItems");
        if (!(items instanceof Collection<?> rows)) {
            return false;
        }
        Object match = null;
        for (Object row : rows) {
            if (rowMatches(row, value, column)) {
                match = row;
                break;
            }
        }
        if (match == null) {
            return false;
        }
        Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
        Integer index = findRowIndex(rows, match);
        if (index != null) {
            ReflectiveJavaFxSupport.invokeOnType(
                selectionModel,
                "javafx.scene.control.TableSelectionModel",
                "select",
                new Class<?>[] {int.class},
                index
            );
            return true;
        }
        ReflectiveJavaFxSupport.invokeOnType(
            selectionModel,
            "javafx.scene.control.TableSelectionModel",
            "select",
            new Class<?>[] {Object.class},
            match
        );
        return true;
    }

    private Integer findRowIndex(Collection<?> rows, Object match) {
        int index = 0;
        for (Object row : rows) {
            if (row == match) {
                return index;
            }
            index += 1;
        }
        return null;
    }

    private boolean rowMatches(Object row, String value, String column) {
        if (row == null) {
            return false;
        }
        if (column != null && !column.isBlank()) {
            String columnValue = extractRowValue(row, column);
            return value.equals(columnValue);
        }
        if (value.equals(row.toString())) {
            return true;
        }
        return value.equals(extractRowValue(row, "name"))
            || value.equals(extractRowValue(row, "role"))
            || value.equals(extractRowValue(row, "state"));
    }

    private String extractRowValue(Object row, String property) {
        try {
            Object result = ReflectiveJavaFxSupport.invoke(row, property);
            return result == null ? null : result.toString();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private Optional<NodeRef> resolve(JsonObject selector, DiscoverySnapshot snapshot) {
        if (selector == null) {
            return Optional.empty();
        }

        if (selector.has("id") && !selector.get("id").isJsonNull()) {
            String requestedId = selector.get("id").getAsString();
            return snapshot.nodes().stream()
                .filter(node -> requestedId.equals(node.metadata().get("fxId")))
                .findFirst();
        }

        if (selector.has("text") && !selector.get("text").isJsonNull()
            && selector.has("type") && !selector.get("type").isJsonNull()) {
            String requestedText = selector.get("text").getAsString();
            String requestedType = selector.get("type").getAsString();
            return snapshot.nodes().stream()
                .filter(node -> requestedType.equals(node.metadata().get("nodeType")))
                .filter(node -> requestedText.equals(node.metadata().get("text")))
                .findFirst();
        }

        return Optional.empty();
    }

    private DiscoverySnapshot snapshot() {
        Object scene = sceneSupplier.get();
        if (scene == null) {
            return new DiscoverySnapshot(List.of());
        }
        Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
        if (root == null) {
            return new DiscoverySnapshot(List.of());
        }

        IdentityHashMap<Object, String> handles = new IdentityHashMap<>();
        AtomicInteger counter = new AtomicInteger();
        List<NodeRef> nodes = new ArrayList<>();
        Deque<TraversalFrame> stack = new ArrayDeque<>();
        stack.push(new TraversalFrame(root, "/Scene"));
        while (!stack.isEmpty()) {
            TraversalFrame frame = stack.pop();
            String handle = handles.computeIfAbsent(frame.node(), ignored -> "node-" + counter.incrementAndGet());
            Map<String, Object> metadata = toNodeMap(frame.node(), frame.path(), handle);
            nodes.add(new NodeRef(frame.node(), metadata));
            List<Object> children = childrenOf(frame.node());
            for (int i = children.size() - 1; i >= 0; i--) {
                Object child = children.get(i);
                stack.push(new TraversalFrame(child, frame.path() + "/" + child.getClass().getSimpleName() + "[" + (i + 1) + "]"));
            }
        }
        return new DiscoverySnapshot(nodes);
    }

    private void walk(Object node, String path, IdentityHashMap<Object, String> handles, AtomicInteger counter, List<Map<String, Object>> result) {
        String handle = handles.computeIfAbsent(node, ignored -> "node-" + counter.incrementAndGet());
        result.add(toNodeMap(node, path, handle));
        List<Object> children = childrenOf(node);
        for (int index = 0; index < children.size(); index++) {
            Object child = children.get(index);
            walk(child, path + "/" + child.getClass().getSimpleName() + "[" + (index + 1) + "]", handles, counter, result);
        }
    }

    private List<Object> childrenOf(Object node) {
        try {
            Object children = ReflectiveJavaFxSupport.invoke(node, "getChildrenUnmodifiable");
            if (children instanceof List<?> list) {
                return new ArrayList<>(list.stream().map(Object.class::cast).toList());
            }
            return List.of();
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    private Map<String, Object> toNodeMap(Object node, String path, String handle) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("handle", handle);
        metadata.put("fxId", safeString(node, "getId"));
        metadata.put("nodeType", node.getClass().getSimpleName());
        metadata.put("text", ReflectiveJavaFxSupport.textOf(node));
        metadata.put("hierarchyPath", path);
        metadata.put("visible", safeBoolean(node, "isVisible", true));
        metadata.put("enabled", !safeBoolean(node, "isDisabled", false));
        return metadata;
    }

    private String safeString(Object target, String methodName) {
        try {
            Object value = ReflectiveJavaFxSupport.invoke(target, methodName);
            return value == null ? null : value.toString();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private boolean safeBoolean(Object target, String methodName, boolean defaultValue) {
        try {
            Object value = ReflectiveJavaFxSupport.invoke(target, methodName);
            return value instanceof Boolean actual ? actual : defaultValue;
        } catch (IllegalStateException ex) {
            return defaultValue;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private record TraversalFrame(Object node, String path) {
    }

    private record NodeRef(Object node, Map<String, Object> metadata) {
    }

    private record DiscoverySnapshot(List<NodeRef> nodes) {
    }
}
