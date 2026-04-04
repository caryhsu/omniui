package dev.omniui.agent.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.nio.charset.StandardCharsets;

public final class ReflectiveJavaFxTarget implements AutomationTarget {
    private static final int OVERLAY_TIMEOUT_MS = 2_000;
    private static final int OVERLAY_POLL_INTERVAL_MS = 50;

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
        return switch (action) {
            case "right_click"     -> performWithOverlayWait(() -> doRightClick(selector),      this::isContextMenuWindow);
            case "open_menu"       -> performWithOverlayWait(() -> doOpenMenu(selector, payload), this::isMenuPopupWindow);
            case "open_datepicker" -> performWithOverlayWait(() -> doOpenDatePicker(selector),  this::isDatePickerWindow);
            case "click_menu_item" -> doClickMenuItem(payload);
            case "navigate_menu"   -> doNavigateMenu(selector, payload);
            case "dismiss_menu"    -> doDismissMenu();
            case "navigate_month"  -> doNavigateMonth(payload);
            case "pick_date"       -> doPickDate(payload);
            case "set_date"        -> doSetDate(selector, payload);
            case "open_colorpicker" -> performWithOverlayWait(() -> doOpenColorPicker(selector), this::isColorPickerWindow);
            case "set_color"        -> doSetColor(selector, payload);
            case "dismiss_colorpicker" -> doDismissColorPicker();
            case "get_dialog"      -> doGetDialog();
            case "dismiss_dialog"  -> doDismissDialog(payload);
            case "close_app"       -> doCloseApp();
            case "press_key"       -> doPressKey(selector, payload);
            default -> ReflectiveJavaFxSupport.onFxThread(() -> performOnFxThread(action, selector, payload));
        };
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
        walk(root, "/Scene", "primary", handles, counter, result);

        // Also walk overlay windows (popups, dialogs, context menus, etc.)
        List<Object> allWindows = getAllWindows();
        Object primaryWindow = safeInvoke(scene, "getWindow");
        for (Object window : allWindows) {
            if (window == primaryWindow) continue;
            Object overlayScene = safeInvoke(window, "getScene");
            if (overlayScene == null) continue;
            Object overlayRoot = safeInvoke(overlayScene, "getRoot");
            if (overlayRoot == null) continue;
            String windowType = classifyWindow(window);
            walk(overlayRoot, "/Overlay", windowType, handles, counter, result);
        }

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
        // scroll_by can target the first ScrollPane when no selector is given
        if (match.isEmpty() && !"scroll_by".equals(action)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }

        NodeRef nodeRef = match.orElse(null);
        Object node = nodeRef != null ? nodeRef.node() : null;
        String fxId = nodeRef != null ? asString(nodeRef.metadata().get("fxId")) : "";
        String handle = nodeRef != null ? asString(nodeRef.metadata().get("handle")) : "";

        return switch (action) {
            case "click"        -> handleClick(node, fxId, handle, payload);
            case "double_click" -> handleDoubleClick(node, fxId, handle);
            case "hover"        -> doHover(node, fxId, handle);
            case "select"       -> handleSelect(node, fxId, handle, payload);
            case "type"         -> handleType(node, fxId, handle, payload);
            case "get_text"     -> ActionResult.success("javafx", handle, Map.of("fxId", fxId), ReflectiveJavaFxSupport.textOf(node));
            case "get_tooltip"  -> {
                Object tooltip = safeInvoke(node, "getTooltip");
                String tipText = "";
                if (tooltip != null) {
                    Object txt = safeInvoke(tooltip, "getText");
                    if (txt != null) tipText = txt.toString();
                }
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), tipText);
            }
            case "get_style" -> {
                Object s = safeInvoke(node, "getStyle");
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), s == null ? "" : s.toString());
            }
            case "get_style_class" -> {
                Object sc = safeInvoke(node, "getStyleClass");
                java.util.List<String> classes = sc == null
                    ? java.util.List.of()
                    : new java.util.ArrayList<>((java.util.Collection<String>) sc);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), classes);
            }
            case "get_selected" -> { Object v = ReflectiveJavaFxSupport.invoke(node, "isSelected"); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), v); }
            case "set_selected" -> handleSetSelected(node, fxId, handle, payload);
            case "get_value"    -> { Object v = ReflectiveJavaFxSupport.invoke(node, "getValue"); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), v == null ? null : v.toString()); }
            case "set_slider"   -> handleSetSlider(node, fxId, handle, payload);
            case "set_spinner"  -> handleSetSpinner(node, fxId, handle, payload);
            case "step_spinner" -> handleStepSpinner(node, fxId, handle, payload);
            case "get_progress" -> { Object v = ReflectiveJavaFxSupport.invoke(node, "getProgress"); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), v); }
            case "get_tabs"     -> handleGetTabs(node, fxId, handle);
            case "select_tab"   -> handleSelectTab(node, fxId, handle, payload);
            case "get_visited"  -> { Object v = safeInvoke(node, "isVisited"); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), v); }
            case "expand_pane"  -> { safeInvoke(node, "setExpanded", true); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null); }
            case "collapse_pane" -> { safeInvoke(node, "setExpanded", false); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null); }
            case "get_expanded" -> { Object v = safeInvoke(node, "isExpanded"); yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), v); }
            case "select_tree_table_row" -> {
                String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
                String column = payload != null && payload.has("column") ? payload.get("column").getAsString() : null;
                boolean selected = selectTreeTableRow(node, value, column);
                yield selected
                    ? ActionResult.success("javafx", handle, Map.of("fxId", fxId, "value", value), value)
                    : ActionResult.failure(List.of("javafx"), Map.of("reason", "select_not_supported", "fxId", fxId, "value", value));
            }
            case "get_tree_table_cell" -> {
                String row  = payload != null && payload.has("row")    ? payload.get("row").getAsString()    : "";
                String col2 = payload != null && payload.has("column") ? payload.get("column").getAsString() : "";
                String cellValue = getTreeTableCell(node, row, col2);
                yield cellValue != null
                    ? ActionResult.success("javafx", handle, Map.of("fxId", fxId), cellValue)
                    : ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found", "fxId", fxId));
            }
            case "expand_tree_table_item" -> {
                String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
                Object root2 = ReflectiveJavaFxSupport.invoke(node, "getRoot");
                Object item = findTreeTableItemByCell(node, root2, value, null);
                if (item == null) yield ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found", "fxId", fxId));
                safeInvoke(item, "setExpanded", true);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
            }
            case "collapse_tree_table_item" -> {
                String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
                Object root2 = ReflectiveJavaFxSupport.invoke(node, "getRoot");
                Object item = findTreeTableItemByCell(node, root2, value, null);
                if (item == null) yield ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found", "fxId", fxId));
                safeInvoke(item, "setExpanded", false);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
            }
            case "get_tree_table_expanded" -> {
                String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
                Object root2 = ReflectiveJavaFxSupport.invoke(node, "getRoot");
                Object item = findTreeTableItemByCell(node, root2, value, null);
                if (item == null) yield ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found", "fxId", fxId));
                Object expanded = safeInvoke(item, "isExpanded");
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), expanded);
            }
            case "get_color" -> {
                Object colorVal = ReflectiveJavaFxSupport.invoke(node, "getValue");
                if (colorVal == null) yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
                double r = (double) ReflectiveJavaFxSupport.invokeExact(colorVal, "getRed", new Class<?>[0]);
                double g = (double) ReflectiveJavaFxSupport.invokeExact(colorVal, "getGreen", new Class<?>[0]);
                double b = (double) ReflectiveJavaFxSupport.invokeExact(colorVal, "getBlue", new Class<?>[0]);
                String hex = String.format("#%02x%02x%02x", (int)(r*255), (int)(g*255), (int)(b*255));
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), hex);
            }
            case "get_divider_positions" -> {
                Object positions = ReflectiveJavaFxSupport.invoke(node, "getDividerPositions");
                String serialized = positions == null ? "[]" : java.util.Arrays.toString((double[]) positions);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), serialized);
            }
            case "set_divider_position" -> {
                int divIndex = payload != null && payload.has("index") ? payload.get("index").getAsInt() : 0;
                double divPos = payload != null && payload.has("position") ? payload.get("position").getAsDouble() : 0.5;
                double[] positions2 = (double[]) ReflectiveJavaFxSupport.invoke(node, "getDividerPositions");
                if (positions2 == null || divIndex < 0 || divIndex >= positions2.length) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "invalid_divider_index", "fxId", fxId));
                }
                ReflectiveJavaFxSupport.invoke(node, "setDividerPosition", divIndex, divPos);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
            }
            case "set_visible" -> {
                boolean vis = payload == null || !payload.has("visible") || payload.get("visible").getAsBoolean();
                ReflectiveJavaFxSupport.invoke(node, "setVisible", vis);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), vis);
            }
            case "set_disabled" -> {
                boolean dis = payload != null && payload.has("disabled") && payload.get("disabled").getAsBoolean();
                ReflectiveJavaFxSupport.invoke(node, "setDisable", dis);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), dis);
            }
            case "scroll_to" -> {                Object sp = findScrollPaneAncestor(node);
                if (sp == null) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scroll_pane_ancestor", "fxId", fxId));
                }
                scrollToNode(sp, node);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
            }
            case "scroll_by" -> {
                double deltaX = payload != null && payload.has("delta_x") ? payload.get("delta_x").getAsDouble() : 0.0;
                double deltaY = payload != null && payload.has("delta_y") ? payload.get("delta_y").getAsDouble() : 0.0;
                Object sp = (node != null) ? node : findFirstScrollPane(snapshot);
                if (sp == null) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scroll_pane_found"));
                }
                scrollByDelta(sp, deltaX, deltaY);
                yield ActionResult.success("javafx", handle != null ? handle : "", Map.of("fxId", fxId != null ? fxId : ""), null);
            }
            case "select_multiple" -> {
                if (payload == null || !payload.has("values")) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_values_param", "fxId", fxId));
                }
                JsonArray valArr = payload.getAsJsonArray("values");
                List<String> reqValues = new ArrayList<>();
                for (JsonElement el : valArr) reqValues.add(el.getAsString());

                Object items = ReflectiveJavaFxSupport.invoke(node, "getItems");
                if (!(items instanceof List<?> itemList)) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "not_a_list_control", "fxId", fxId));
                }
                List<Integer> indices = new ArrayList<>();
                for (String val : reqValues) {
                    for (int i = 0; i < itemList.size(); i++) {
                        Object item = itemList.get(i);
                        if (val.equals(item == null ? null : item.toString())) {
                            indices.add(i);
                            break;
                        }
                    }
                }
                if (indices.isEmpty()) {
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "no_items_found", "fxId", fxId));
                }
                Object sm = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
                ReflectiveJavaFxSupport.invokeOnType(sm, "javafx.scene.control.MultipleSelectionModel", "clearSelection", new Class<?>[0]);
                int first = indices.get(0);
                int[] rest = new int[indices.size() - 1];
                for (int i = 0; i < rest.length; i++) rest[i] = indices.get(i + 1);
                ReflectiveJavaFxSupport.invokeOnType(sm, "javafx.scene.control.MultipleSelectionModel", "selectIndices", new Class<?>[]{int.class, int[].class}, first, rest);
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), indices.size());
            }
            case "get_selected_items" -> {
                Object sm = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
                Object selectedItems = ReflectiveJavaFxSupport.invokeOnType(
                    sm, "javafx.scene.control.MultipleSelectionModel", "getSelectedItems", new Class<?>[0]);
                List<String> selected = new ArrayList<>();
                if (selectedItems instanceof Collection<?> col) {
                    for (Object item : col) selected.add(item == null ? null : item.toString());
                }
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), selected);
            }
            default -> ActionResult.failure(List.of("javafx"), Map.of("reason", "unsupported_action", "action", action));
        };
    }

    private ActionResult handleClick(Object node, String fxId, String handle, JsonObject payload) {
        // Parse modifiers from payload (e.g. ["Ctrl", "Shift"])
        boolean shiftDown = false, controlDown = false, altDown = false, metaDown = false;
        boolean hasModifiers = false;
        if (payload != null && payload.has("modifiers") && payload.get("modifiers").isJsonArray()) {
            for (var el : payload.getAsJsonArray("modifiers")) {
                String[] parts = parseKeyString(el.getAsString());
                String mod = parts[0];
                switch (mod) {
                    case "SHIFT"   -> shiftDown   = true;
                    case "CONTROL" -> controlDown = true;
                    case "ALT"     -> altDown     = true;
                    case "META"    -> metaDown    = true;
                    default -> {
                        return ActionResult.failure(List.of("javafx"),
                            Map.of("reason", "unknown_modifier", "modifier", mod, "fxId", fxId));
                    }
                }
                hasModifiers = true;
            }
        }

        if (!hasModifiers) {
            // Original path: node.fire() for maximum compatibility
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

        // Modifier path: fire MouseEvent.MOUSE_CLICKED with modifier flags (mirrors handleDoubleClick)
        try {
            Object bounds = safeInvoke(node, "getBoundsInLocal");
            double localX = 5, localY = 5;
            if (bounds != null) {
                Object w = safeInvoke(bounds, "getWidth");
                Object h = safeInvoke(bounds, "getHeight");
                if (w instanceof Number nw && h instanceof Number nh) {
                    localX = nw.doubleValue() / 2;
                    localY = nh.doubleValue() / 2;
                }
            }
            Object screenPt = safeInvoke(node, "localToScreen", localX, localY);
            double screenX = 0, screenY = 0;
            if (screenPt != null) {
                Object sx = safeInvoke(screenPt, "getX");
                Object sy = safeInvoke(screenPt, "getY");
                if (sx instanceof Number nx) screenX = nx.doubleValue();
                if (sy instanceof Number ny) screenY = ny.doubleValue();
            }

            Class<?> mouseCls  = Class.forName("javafx.scene.input.MouseEvent");
            Class<?> btnCls    = Class.forName("javafx.scene.input.MouseButton");
            Class<?> pickCls   = Class.forName("javafx.scene.input.PickResult");
            Class<?> etCls     = Class.forName("javafx.event.EventType");

            Object mouseClicked = mouseCls.getField("MOUSE_CLICKED").get(null);
            Object primary      = btnCls.getField("PRIMARY").get(null);

            java.lang.reflect.Constructor<?> ctor = mouseCls.getConstructor(
                etCls,
                double.class, double.class, double.class, double.class,
                btnCls, int.class,
                boolean.class, boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                pickCls
            );

            Object event = ctor.newInstance(
                mouseClicked,
                localX, localY, screenX, screenY,
                primary, 1,
                shiftDown, controlDown, altDown, metaDown,
                false, false, false,
                true, false, true,
                (Object) null
            );

            Class<?> eventCls  = Class.forName("javafx.event.Event");
            Class<?> targetCls = Class.forName("javafx.event.EventTarget");
            java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);
            fireMethod.invoke(null, node, event);

            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of(
                "reason", "modifier_click_failed",
                "fxId", fxId,
                "message", ex.getMessage() == null ? "" : ex.getMessage()
            ));
        }
    }

    private ActionResult doHover(Object node, String fxId, String handle) {
        try {
            Object bounds = safeInvoke(node, "getBoundsInLocal");
            double localX = 5, localY = 5;
            if (bounds != null) {
                Object w = safeInvoke(bounds, "getWidth");
                Object h = safeInvoke(bounds, "getHeight");
                if (w instanceof Number nw && h instanceof Number nh) {
                    localX = nw.doubleValue() / 2;
                    localY = nh.doubleValue() / 2;
                }
            }
            Object screenPt = safeInvoke(node, "localToScreen", localX, localY);
            double screenX = 0, screenY = 0;
            if (screenPt != null) {
                Object sx = safeInvoke(screenPt, "getX");
                Object sy = safeInvoke(screenPt, "getY");
                if (sx instanceof Number nx) screenX = nx.doubleValue();
                if (sy instanceof Number ny) screenY = ny.doubleValue();
            }

            Class<?> mouseCls  = Class.forName("javafx.scene.input.MouseEvent");
            Class<?> btnCls    = Class.forName("javafx.scene.input.MouseButton");
            Class<?> pickCls   = Class.forName("javafx.scene.input.PickResult");
            Class<?> etCls     = Class.forName("javafx.event.EventType");
            Object none        = btnCls.getField("NONE").get(null);

            java.lang.reflect.Constructor<?> ctor = mouseCls.getConstructor(
                etCls,
                double.class, double.class, double.class, double.class,
                btnCls, int.class,
                boolean.class, boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                pickCls
            );

            Class<?> eventCls  = Class.forName("javafx.event.Event");
            Class<?> targetCls = Class.forName("javafx.event.EventTarget");
            java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);

            // Fire MOUSE_ENTERED to trigger tooltip timer
            Object entered = mouseCls.getField("MOUSE_ENTERED").get(null);
            Object evEntered = ctor.newInstance(entered,
                localX, localY, screenX, screenY, none, 0,
                false, false, false, false, false, false, false,
                true, false, true, (Object) null);
            fireMethod.invoke(null, node, evEntered);

            // Fire MOUSE_MOVED to apply :hover CSS pseudo-class
            Object moved = mouseCls.getField("MOUSE_MOVED").get(null);
            Object evMoved = ctor.newInstance(moved,
                localX, localY, screenX, screenY, none, 0,
                false, false, false, false, false, false, false,
                true, false, true, (Object) null);
            fireMethod.invoke(null, node, evMoved);

            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of(
                "reason", "hover_failed",
                "fxId", fxId,
                "message", ex.getMessage() == null ? "" : ex.getMessage()
            ));
        }
    }

    private ActionResult handleDoubleClick(Object node, String fxId, String handle) {
        try {
            // Compute center of node in local coordinates
            Object bounds = safeInvoke(node, "getBoundsInLocal");
            double localX = 5, localY = 5;
            if (bounds != null) {
                Object w = safeInvoke(bounds, "getWidth");
                Object h = safeInvoke(bounds, "getHeight");
                if (w instanceof Number nw && h instanceof Number nh) {
                    localX = nw.doubleValue() / 2;
                    localY = nh.doubleValue() / 2;
                }
            }

            // Convert to screen coordinates (may be null in headless/off-screen)
            Object screenPt = safeInvoke(node, "localToScreen", localX, localY);
            double screenX = 0, screenY = 0;
            if (screenPt != null) {
                Object sx = safeInvoke(screenPt, "getX");
                Object sy = safeInvoke(screenPt, "getY");
                if (sx instanceof Number nx) screenX = nx.doubleValue();
                if (sy instanceof Number ny) screenY = ny.doubleValue();
            }

            // Build MouseEvent.MOUSE_CLICKED with clickCount=2 via reflection
            Class<?> mouseCls  = Class.forName("javafx.scene.input.MouseEvent");
            Class<?> btnCls    = Class.forName("javafx.scene.input.MouseButton");
            Class<?> pickCls   = Class.forName("javafx.scene.input.PickResult");
            Class<?> etCls     = Class.forName("javafx.event.EventType");

            Object mouseClicked = mouseCls.getField("MOUSE_CLICKED").get(null);
            Object primary      = btnCls.getField("PRIMARY").get(null);

            java.lang.reflect.Constructor<?> ctor = mouseCls.getConstructor(
                etCls,
                double.class, double.class, double.class, double.class,
                btnCls, int.class,
                boolean.class, boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class,
                pickCls
            );

            Object event = ctor.newInstance(
                mouseClicked,
                localX, localY, screenX, screenY,
                primary, 2,
                false, false, false, false,   // shift/ctrl/alt/meta
                false, false, false,            // primaryDown/middleDown/secondaryDown
                true, false, true,              // synthesized=true, popupTrigger=false, stillSincePress=true
                (Object) null                   // PickResult
            );

            // Fire via Event.fireEvent(EventTarget target, Event event) [static]
            Class<?> eventCls  = Class.forName("javafx.event.Event");
            Class<?> targetCls = Class.forName("javafx.event.EventTarget");
            java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);
            fireMethod.invoke(null, node, event);

            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of(
                "reason", "double_click_failed",
                "fxId", fxId,
                "message", ex.getMessage() == null ? "" : ex.getMessage()
            ));
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
        String simpleName = node.getClass().getSimpleName();

        try {
            // For item-list controls, distinguish "item not found" from "wrong control type"
            if ("ComboBox".equals(simpleName) || "ChoiceBox".equals(simpleName) || "ListView".equals(simpleName)) {
                boolean selected = selectComboBoxItem(node, value)
                    || selectChoiceBoxItem(node, value)
                    || selectListItem(node, value);
                if (!selected) {
                    return ActionResult.failure(List.of("javafx"),
                        Map.of("reason", "item_not_found", "fxId", fxId, "value", value));
                }
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "value", value), value);
            }
            if (selectTreeItem(node, value) || selectTableRow(node, value, column)) {
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

    private boolean selectChoiceBoxItem(Object node, String value) {
        if (!"ChoiceBox".equals(node.getClass().getSimpleName())) {
            return false;
        }
        Integer index = findItemIndex(ReflectiveJavaFxSupport.invoke(node, "getItems"), value);
        if (index == null) {
            return false;
        }
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

    // ---- TreeTableView helpers ---------------------------------------------

    private Object findTreeTableItem(Object treeItem, String value) {
        if (treeItem == null) return null;
        Object itemValue = safeInvoke(treeItem, "getValue");
        if (value.equals(itemValue == null ? null : itemValue.toString())) {
            return treeItem;
        }
        Object children = safeInvoke(treeItem, "getChildren");
        if (!(children instanceof Collection<?> col)) return null;
        for (Object child : col) {
            Object found = findTreeTableItem(child, value);
            if (found != null) return found;
        }
        return null;
    }

    private Object findTreeTableItemByCell(Object treeTable, Object treeItem, String value, String column) {
        if (treeItem == null) return null;
        if (treeTableItemMatchesCell(treeTable, treeItem, value, column)) return treeItem;
        Object children = safeInvoke(treeItem, "getChildren");
        if (!(children instanceof Collection<?> col)) return null;
        for (Object child : col) {
            Object found = findTreeTableItemByCell(treeTable, child, value, column);
            if (found != null) return found;
        }
        return null;
    }

    private boolean treeTableItemMatchesCell(Object treeTable, Object treeItem, String value, String column) {
        Object columns = safeInvoke(treeTable, "getColumns");
        if (columns instanceof List<?> cols) {
            for (Object col : cols) {
                String header = asString(safeInvoke(col, "getText"));
                if (column != null && !column.isBlank() && !column.equals(header)) continue;
                Object observable = safeInvoke(col, "getCellObservableValue", treeItem);
                if (observable != null) {
                    Object cellValue = safeInvoke(observable, "getValue");
                    if (value.equals(cellValue == null ? null : cellValue.toString())) return true;
                }
            }
        }
        // fallback: check all no-arg String-returning methods on the item value (handles records/beans)
        if (column == null || column.isBlank()) {
            Object itemValue = safeInvoke(treeItem, "getValue");
            if (itemValue != null) {
                for (java.lang.reflect.Method m : itemValue.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && m.getReturnType() == String.class) {
                        try {
                            Object result = m.invoke(itemValue);
                            if (value.equals(result)) return true;
                        } catch (Exception ignored) { }
                    }
                }
            }
        }
        return false;
    }

    private boolean selectTreeTableRow(Object node, String value, String column) {
        if (!"TreeTableView".equals(node.getClass().getSimpleName())) return false;
        Object root = safeInvoke(node, "getRoot");
        Object match = findTreeTableItemByCell(node, root, value, column);
        if (match == null) return false;
        Object rowIndex = safeInvoke(node, "getRow", match);
        if (!(rowIndex instanceof Number idx)) return false;
        Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
        ReflectiveJavaFxSupport.invokeOnType(
            selectionModel,
            "javafx.scene.control.TableSelectionModel",
            "select",
            new Class<?>[] {int.class},
            idx.intValue()
        );
        return true;
    }

    private String getTreeTableCell(Object node, String rowValue, String column) {
        if (!"TreeTableView".equals(node.getClass().getSimpleName())) return null;
        Object root = safeInvoke(node, "getRoot");
        Object match = findTreeTableItemByCell(node, root, rowValue, null);
        if (match == null) return null;
        Object columns = safeInvoke(node, "getColumns");
        if (!(columns instanceof List<?> cols)) return null;
        for (Object col : cols) {
            String header = asString(safeInvoke(col, "getText"));
            if (column.equals(header)) {
                Object observable = safeInvoke(col, "getCellObservableValue", match);
                if (observable != null) {
                    Object cellValue = safeInvoke(observable, "getValue");
                    return cellValue == null ? null : cellValue.toString();
                }
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

        int index = 0;
        if (selector.has("index") && !selector.get("index").isJsonNull()) {
            index = selector.get("index").getAsInt();
        }

        if (selector.has("id") && !selector.get("id").isJsonNull()) {
            String requestedId = selector.get("id").getAsString();
            return snapshot.nodes().stream()
                .filter(node -> requestedId.equals(node.metadata().get("fxId")))
                .skip(index)
                .findFirst();
        }

        if (selector.has("text") && !selector.get("text").isJsonNull()
            && selector.has("type") && !selector.get("type").isJsonNull()) {
            String requestedText = selector.get("text").getAsString();
            String requestedType = selector.get("type").getAsString();
            return snapshot.nodes().stream()
                .filter(node -> requestedType.equals(node.metadata().get("nodeType")))
                .filter(node -> requestedText.equals(node.metadata().get("text")))
                .skip(index)
                .findFirst();
        }

        if (selector.has("type") && !selector.get("type").isJsonNull()) {
            String requestedType = selector.get("type").getAsString();
            return snapshot.nodes().stream()
                .filter(node -> requestedType.equals(node.metadata().get("nodeType")))
                .skip(index)
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

    private void walk(Object node, String path, String windowType, IdentityHashMap<Object, String> handles, AtomicInteger counter, List<Map<String, Object>> result) {
        String handle = handles.computeIfAbsent(node, ignored -> "node-" + counter.incrementAndGet());
        result.add(toNodeMap(node, path, handle, windowType));
        List<Object> children = childrenOf(node);
        for (int index = 0; index < children.size(); index++) {
            Object child = children.get(index);
            walk(child, path + "/" + child.getClass().getSimpleName() + "[" + (index + 1) + "]", windowType, handles, counter, result);
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

    private Map<String, Object> toNodeMap(Object node, String path, String handle, String windowType) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("handle", handle);
        metadata.put("fxId", safeString(node, "getId"));
        metadata.put("nodeType", node.getClass().getSimpleName());
        metadata.put("text", ReflectiveJavaFxSupport.textOf(node));
        metadata.put("hierarchyPath", path);
        metadata.put("visible", safeBoolean(node, "isVisible", true));
        metadata.put("enabled", !safeBoolean(node, "isDisabled", false));
        if (windowType != null) {
            metadata.put("windowType", windowType);
        }
        return metadata;
    }

    private Map<String, Object> toNodeMap(Object node, String path, String handle) {
        return toNodeMap(node, path, handle, "primary");
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

    // ---- Overlay window utilities ------------------------------------------

    /** Returns a snapshot of all open JavaFX windows (safe to call on any thread). */
    private List<Object> getAllWindows() {
        try {
            Class<?> windowClass = ReflectiveJavaFxSupport.loadClass("javafx.stage.Window");
            Object windows = ReflectiveJavaFxSupport.invokeStatic(windowClass, "getWindows");
            if (windows instanceof List<?> list) {
                return new ArrayList<>(list.stream().map(Object.class::cast).toList());
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }

    private String classifyWindow(Object window) {
        String simpleName = window.getClass().getSimpleName();
        if (simpleName.contains("Alert") || simpleName.contains("Dialog")) return "dialog";
        if (simpleName.contains("ContextMenu")) return "contextmenu";
        if (simpleName.contains("Popup")) return "popup";
        // Stage with DialogPane scene root → dialog
        try {
            Object scn = ReflectiveJavaFxSupport.invoke(window, "getScene");
            if (scn != null) {
                Object root = ReflectiveJavaFxSupport.invoke(scn, "getRoot");
                if (root != null && root.getClass().getSimpleName().equals("DialogPane")) {
                    return "dialog";
                }
            }
        } catch (Exception ignored) {
        }
        return "overlay";
    }

    private boolean isContextMenuWindow(Object window) {
        String name = window.getClass().getName();
        return name.contains("ContextMenu") || name.contains("PopupControl");
    }

    private boolean isMenuPopupWindow(Object window) {
        String name = window.getClass().getName();
        return name.contains("Menu") || name.contains("Popup");
    }

    private boolean isDatePickerWindow(Object window) {
        try {
            Object scn = ReflectiveJavaFxSupport.invoke(window, "getScene");
            if (scn == null) return false;
            Object root = ReflectiveJavaFxSupport.invoke(scn, "getRoot");
            return root != null && root.getClass().getName().contains("DatePicker");
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Runs {@code triggerOnFxThread} on the JavaFX thread, then polls off-thread
     * until at least one window matching {@code windowPredicate} appears (or times out).
     */
    private ActionResult performWithOverlayWait(
            Supplier<ActionResult> triggerOnFxThread,
            Predicate<Object> windowPredicate) {
        ActionResult triggerResult = ReflectiveJavaFxSupport.onFxThread(triggerOnFxThread);
        long deadline = System.currentTimeMillis() + OVERLAY_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            List<Object> windows = getAllWindows();
            if (windows.stream().anyMatch(windowPredicate)) {
                return triggerResult;
            }
            try {
                Thread.sleep(OVERLAY_POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return triggerResult;
    }

    private Object safeInvoke(Object target, String method, Object... args) {
        try {
            return ReflectiveJavaFxSupport.invoke(target, method, args);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ---- Scroll helpers ----------------------------------------------------

    private Object findScrollPaneAncestor(Object node) {
        Object current = safeInvoke(node, "getParent");
        while (current != null) {
            if (current.getClass().getName().endsWith("ScrollPane")) {
                return current;
            }
            current = safeInvoke(current, "getParent");
        }
        return null;
    }

    private void scrollToNode(Object scrollPane, Object node) {
        try {
            Object content = safeInvoke(scrollPane, "getContent");
            if (content == null) return;
            Object contentBounds  = safeInvoke(content, "getBoundsInLocal");
            Object viewportBounds = safeInvoke(scrollPane, "getViewportBounds");
            Object nodeBoundsInLocal   = safeInvoke(node, "getBoundsInLocal");
            Object nodeBoundsInScene   = safeInvoke(node, "localToScene", nodeBoundsInLocal);
            Object nodeBoundsInContent = safeInvoke(content, "sceneToLocal", nodeBoundsInScene);
            if (nodeBoundsInContent == null || contentBounds == null || viewportBounds == null) return;
            double nodeTop  = (double) ReflectiveJavaFxSupport.invokeExact(nodeBoundsInContent, "getMinY",  new Class<?>[0]);
            double contentH = (double) ReflectiveJavaFxSupport.invokeExact(contentBounds,       "getHeight", new Class<?>[0]);
            double viewportH = (double) ReflectiveJavaFxSupport.invokeExact(viewportBounds,     "getHeight", new Class<?>[0]);
            double range = contentH - viewportH;
            if (range <= 0) return;
            double vvalue = Math.max(0.0, Math.min(1.0, nodeTop / range));
            ReflectiveJavaFxSupport.invoke(scrollPane, "setVvalue", vvalue);
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private void scrollByDelta(Object scrollPane, double deltaX, double deltaY) {
        try {
            if (deltaY != 0.0) {
                Object vv = safeInvoke(scrollPane, "getVvalue");
                double cur = vv == null ? 0.0 : ((Number) vv).doubleValue();
                ReflectiveJavaFxSupport.invoke(scrollPane, "setVvalue", Math.max(0.0, Math.min(1.0, cur + deltaY)));
            }
            if (deltaX != 0.0) {
                Object hv = safeInvoke(scrollPane, "getHvalue");
                double cur = hv == null ? 0.0 : ((Number) hv).doubleValue();
                ReflectiveJavaFxSupport.invoke(scrollPane, "setHvalue", Math.max(0.0, Math.min(1.0, cur + deltaX)));
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private Object findFirstScrollPane(DiscoverySnapshot snapshot) {
        return snapshot.nodes().stream()
            .filter(r -> r.node().getClass().getName().endsWith("ScrollPane"))
            .map(NodeRef::node)
            .findFirst()
            .orElse(null);
    }

    private ActionResult doRightClick(JsonObject selector) {
        DiscoverySnapshot snapshot = snapshot();
        Optional<NodeRef> match = resolve(selector, snapshot);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }
        Object node = match.get().node();
        String handle = asString(match.get().metadata().get("handle"));
        String fxId = asString(match.get().metadata().get("fxId"));

        Object contextMenu = safeInvoke(node, "getContextMenu");
        if (contextMenu == null) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_context_menu", "fxId", fxId));
        }
        // Get screen coordinates of the node
        Object bounds = safeInvoke(node, "localToScreen", 0.0, 0.0);
        double screenX = 0, screenY = 0;
        if (bounds != null) {
            Object sx = safeInvoke(bounds, "getX");
            Object sy = safeInvoke(bounds, "getY");
            if (sx instanceof Number nx) screenX = nx.doubleValue();
            if (sy instanceof Number ny) screenY = ny.doubleValue();
        }
        try {
            ReflectiveJavaFxSupport.invoke(contextMenu, "show", node, screenX, screenY);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"),
                Map.of("reason", "show_context_menu_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
        }
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
    }

    private ActionResult doClickMenuItem(JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String text = payload != null && payload.has("text") ? payload.get("text").getAsString() : null;
            String id   = payload != null && payload.has("id")   ? payload.get("id").getAsString()   : null;
            String path = payload != null && payload.has("path") ? payload.get("path").getAsString() : null;

            // MenuItem is NOT a JavaFX Node — look in window.getItems(), not the scene tree.
            List<Object> windows = getAllWindows();
            for (Object window : windows) {
                Object items = safeInvoke(window, "getItems");
                if (!(items instanceof List<?> itemList)) continue;

                if (path != null && !path.isBlank()) {
                    Object item = findMenuItemByPath(window, path.split("/"));
                    if (item != null) {
                        safeInvoke(item, "fire");
                        return ActionResult.success("javafx", null, Map.of("path", path), null);
                    }
                } else {
                    Object item = findMenuItemInList(itemList, text, id);
                    if (item != null) {
                        safeInvoke(item, "fire");
                        return ActionResult.success("javafx", null,
                            Map.of("text", text != null ? text : Objects.toString(id, "")), null);
                    }
                }
            }
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "menu_item_not_found"));
        });
    }

    private Object findMenuItemInList(List<?> items, String text, String id) {
        for (Object item : items) {
            String itemText = asString(safeInvoke(item, "getText"));
            String itemId   = asString(safeInvoke(item, "getId"));
            if ((text != null && text.equals(itemText)) || (id != null && id.equals(itemId))) {
                return item;
            }
        }
        return null;
    }

    private Object findMenuItemByPath(Object contextMenuWindow, String[] segments) {
        try {
            Object items = ReflectiveJavaFxSupport.invoke(contextMenuWindow, "getItems");
            if (!(items instanceof List<?> list)) return null;
            Object current = null;
            List<?> currentList = list;
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i];
                Object found = null;
                for (Object item : currentList) {
                    String label = asString(safeInvoke(item, "getText"));
                    String itemId = asString(safeInvoke(item, "getId"));
                    if (seg.equals(label) || seg.equals(itemId)) {
                        found = item;
                        break;
                    }
                }
                if (found == null) return null;
                current = found;
                if (i < segments.length - 1) {
                    Object subItems = safeInvoke(found, "getItems");
                    if (!(subItems instanceof List<?> sub)) return null;
                    currentList = sub;
                }
            }
            return current;
        } catch (Exception ignored) {
            return null;
        }
    }

    private ActionResult doDismissMenu() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            List<Object> windows = getAllWindows();
            for (Object window : windows) {
                String name = window.getClass().getName();
                if (name.contains("ContextMenu") || name.contains("PopupControl")) {
                    safeInvoke(window, "hide");
                }
            }
            return ActionResult.success("javafx", null, Map.of(), null);
        });
    }

    // ---- MenuBar actions ---------------------------------------------------

    private ActionResult doOpenMenu(JsonObject selector, JsonObject payload) {
        DiscoverySnapshot snapshot = snapshot();
        Optional<NodeRef> match = resolve(selector, snapshot);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }
        Object menuBar = match.get().node();
        String handle = asString(match.get().metadata().get("handle"));
        String fxId = asString(match.get().metadata().get("fxId"));
        String menuName = payload != null && payload.has("menu") ? payload.get("menu").getAsString() : null;

        // SkinBase.getChildren() is protected — walk the public scene graph.
        // MenuBarSkin uses an inner class (MenuBarButton extends MenuButton),
        // so match by full class hierarchy, not simple name.
        List<Object> buttons = findChildrenAssignableTo(menuBar, "javafx.scene.control.MenuButton");
        for (Object button : buttons) {
            String btnText = asString(safeInvoke(button, "getText"));
            if (menuName == null || menuName.equals(btnText)) {
                try {
                    ReflectiveJavaFxSupport.invoke(button, "show");
                } catch (Exception ex) {
                    return ActionResult.failure(List.of("javafx"),
                        Map.of("reason", "show_menu_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
                }
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "menu", menuName != null ? menuName : ""), null);
            }
        }
        return ActionResult.failure(List.of("javafx"), Map.of("reason", "menu_not_found",
            "menu", menuName != null ? menuName : "", "buttonsFound", buttons.size()));
    }

    private ActionResult doNavigateMenu(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String pathStr = payload != null && payload.has("path") ? payload.get("path").getAsString() : "";
            if (pathStr.isBlank()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_path"));
            }
            String[] segments = pathStr.split("/");

            DiscoverySnapshot snapshot = snapshot();
            Optional<NodeRef> menuBarRef = snapshot.nodes().stream()
                .filter(n -> "MenuBar".equals(n.metadata().get("nodeType")))
                .findFirst();

            if (menuBarRef.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "menubar_not_found"));
            }
            Object menuBar = menuBarRef.get().node();
            Object menus = ReflectiveJavaFxSupport.invoke(menuBar, "getMenus");
            if (!(menus instanceof List<?> menuList)) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_menus"));
            }

            // Find top-level menu
            Object topMenu = null;
            for (Object m : menuList) {
                if (segments[0].equals(asString(safeInvoke(m, "getText")))) {
                    topMenu = m;
                    break;
                }
            }
            if (topMenu == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "top_menu_not_found", "menu", segments[0]));
            }

            // Traverse items for middle segments
            Object currentMenu = topMenu;
            for (int i = 1; i < segments.length - 1; i++) {
                Object items = safeInvoke(currentMenu, "getItems");
                if (!(items instanceof List<?> itemList)) break;
                Object found = null;
                for (Object item : itemList) {
                    if (segments[i].equals(asString(safeInvoke(item, "getText")))) {
                        found = item;
                        break;
                    }
                }
                if (found == null) {
                    return ActionResult.failure(List.of("javafx"), Map.of("reason", "submenu_not_found", "segment", segments[i]));
                }
                currentMenu = found;
            }

            // Fire the final item
            String lastSeg = segments[segments.length - 1];
            Object items = safeInvoke(currentMenu, "getItems");
            if (!(items instanceof List<?> itemList)) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_items_in_menu"));
            }
            for (Object item : itemList) {
                if (lastSeg.equals(asString(safeInvoke(item, "getText")))) {
                    safeInvoke(item, "fire");
                    return ActionResult.success("javafx", null, Map.of("path", pathStr), null);
                }
            }
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "menu_item_not_found", "item", lastSeg));
        });
    }

    // ---- DatePicker actions ------------------------------------------------

    private ActionResult doOpenDatePicker(JsonObject selector) {
        DiscoverySnapshot snapshot = snapshot();
        Optional<NodeRef> match = resolve(selector, snapshot);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }
        Object datePicker = match.get().node();
        String handle = asString(match.get().metadata().get("handle"));
        String fxId = asString(match.get().metadata().get("fxId"));
        try {
            ReflectiveJavaFxSupport.invoke(datePicker, "show");
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"),
                Map.of("reason", "open_datepicker_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
        }
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
    }

    private ActionResult doNavigateMonth(JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String direction = payload != null && payload.has("direction") ? payload.get("direction").getAsString() : "next";
            Object dpContent = findDatePickerContent();
            if (dpContent == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "datepicker_popup_not_found"));
            }
            // Get all Button children; first = previous, last = next
            List<Object> buttons = findChildrenAssignableTo(dpContent, "javafx.scene.control.Button");
            if (buttons.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "navigation_button_not_found"));
            }
            Object button = "next".equalsIgnoreCase(direction)
                ? buttons.get(buttons.size() - 1)
                : buttons.get(0);
            safeInvoke(button, "fire");
            return ActionResult.success("javafx", null, Map.of("direction", direction), null);
        });
    }

    private ActionResult doPickDate(JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String dateStr = payload != null && payload.has("date") ? payload.get("date").getAsString() : null;
            if (dateStr == null || dateStr.isBlank()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_date"));
            }

            Object dpContent = findDatePickerContent();
            if (dpContent == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "datepicker_popup_not_found"));
            }

            Object targetDate;
            try {
                Class<?> localDateClass = ReflectiveJavaFxSupport.loadClass("java.time.LocalDate");
                targetDate = ReflectiveJavaFxSupport.invokeStatic(localDateClass, "parse", dateStr);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "invalid_date_format", "date", dateStr));
            }

            for (int attempt = 0; attempt < 48; attempt++) {
                Object cell = findDateCellForDate(dpContent, targetDate);
                if (cell != null) {
                    safeInvoke(cell, "fire");
                    return ActionResult.success("javafx", null, Map.of("date", dateStr), null);
                }
                // Navigate forward or backward toward the target month
                List<Object> buttons = findChildrenAssignableTo(dpContent, "javafx.scene.control.Button");
                if (buttons.size() < 2) break;
                boolean forward = shouldNavigateForward(dpContent, targetDate);
                safeInvoke(forward ? buttons.get(buttons.size() - 1) : buttons.get(0), "fire");
            }
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "date_cell_not_found", "date", dateStr));
        });
    }

    private ActionResult doSetDate(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String dateStr = payload != null && payload.has("date") ? payload.get("date").getAsString() : null;
            if (dateStr == null || dateStr.isBlank()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_date"));
            }
            DiscoverySnapshot snapshot = snapshot();
            Optional<NodeRef> match = resolve(selector, snapshot);
            if (match.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
            }
            Object node = match.get().node();
            String fxId = asString(match.get().metadata().get("fxId"));
            String handle = asString(match.get().metadata().get("handle"));
            try {
                Class<?> ldClass = ReflectiveJavaFxSupport.loadClass("java.time.LocalDate");
                Object localDate = ReflectiveJavaFxSupport.invokeStatic(ldClass, "parse", dateStr);
                ReflectiveJavaFxSupport.invoke(node, "setValue", localDate);
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "date", dateStr), dateStr);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "set_date_failed", "fxId", fxId,
                        "detail", ex.getMessage() == null ? "" : ex.getMessage()));
            }
        });
    }

    private ActionResult doSetColor(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String hexStr = payload != null && payload.has("color") ? payload.get("color").getAsString() : null;
            if (hexStr == null || hexStr.isBlank()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_color"));
            }
            DiscoverySnapshot snapshot = snapshot();
            Optional<NodeRef> match = resolve(selector, snapshot);
            if (match.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
            }
            Object node = match.get().node();
            String fxId = asString(match.get().metadata().get("fxId"));
            String handle = asString(match.get().metadata().get("handle"));
            try {
                Class<?> colorClass = ReflectiveJavaFxSupport.loadClass("javafx.scene.paint.Color");
                Object color = ReflectiveJavaFxSupport.invokeStatic(colorClass, "web", hexStr);
                ReflectiveJavaFxSupport.invoke(node, "setValue", color);
                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "color", hexStr), hexStr);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "set_color_failed", "fxId", fxId,
                        "detail", ex.getMessage() == null ? "" : ex.getMessage()));
            }
        });
    }

    private ActionResult doOpenColorPicker(JsonObject selector) {
        DiscoverySnapshot snapshot = snapshot();
        Optional<NodeRef> match = resolve(selector, snapshot);
        if (match.isEmpty()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
        }
        Object colorPicker = match.get().node();
        String handle = asString(match.get().metadata().get("handle"));
        String fxId = asString(match.get().metadata().get("fxId"));
        try {
            ReflectiveJavaFxSupport.invoke(colorPicker, "show");
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"),
                Map.of("reason", "open_colorpicker_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
        }
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
    }

    private ActionResult doCloseApp() {
        // Schedule System.exit(0) on a daemon thread with a short delay so the
        // HTTP response can flush before the JVM terminates. Platform.exit() alone
        // only stops the JavaFX runtime — the HTTP server's non-daemon threads keep
        // the JVM alive. System.exit(0) kills everything cleanly.
        Thread shutdownThread = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            System.exit(0);
        });
        shutdownThread.setDaemon(true);
        shutdownThread.start();
        return ActionResult.success("javafx", null, Map.of(), null);
    }

    /**
     * Parse a Playwright-style key string (e.g. "Control+Shift+Z", "ctrl+c", "Enter")
     * into an array where [0..n-2] are modifier names and [n-1] is the key name.
     * All parts are uppercased; CTRL→CONTROL, WIN→META aliases are applied.
     */
    private String[] parseKeyString(String key) {
        String[] parts = key.trim().split("\\+");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim().toUpperCase();
            if (p.equals("CTRL"))  p = "CONTROL";
            if (p.equals("WIN"))   p = "META";
            parts[i] = p;
        }
        return parts;
    }

    private ActionResult doPressKey(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String key = payload != null && payload.has("key") ? payload.get("key").getAsString() : "";
            if (key.isBlank()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_key"));
            }

            // Parse key string
            String[] parts = parseKeyString(key);
            String keyName = parts[parts.length - 1];
            boolean shiftDown   = false;
            boolean controlDown = false;
            boolean altDown     = false;
            boolean metaDown    = false;
            for (int i = 0; i < parts.length - 1; i++) {
                switch (parts[i]) {
                    case "SHIFT"   -> shiftDown   = true;
                    case "CONTROL" -> controlDown = true;
                    case "ALT"     -> altDown     = true;
                    case "META"    -> metaDown    = true;
                }
            }

            // Resolve KeyCode
            Class<?> keyCodeCls;
            Object keyCode;
            try {
                keyCodeCls = Class.forName("javafx.scene.input.KeyCode");
                keyCode = Enum.valueOf((Class<Enum>) keyCodeCls, keyName);
            } catch (IllegalArgumentException ex) {
                return ActionResult.failure(List.of("javafx"),
                    Map.of("reason", "unknown_key", "key", keyName));
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"),
                    Map.of("reason", "press_key_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
            }

            // Resolve target node: selector → matched node; no selector → scene focus owner or root
            Object target;
            String fxId = "";
            String handle = null;
            boolean hasSelector = selector != null && !selector.entrySet().isEmpty();
            if (hasSelector) {
                DiscoverySnapshot snapshot = snapshot();
                Optional<NodeRef> match = resolve(selector, snapshot);
                if (match.isEmpty()) {
                    return ActionResult.failure(List.of("javafx"), Map.of("reason", "selector_not_found"));
                }
                target  = match.get().node();
                fxId    = asString(match.get().metadata().get("fxId"));
                handle  = asString(match.get().metadata().get("handle"));
            } else {
                Object scene = sceneSupplier.get();
                if (scene == null) {
                    return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scene"));
                }
                Object focusOwner = safeInvoke(scene, "getFocusOwner");
                if (focusOwner != null) {
                    target = focusOwner;
                } else {
                    target = safeInvoke(scene, "getRoot");
                }
                if (target == null) {
                    return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_target_node"));
                }
            }

            // Build and fire KEY_PRESSED + KEY_RELEASED
            try {
                Class<?> keyEventCls = Class.forName("javafx.scene.input.KeyEvent");
                Class<?> etCls       = Class.forName("javafx.event.EventType");

                Object keyPressed  = keyEventCls.getField("KEY_PRESSED").get(null);
                Object keyReleased = keyEventCls.getField("KEY_RELEASED").get(null);
                String charUndef   = (String) keyEventCls.getField("CHAR_UNDEFINED").get(null);
                String keyText     = (String) safeInvoke(keyCode, "getName");
                if (keyText == null) keyText = keyName;

                // KeyEvent(EventType, String character, String text, KeyCode,
                //          boolean shift, boolean control, boolean alt, boolean meta)
                java.lang.reflect.Constructor<?> ctor = keyEventCls.getConstructor(
                    etCls, String.class, String.class, keyCodeCls,
                    boolean.class, boolean.class, boolean.class, boolean.class
                );

                Object pressedEvent  = ctor.newInstance(keyPressed,  charUndef, keyText, keyCode,
                                                         shiftDown, controlDown, altDown, metaDown);
                Object releasedEvent = ctor.newInstance(keyReleased, charUndef, keyText, keyCode,
                                                         shiftDown, controlDown, altDown, metaDown);

                Class<?> eventCls  = Class.forName("javafx.event.Event");
                Class<?> targetCls = Class.forName("javafx.event.EventTarget");
                java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);
                fireMethod.invoke(null, target, pressedEvent);
                fireMethod.invoke(null, target, releasedEvent);

                return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "key", key), null);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of(
                    "reason", "press_key_failed",
                    "key", key,
                    "message", ex.getMessage() == null ? "" : ex.getMessage()
                ));
            }
        });
    }

    private ActionResult doDismissColorPicker() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            List<Object> windows = getAllWindows();
            for (Object window : windows) {
                String name = window.getClass().getName();
                if (name.contains("ColorPalette") || name.contains("CustomColor") || name.contains("PopupControl")) {
                    try {
                        Object scene = ReflectiveJavaFxSupport.invoke(window, "getScene");
                        if (scene != null) {
                            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
                            if (root != null && root.getClass().getName().contains("Color")) {
                                safeInvoke(window, "hide");
                                continue;
                            }
                        }
                    } catch (Exception ignored) {}
                    if (name.contains("ColorPalette") || name.contains("CustomColor")) {
                        safeInvoke(window, "hide");
                    }
                }
            }
            // Also try hiding via any visible ColorPicker nodes
            List<Object> allWindows = getAllWindows();
            for (Object window : allWindows) {
                try {
                    Object scene = ReflectiveJavaFxSupport.invoke(window, "getScene");
                    if (scene == null) continue;
                    Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
                    if (root == null) continue;
                    findChildrenByType(root, "ColorPicker").forEach(cp -> safeInvoke(cp, "hide"));
                } catch (Exception ignored) {}
            }
            return ActionResult.success("javafx", null, Map.of(), null);
        });
    }

    private boolean isColorPickerWindow(Object window) {
        String name = window.getClass().getName();
        if (name.contains("ColorPalette") || name.contains("CustomColor")) return true;
        try {
            Object scene = ReflectiveJavaFxSupport.invoke(window, "getScene");
            if (scene == null) return false;
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            return root != null && root.getClass().getName().contains("Color");
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean shouldNavigateForward(Object dpContent, Object targetDate) {
        // Compare target against the first visible date cell to determine direction
        List<Object> cells = findDateCells(dpContent);
        for (Object cell : cells) {
            Object cellDate = safeInvoke(cell, "getItem");
            if (cellDate == null) continue;
            try {
                Object cmp = ReflectiveJavaFxSupport.invoke(targetDate, "compareTo", cellDate);
                if (cmp instanceof Number n) {
                    return n.intValue() > 0; // target is after this cell → navigate forward
                }
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    private List<Object> findDateCells(Object container) {
        // DateCell is public; concrete cells in DatePickerContent may be subclasses
        List<Object> cells = findChildrenAssignableTo(container, "javafx.scene.control.DateCell");
        if (cells.isEmpty()) {
            cells = findChildrenByType(container, "DateCell");
        }
        return cells;
    }

    private Object findDatePickerContent() {
        List<Object> windows = getAllWindows();
        for (Object window : windows) {
            Object scn = safeInvoke(window, "getScene");
            if (scn == null) continue;
            Object root = safeInvoke(scn, "getRoot");
            if (root == null) continue;
            if (root.getClass().getName().contains("DatePicker")) return root;
            // Search children for DatePickerContent
            Object found = findChildByClassName(root, "DatePickerContent");
            if (found != null) return found;
        }
        return null;
    }

    private Object findDateCellForDate(Object container, Object targetDate) {
        for (Object cell : findDateCells(container)) {
            Object cellDate = safeInvoke(cell, "getItem");
            if (cellDate == null) continue;
            try {
                Object cmp = ReflectiveJavaFxSupport.invoke(targetDate, "compareTo", cellDate);
                if (cmp instanceof Number n && n.intValue() == 0) return cell;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // ---- Dialog / Alert actions --------------------------------------------

    private ActionResult doGetDialog() {
        // Poll off-FX-thread: the dialog window may not be in Window.getWindows() yet
        // if the ActionEvent that opened it was deferred via Platform.runLater.
        long deadline = System.currentTimeMillis() + OVERLAY_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            ActionResult result = ReflectiveJavaFxSupport.onFxThread(this::tryFindDialog);
            if (result.ok()) return result;
            try {
                Thread.sleep(OVERLAY_POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return ActionResult.failure(List.of("javafx"), Map.of("reason", "dialog_not_found"));
    }

    private ActionResult tryFindDialog() {
        List<Object> windows = getAllWindows();
        for (Object window : windows) {
            Object scn = safeInvoke(window, "getScene");
            if (scn == null) continue;
            Object sceneRoot = safeInvoke(scn, "getRoot");
            if (sceneRoot == null) continue;
            // DialogPane may be the root or nested one level inside a wrapper pane
            Object dialogPane = isDialogPane(sceneRoot)
                ? sceneRoot
                : findChildByClassName(sceneRoot, "DialogPane");
            if (dialogPane == null) continue;
            Map<String, Object> desc = buildDialogDescriptor(window, dialogPane);
            return ActionResult.success("javafx", null, desc, null);
        }
        return ActionResult.failure(List.of("javafx"), Map.of("reason", "dialog_not_found"));
    }

    private boolean isDialogPane(Object obj) {
        String name = obj.getClass().getName();
        return name.equals("javafx.scene.control.DialogPane") || name.contains("DialogPane");
    }

    private Map<String, Object> buildDialogDescriptor(Object window, Object dialogPane) {
        Map<String, Object> desc = new LinkedHashMap<>();
        desc.put("title", asString(safeInvoke(window, "getTitle")));
        desc.put("header", asString(safeInvoke(dialogPane, "getHeaderText")));
        desc.put("content", asString(safeInvoke(dialogPane, "getContentText")));

        // Alert type (if it's an Alert)
        try {
            Object dialog = safeInvoke(dialogPane, "getDialog");
            if (dialog != null) {
                Object alertType = safeInvoke(dialog, "getAlertType");
                if (alertType != null) {
                    desc.put("alertType", alertType.toString());
                }
            }
        } catch (Exception ignored) {
        }

        // Button labels
        Object buttonBar = findChildByClassName(dialogPane, "ButtonBar");
        List<String> buttonLabels = new ArrayList<>();
        if (buttonBar != null) {
            List<Object> buttons = findChildrenByType(buttonBar, "Button");
            for (Object btn : buttons) {
                String label = asString(safeInvoke(btn, "getText"));
                if (label != null && !label.isBlank()) buttonLabels.add(label);
            }
        }
        desc.put("buttons", buttonLabels);
        return desc;
    }

    private ActionResult doDismissDialog(JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            String buttonText = payload != null && payload.has("button") ? payload.get("button").getAsString() : null;

            List<Object> windows = getAllWindows();
            for (Object window : windows) {
                Object scn = safeInvoke(window, "getScene");
                if (scn == null) continue;
                Object sceneRoot = safeInvoke(scn, "getRoot");
                if (sceneRoot == null) continue;
                Object root = isDialogPane(sceneRoot) ? sceneRoot : findChildByClassName(sceneRoot, "DialogPane");
                if (root == null) continue;

                if (buttonText != null && !buttonText.isBlank()) {
                    // Try exact button match by text
                    Object buttonBar = findChildByClassName(root, "ButtonBar");
                    if (buttonBar != null) {
                        List<Object> buttons = findChildrenByType(buttonBar, "Button");
                        for (Object btn : buttons) {
                            String label = asString(safeInvoke(btn, "getText"));
                            if (buttonText.equalsIgnoreCase(label)) {
                                safeInvoke(btn, "fire");
                                return ActionResult.success("javafx", null, Map.of("button", buttonText), null);
                            }
                        }
                    }
                }
                // Fallback: click any visible button (OK, Yes, or first available)
                Object buttonBar = findChildByClassName(root, "ButtonBar");
                if (buttonBar != null) {
                    List<Object> buttons = findChildrenByType(buttonBar, "Button");
                    if (!buttons.isEmpty()) {
                        safeInvoke(buttons.get(0), "fire");
                        return ActionResult.success("javafx", null, Map.of(), null);
                    }
                }
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "button_not_found"));
            }
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "dialog_not_found"));
        });
    }

    // ---- Node-tree search helpers -----------------------------------------

    private Object findChildByType(Object root, String simpleClassName, String text) {
        List<Object> all = findChildrenByType(root, simpleClassName);
        for (Object node : all) {
            String nodeText = asString(safeInvoke(node, "getText"));
            if (text == null || text.equals(nodeText)) return node;
        }
        return null;
    }

    private List<Object> findChildrenByType(Object root, String simpleClassName) {
        List<Object> result = new ArrayList<>();
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Object node = stack.pop();
            if (node == null) continue;
            if (node.getClass().getSimpleName().equals(simpleClassName)) {
                result.add(node);
            }
            List<Object> children = childrenOf(node);
            for (int i = children.size() - 1; i >= 0; i--) stack.push(children.get(i));
        }
        return result;
    }

    /** Find all nodes whose class is the named class or a subclass of it. */
    private List<Object> findChildrenAssignableTo(Object root, String className) {
        Class<?> target;
        try {
            target = ReflectiveJavaFxSupport.loadClass(className);
        } catch (Exception ex) {
            return List.of();
        }
        List<Object> result = new ArrayList<>();
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Object node = stack.pop();
            if (node == null) continue;
            if (target.isAssignableFrom(node.getClass())) {
                result.add(node);
            }
            List<Object> children = childrenOf(node);
            for (int i = children.size() - 1; i >= 0; i--) stack.push(children.get(i));
        }
        return result;
    }

    private Object findChildByClassName(Object root, String partialClassName) {
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Object node = stack.pop();
            if (node == null) continue;
            if (node.getClass().getName().contains(partialClassName)) return node;
            List<Object> children = childrenOf(node);
            for (int i = children.size() - 1; i >= 0; i--) stack.push(children.get(i));
        }
        return null;
    }

    // ---- Input / navigation action helpers ----------------------------------

    private ActionResult handleSetSelected(Object node, String fxId, String handle, JsonObject payload) {
        boolean selected = payload != null && payload.has("value") && payload.get("value").getAsBoolean();
        ReflectiveJavaFxSupport.invoke(node, "setSelected", selected);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), selected);
    }

    private ActionResult handleSetSlider(Object node, String fxId, String handle, JsonObject payload) {
        if (payload == null || !payload.has("value")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_value"));
        }
        double target = payload.get("value").getAsDouble();
        double min = ((Number) ReflectiveJavaFxSupport.invoke(node, "getMin")).doubleValue();
        double max = ((Number) ReflectiveJavaFxSupport.invoke(node, "getMax")).doubleValue();
        if (target < min || target > max) {
            return ActionResult.failure(List.of("javafx"),
                Map.of("reason", "value_out_of_range", "value", target, "min", min, "max", max));
        }
        ReflectiveJavaFxSupport.invoke(node, "setValue", target);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), target);
    }

    private ActionResult handleSetSpinner(Object node, String fxId, String handle, JsonObject payload) {
        if (payload == null || !payload.has("value")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_value"));
        }
        String strVal = payload.get("value").getAsString();
        try {
            Object factory = ReflectiveJavaFxSupport.invoke(node, "getValueFactory");
            if (factory == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_value_factory"));
            }
            Object converter = ReflectiveJavaFxSupport.invoke(factory, "getConverter");
            Object converted = ReflectiveJavaFxSupport.invoke(converter, "fromString", strVal);
            ReflectiveJavaFxSupport.invoke(factory, "setValue", converted);
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), strVal);
        } catch (IllegalStateException ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "set_spinner_failed", "error", ex.getMessage()));
        }
    }

    private ActionResult handleStepSpinner(Object node, String fxId, String handle, JsonObject payload) {
        if (payload == null || !payload.has("steps")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_steps"));
        }
        int steps = payload.get("steps").getAsInt();
        if (steps > 0) {
            ReflectiveJavaFxSupport.invoke(node, "increment", steps);
        } else if (steps < 0) {
            ReflectiveJavaFxSupport.invoke(node, "decrement", -steps);
        }
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
    }

    private ActionResult handleGetTabs(Object node, String fxId, String handle) {
        try {
            java.util.List<?> tabsList = (java.util.List<?>) ReflectiveJavaFxSupport.invoke(node, "getTabs");
            int size = tabsList.size();
            List<Map<String, Object>> tabs = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Object tab = tabsList.get(i);
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("text", asString(safeInvoke(tab, "getText")));
                Object dis = safeInvoke(tab, "isDisabled");
                t.put("disabled", dis instanceof Boolean b ? b : false);
                tabs.add(t);
            }
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), tabs);
        } catch (IllegalStateException ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "get_tabs_failed", "error", ex.getMessage()));
        }
    }

    private ActionResult handleSelectTab(Object node, String fxId, String handle, JsonObject payload) {
        if (payload == null || !payload.has("tab")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_tab"));
        }
        String title = payload.get("tab").getAsString();
        try {
            java.util.List<?> tabsList = (java.util.List<?>) ReflectiveJavaFxSupport.invoke(node, "getTabs");
            int size = tabsList.size();
            for (int i = 0; i < size; i++) {
                Object tab = tabsList.get(i);
                String text = asString(safeInvoke(tab, "getText"));
                if (title.equals(text)) {
                    Object selectionModel = ReflectiveJavaFxSupport.invoke(node, "getSelectionModel");
                    ReflectiveJavaFxSupport.invokeOnType(selectionModel, "javafx.scene.control.SelectionModel", "select", new Class[]{int.class}, i);
                    return ActionResult.success("javafx", handle, Map.of("fxId", fxId), title);
                }
            }
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "tab_not_found", "tab", title));
        } catch (IllegalStateException ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "select_tab_failed", "error", ex.getMessage()));
        }
    }

    private record TraversalFrame(Object node, String path) {
    }

    private record NodeRef(Object node, Map<String, Object> metadata) {
    }

    private record DiscoverySnapshot(List<NodeRef> nodes) {
    }
}
