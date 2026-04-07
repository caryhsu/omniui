package dev.omniui.agent.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.nio.charset.StandardCharsets;

public final class ReflectiveJavaFxTarget implements AutomationTarget {
    private static final int OVERLAY_TIMEOUT_MS = 2_000;
    private static final int OVERLAY_POLL_INTERVAL_MS = 50;
    private static final int MAX_RECORDER_EVENTS = 1_000;

    private final String appName;
    private final Supplier<Object> sceneSupplier;

    // ── Recorder state ────────────────────────────────────────────────────────
    private final ConcurrentLinkedDeque<Map<String, Object>> recorderBuffer = new ConcurrentLinkedDeque<>();
    private final AtomicInteger deliveredUpTo = new AtomicInteger(0); // poll cursor
    private final ConcurrentHashMap<String, StringBuilder> keyAccumulator   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long>          keyTimestamps    = new ConcurrentHashMap<>();
    private volatile Object mouseEventFilter    = null;
    private volatile Object keyEventFilter      = null;
    private volatile Object keyPressFilter      = null;  // Enter/Space in dialogs
    private volatile Object mousePressFilter    = null;  // for drag detection
    private volatile Object mouseReleaseFilter  = null;  // for drag detection
    private volatile Object dialogActionFilter  = null;  // ActionEvent on dialog buttons

    // ColorPicker value listeners registered during recording (pairs of [valueProp, listener])
    private final List<Object[]> colorPickerListeners = new ArrayList<>();
    // ComboBox value listeners registered during recording (pairs of [valueProp, listener])
    private final List<Object[]> comboBoxListeners    = new ArrayList<>();
    // DatePicker value listeners registered during recording (pairs of [valueProp, listener])
    private final List<Object[]> datePickerListeners  = new ArrayList<>();
    // Last recorded value per ComboBox fxId (deduplication)
    private final Map<String, String> lastComboValues = new java.util.HashMap<>();
    // Scenes (including overlays) that already have recorder event filters attached
    private final Set<Object> registeredWindowScenes  = Collections.newSetFromMap(new IdentityHashMap<>());

    // Pending drag press info (set on MOUSE_PRESSED, consumed on MOUSE_RELEASED)
    private volatile double  dragPressX         = 0;
    private volatile double  dragPressY         = 0;
    private volatile String  dragPressId        = "";
    private volatile String  dragPressText      = "";
    private volatile String  dragPressType      = "";
    private volatile int     dragPressIndex     = 0;
    private volatile double  dragPressTime      = 0;
    // Set to true after a drag is recorded; onMouseClicked will skip the next click
    private volatile boolean dragJustFired      = false;
    private static final double DRAG_MIN_DIST   = 15.0; // px threshold to distinguish drag from click

    // ── Recording title animation ─────────────────────────────────────────────
    private volatile java.util.Timer recordingTitleTimer    = null;
    private volatile Object          recordingTitleWindow   = null;
    private volatile String          recordingOriginalTitle = "";

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
            case "get_focused"     -> doGetFocused();
            case "press_key"       -> doPressKey(selector, payload);
            case "get_clipboard"   -> doGetClipboard();
            case "set_clipboard"   -> doSetClipboard(payload);
            case "click_at"        -> doClickAt(payload);
            case "drag"            -> doDrag(selector, payload);
            case "drag_to"         -> doDragTo(selector, payload);
            case "get_windows"         -> ReflectiveJavaFxSupport.onFxThread(() -> handleGetWindows());
            case "focus_window"        -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "maximize_window"     -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "minimize_window"     -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "restore_window"      -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "is_maximized"        -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "is_minimized"        -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "set_window_size"     -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "set_window_position" -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "get_window_size"     -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
            case "get_window_position" -> ReflectiveJavaFxSupport.onFxThread(() -> handleWindowAction(action, payload));
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

    // ── Recorder ─────────────────────────────────────────────────────────────

    @Override
    public ActionResult startRecording() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            Object scene = sceneSupplier.get();
            if (scene == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scene"));
            }
            recorderBuffer.clear();
            deliveredUpTo.set(0);
            keyAccumulator.clear();
            keyTimestamps.clear();

            Class<?> eventHandlerClass = ReflectiveJavaFxSupport.loadClass("javafx.event.EventHandler");
            Class<?> mouseEventClass   = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.MouseEvent");
            Class<?> keyEventClass     = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.KeyEvent");
            Class<?> eventTypeClass    = ReflectiveJavaFxSupport.loadClass("javafx.event.EventType");

            mouseEventFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onMouseClicked(args[0]);
                    }
                    return null;
                }
            );

            keyEventFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onKeyTyped(args[0]);
                    }
                    return null;
                }
            );

            keyPressFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onKeyPressed(args[0]);
                    }
                    return null;
                }
            );

            mousePressFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onMousePressed(args[0]);
                    }
                    return null;
                }
            );

            mouseReleaseFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onMouseReleased(args[0]);
                    }
                    return null;
                }
            );

            dialogActionFilter = java.lang.reflect.Proxy.newProxyInstance(
                eventHandlerClass.getClassLoader(),
                new Class<?>[]{ eventHandlerClass },
                (proxy, method, args) -> {
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        onDialogAction(args[0]);
                    }
                    return null;
                }
            );

            try {
                Class<?> actionEventClass = ReflectiveJavaFxSupport.loadClass("javafx.event.ActionEvent");
                Object mouseClickedType  = mouseEventClass.getField("MOUSE_CLICKED").get(null);
                Object mousePressedType  = mouseEventClass.getField("MOUSE_PRESSED").get(null);
                Object mouseReleasedType = mouseEventClass.getField("MOUSE_RELEASED").get(null);
                Object keyTypedType      = keyEventClass.getField("KEY_TYPED").get(null);
                Object keyPressedType    = keyEventClass.getField("KEY_PRESSED").get(null);
                Object actionType        = actionEventClass.getField("ACTION").get(null);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, mouseClickedType, mouseEventFilter);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, keyTypedType, keyEventFilter);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, keyPressedType, keyPressFilter);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, mousePressedType, mousePressFilter);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, mouseReleasedType, mouseReleaseFilter);
                scene.getClass().getMethod("addEventFilter", eventTypeClass, eventHandlerClass)
                    .invoke(scene, actionType, dialogActionFilter);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "filter_attach_failed", "message", ex.getMessage()));
            }

            attachColorPickerListeners();
            attachComboBoxListeners();
            attachDatePickerListeners();
            // Track primary scene and any already-open overlay windows
            registeredWindowScenes.clear();
            registeredWindowScenes.add(scene);
            checkAndAttachNewWindows();
            // Animate the window title to indicate recording is active
            try {
                Object window = scene.getClass().getMethod("getWindow").invoke(scene);
                if (window != null) startTitleAnimation(window);
            } catch (Exception ignored) {}
            return ActionResult.success("javafx", null, Map.of(), null);
        });
    }

    /**
     * Starts an animated window-title indicator showing that recording is active.
     * The title blinks between 🔴 and ⚫ frames every 600 ms.
     * Must be called from the JavaFX application thread.
     */
    private void startTitleAnimation(Object window) {
        // Store original title (called on FX thread so getTitle() is safe)
        try {
            Object t = window.getClass().getMethod("getTitle").invoke(window);
            recordingOriginalTitle = t != null ? t.toString() : "";
        } catch (Exception ignored) {
            recordingOriginalTitle = "";
        }
        recordingTitleWindow = window;

        // Animation frames: blinking red / black circle prefix
        String[] frames = {
            recordingOriginalTitle + " \uD83D\uDD34 REC",   // 🔴 REC
            recordingOriginalTitle + " \u26AB REC",          // ⚫ REC
        };
        final int[] idx = {0};
        recordingTitleTimer = new java.util.Timer("omniui-rec-blink", /* daemon */ true);
        recordingTitleTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                String newTitle = frames[idx[0] % frames.length];
                idx[0]++;
                try {
                    Class<?> platform = ReflectiveJavaFxSupport.loadClass("javafx.application.Platform");
                    platform.getMethod("runLater", Runnable.class).invoke(null, (Runnable) () -> {
                        try {
                            window.getClass().getMethod("setTitle", String.class).invoke(window, newTitle);
                        } catch (Exception ignored) {}
                    });
                } catch (Exception ignored) {}
            }
        }, 0, 600);
    }

    /** Cancels the title animation and restores the original window title. */
    private void stopTitleAnimation() {
        if (recordingTitleTimer != null) {
            recordingTitleTimer.cancel();
            recordingTitleTimer = null;
        }
        if (recordingTitleWindow != null) {
            final Object w = recordingTitleWindow;
            final String t = recordingOriginalTitle;
            recordingTitleWindow   = null;
            recordingOriginalTitle = "";
            try {
                // Restore on FX thread (may already be on FX thread when called from stopRecordingFlush,
                // but runLater is always safe)
                Class<?> platform = ReflectiveJavaFxSupport.loadClass("javafx.application.Platform");
                platform.getMethod("runLater", Runnable.class).invoke(null, (Runnable) () -> {
                    try {
                        w.getClass().getMethod("setTitle", String.class).invoke(w, t);
                    } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    /** Attach recording event filters to an arbitrary scene (reuses existing proxy objects). */
    private void attachFiltersToScene(Object scene) {
        if (mouseEventFilter == null) return;
        try {
            Class<?> ehc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventHandler");
            Class<?> mec = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.MouseEvent");
            Class<?> kec = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.KeyEvent");
            Class<?> aec = ReflectiveJavaFxSupport.loadClass("javafx.event.ActionEvent");
            Class<?> etc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventType");
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_CLICKED").get(null),  mouseEventFilter);
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, kec.getField("KEY_TYPED").get(null),      keyEventFilter);
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, kec.getField("KEY_PRESSED").get(null),    keyPressFilter);
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_PRESSED").get(null),  mousePressFilter);
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_RELEASED").get(null), mouseReleaseFilter);
            scene.getClass().getMethod("addEventFilter", etc, ehc).invoke(scene, aec.getField("ACTION").get(null),         dialogActionFilter);
        } catch (Exception ignored) {}
    }

    /** Remove recording event filters from a scene. */
    private void detachFiltersFromScene(Object scene) {
        if (mouseEventFilter == null) return;
        try {
            Class<?> ehc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventHandler");
            Class<?> mec = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.MouseEvent");
            Class<?> kec = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.KeyEvent");
            Class<?> aec = ReflectiveJavaFxSupport.loadClass("javafx.event.ActionEvent");
            Class<?> etc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventType");
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_CLICKED").get(null),  mouseEventFilter);
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, kec.getField("KEY_TYPED").get(null),      keyEventFilter);
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, kec.getField("KEY_PRESSED").get(null),    keyPressFilter);
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_PRESSED").get(null),  mousePressFilter);
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, mec.getField("MOUSE_RELEASED").get(null), mouseReleaseFilter);
            scene.getClass().getMethod("removeEventFilter", etc, ehc).invoke(scene, aec.getField("ACTION").get(null),         dialogActionFilter);
        } catch (Exception ignored) {}
    }

    /**
     * Scan all open windows and attach recording filters + property listeners to any
     * not yet registered. Called via double Platform.runLater after each click so that
     * dialogs opened by button handlers are captured.
     */
    private void checkAndAttachNewWindows() {
        if (mouseEventFilter == null) return;
        for (Object window : getAllWindows()) {
            Object scene = safeInvoke(window, "getScene");
            if (scene == null) continue;
            if (!registeredWindowScenes.add(scene)) continue; // already registered
            attachFiltersToScene(scene);
            Object root = safeInvoke(scene, "getRoot");
            if (root != null) {
                attachComboBoxListenersForRoot(root);
                attachDatePickerListenersForRoot(root);
                if (root.getClass().getSimpleName().equals("DialogPane")) {
                    attachDialogButtonListeners(root);
                }
            }
        }
    }

    /** Directly attach ACTION event handlers to all buttons inside a DialogPane's ButtonBar. */
    private void attachDialogButtonListeners(Object dialogPaneRoot) {
        if (dialogActionFilter == null) return;
        try {
            Object buttonBar = findFirstNodeBySimpleName(dialogPaneRoot, "ButtonBar");
            if (buttonBar == null) return;
            Object buttons = ReflectiveJavaFxSupport.invoke(buttonBar, "getButtons");
            if (!(buttons instanceof java.util.List<?> list) || list.isEmpty()) return;
            Class<?> ehc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventHandler");
            Class<?> aec = ReflectiveJavaFxSupport.loadClass("javafx.event.ActionEvent");
            Class<?> etc = ReflectiveJavaFxSupport.loadClass("javafx.event.EventType");
            Object actionType = aec.getField("ACTION").get(null);
            for (Object btn : list) {
                try {
                    btn.getClass().getMethod("addEventHandler", etc, ehc)
                       .invoke(btn, actionType, dialogActionFilter);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    /** Walk the scene graph and return the first node whose simple class name matches {@code name}. */
    private Object findFirstNodeBySimpleName(Object root, String name) {
        if (root == null) return null;
        if (root.getClass().getSimpleName().equals(name)) return root;
        try {
            Object children = ReflectiveJavaFxSupport.invoke(root, "getChildrenUnmodifiable");
            if (children instanceof java.util.List<?> list) {
                for (Object child : list) {
                    Object found = findFirstNodeBySimpleName(child, name);
                    if (found != null) return found;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Schedule {@code r} to run on the JavaFX Application Thread. */
    private static void runLaterFx(Runnable r) {
        try {
            Class.forName("javafx.application.Platform")
                 .getMethod("runLater", Runnable.class)
                 .invoke(null, r);
        } catch (Exception ignored) {}
    }

    /** Returns true if {@code node} is inside a JavaFX DialogPane (modal dialog). */
    private boolean isInsideDialogPane(Object node) {
        Object current = node;
        for (int i = 0; i < 15 && current != null; i++) {
            if (current.getClass().getName().contains("DialogPane")) return true;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return false;
    }

    /**
     * Returns true if {@code node} is inside a button area of a DialogPane.
     * Matches Button, ButtonBase, or ButtonBar (and their skin containers) anywhere
     * in the ancestor chain that also has a DialogPane ancestor.
     */
    private boolean isInsideDialogButton(Object node) {
        boolean foundButtonArea = false;
        boolean foundDialogPane = false;
        Object current = node;
        for (int i = 0; i < 20 && current != null; i++) {
            String simpleName = current.getClass().getSimpleName();
            String fullName   = current.getClass().getName();
            if (!foundButtonArea && (
                    "Button".equals(simpleName)
                    || "ButtonBase".equals(simpleName)
                    || fullName.contains("ButtonBar"))) {
                foundButtonArea = true;
            }
            if (!foundDialogPane && fullName.contains("DialogPane")) {
                foundDialogPane = true;
            }
            if (foundButtonArea && foundDialogPane) return true;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return false;
    }

    /**
     * Determine the dialog button text for a mouse click event.
     * Strategy:
     *   1. Walk up from node to find a Button ancestor (works when node is inside button skin).
     *   2. Walk up to find ButtonBar, then use click coordinates to hit-test its buttons.
     *   3. Any text in ancestor chain.
     *   4. Default "OK".
     */
    private String getDialogButtonTextFromEvent(Object node, Object event) {
        // Pass 1: Button ancestor
        Object current = node;
        for (int i = 0; i < 20 && current != null; i++) {
            String simpleName = current.getClass().getSimpleName();
            if ("Button".equals(simpleName) || "ButtonBase".equals(simpleName)) {
                String t = nullToEmpty(ReflectiveJavaFxSupport.textOf(current));
                if (!t.isEmpty()) return t;
            }
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        // Pass 2: ButtonBar coordinate hit-test
        try {
            double sceneX = ((Number) ReflectiveJavaFxSupport.invoke(event, "getSceneX")).doubleValue();
            double sceneY = ((Number) ReflectiveJavaFxSupport.invoke(event, "getSceneY")).doubleValue();
            current = node;
            for (int i = 0; i < 20 && current != null; i++) {
                if (current.getClass().getName().contains("ButtonBar")) {
                    String t = hitTestButtonBarButtons(current, sceneX, sceneY);
                    if (!t.isEmpty()) return t;
                    break;
                }
                try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
                catch (Exception ex) { break; }
            }
        } catch (Exception ignored) {}
        // Pass 3: any text in ancestor chain
        current = node;
        for (int i = 0; i < 20 && current != null; i++) {
            String t = nullToEmpty(ReflectiveJavaFxSupport.textOf(current));
            if (!t.isEmpty()) return t;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return "OK";
    }

    /**
     * Walk ButtonBar's button list and return the text of the button whose scene bounds
     * contain (sceneX, sceneY). Returns empty string if none matches.
     */
    @SuppressWarnings("unchecked")
    private String hitTestButtonBarButtons(Object buttonBar, double sceneX, double sceneY) {
        try {
            Object buttons = ReflectiveJavaFxSupport.invoke(buttonBar, "getButtons");
            if (!(buttons instanceof List<?> list)) return "";
            Class<?> boundsClass = ReflectiveJavaFxSupport.loadClass("javafx.geometry.Bounds");
            for (Object btn : list) {
                try {
                    Object localBounds = ReflectiveJavaFxSupport.invoke(btn, "getBoundsInLocal");
                    Object sceneBounds = btn.getClass().getMethod("localToScene", boundsClass)
                                           .invoke(btn, localBounds);
                    double minX = ((Number) ReflectiveJavaFxSupport.invoke(sceneBounds, "getMinX")).doubleValue();
                    double maxX = ((Number) ReflectiveJavaFxSupport.invoke(sceneBounds, "getMaxX")).doubleValue();
                    double minY = ((Number) ReflectiveJavaFxSupport.invoke(sceneBounds, "getMinY")).doubleValue();
                    double maxY = ((Number) ReflectiveJavaFxSupport.invoke(sceneBounds, "getMaxY")).doubleValue();
                    if (sceneX >= minX && sceneX <= maxX && sceneY >= minY && sceneY <= maxY) {
                        String t = nullToEmpty(ReflectiveJavaFxSupport.textOf(btn));
                        if (!t.isEmpty()) return t;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return "";
    }

    /** Walk up from {@code node} to find dialog button text. Used for Enter/Space key path. */
    private String getDialogButtonText(Object node) {
        return getDialogButtonTextFromEvent(node, null);
    }

    /** Scan scene for ColorPicker nodes and register valueProperty change listeners. */
    private void attachColorPickerListeners() {
        try {
            Object scene = sceneSupplier.get();
            if (scene == null) return;
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            if (root == null) return;
            List<Object> pickers = findChildrenByType(root, "ColorPicker");
            Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
            for (Object picker : pickers) {
                Object valueProp = ReflectiveJavaFxSupport.invoke(picker, "valueProperty");
                String fxId = nullToEmpty(safeString(picker, "getId"));
                int idx = nodeIndexOf(picker);
                Object listener = java.lang.reflect.Proxy.newProxyInstance(
                    changeListenerClass.getClassLoader(),
                    new Class<?>[]{ changeListenerClass },
                    (proxy, method, args) -> {
                        if ("changed".equals(method.getName()) && args != null && args.length == 3 && args[2] != null) {
                            try {
                                double r = (double) ReflectiveJavaFxSupport.invokeExact(args[2], "getRed", new Class<?>[0]);
                                double g = (double) ReflectiveJavaFxSupport.invokeExact(args[2], "getGreen", new Class<?>[0]);
                                double b = (double) ReflectiveJavaFxSupport.invokeExact(args[2], "getBlue", new Class<?>[0]);
                                String hex = String.format("#%02x%02x%02x", (int)(r * 255), (int)(g * 255), (int)(b * 255));
                                if (recorderBuffer.size() < MAX_RECORDER_EVENTS) {
                                    Map<String, Object> entry = new LinkedHashMap<>();
                                    entry.put("type",      "set_color");
                                    entry.put("fxId",      fxId);
                                    entry.put("text",      "");
                                    entry.put("nodeType",  "ColorPicker");
                                    entry.put("nodeIndex", idx);
                                    entry.put("timestamp", System.currentTimeMillis() / 1000.0);
                                    entry.put("color",     hex);
                                    recorderBuffer.addLast(entry);
                                }
                            } catch (Exception ignored) {}
                        }
                        return null;
                    }
                );
                ReflectiveJavaFxSupport.invokeExact(valueProp, "addListener",
                    new Class<?>[]{ changeListenerClass }, listener);
                colorPickerListeners.add(new Object[]{ valueProp, listener });
            }
        } catch (Exception ignored) {}
    }

    /** Scan scene for ComboBox nodes and register valueProperty change listeners. */
    private void attachComboBoxListeners() {
        try {
            Object scene = sceneSupplier.get();
            if (scene == null) return;
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            if (root == null) return;
            lastComboValues.clear();
            attachComboBoxListenersForRoot(root);
        } catch (Exception ignored) {}
    }

    /** Attach ComboBox valueProperty listeners to all ComboBoxes under {@code root}. */
    private void attachComboBoxListenersForRoot(Object root) {
        try {
            List<Object> combos = findChildrenByType(root, "ComboBox");
            Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
            for (Object combo : combos) {
                Object valueProp = ReflectiveJavaFxSupport.invoke(combo, "valueProperty");
                String fxId = nullToEmpty(safeString(combo, "getId"));
                int idx = nodeIndexOf(combo);
                Object curVal = ReflectiveJavaFxSupport.invoke(combo, "getValue");
                if (curVal != null) lastComboValues.put(fxId, curVal.toString());
                Object listener = java.lang.reflect.Proxy.newProxyInstance(
                    changeListenerClass.getClassLoader(),
                    new Class<?>[]{ changeListenerClass },
                    (proxy, method, args) -> {
                        if ("changed".equals(method.getName()) && args != null && args.length == 3 && args[2] != null) {
                            try {
                                String selected = args[2].toString();
                                String last = lastComboValues.get(fxId);
                                if (selected.equals(last)) return null;
                                lastComboValues.put(fxId, selected);
                                if (recorderBuffer.size() < MAX_RECORDER_EVENTS) {
                                    Map<String, Object> entry = new LinkedHashMap<>();
                                    entry.put("type",      "select_combo");
                                    entry.put("fxId",      fxId);
                                    entry.put("text",      selected);
                                    entry.put("nodeType",  "ComboBox");
                                    entry.put("nodeIndex", idx);
                                    entry.put("timestamp", System.currentTimeMillis() / 1000.0);
                                    recorderBuffer.addLast(entry);
                                }
                            } catch (Exception ignored) {}
                        }
                        return null;
                    }
                );
                ReflectiveJavaFxSupport.invokeExact(valueProp, "addListener",
                    new Class<?>[]{ changeListenerClass }, listener);
                comboBoxListeners.add(new Object[]{ valueProp, listener });
            }
        } catch (Exception ignored) {}
    }

    /** Scan scene for DatePicker nodes and register valueProperty change listeners. */
    private void attachDatePickerListeners() {
        try {
            Object scene = sceneSupplier.get();
            if (scene == null) return;
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            if (root == null) return;
            attachDatePickerListenersForRoot(root);
        } catch (Exception ignored) {}
    }

    /** Attach DatePicker valueProperty listeners to all DatePickers under {@code root}. */
    private void attachDatePickerListenersForRoot(Object root) {
        try {
            List<Object> pickers = findChildrenByType(root, "DatePicker");
            Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
            for (Object picker : pickers) {
                Object valueProp = ReflectiveJavaFxSupport.invoke(picker, "valueProperty");
                String fxId = nullToEmpty(safeString(picker, "getId"));
                int idx = nodeIndexOf(picker);
                Object listener = java.lang.reflect.Proxy.newProxyInstance(
                    changeListenerClass.getClassLoader(),
                    new Class<?>[]{ changeListenerClass },
                    (proxy, method, args) -> {
                        if ("changed".equals(method.getName()) && args != null && args.length == 3 && args[2] != null) {
                            try {
                                String dateStr = args[2].toString();
                                if (recorderBuffer.size() < MAX_RECORDER_EVENTS) {
                                    Map<String, Object> entry = new LinkedHashMap<>();
                                    entry.put("type",      "set_date");
                                    entry.put("fxId",      fxId);
                                    entry.put("text",      dateStr);
                                    entry.put("nodeType",  "DatePicker");
                                    entry.put("nodeIndex", idx);
                                    entry.put("timestamp", System.currentTimeMillis() / 1000.0);
                                    recorderBuffer.addLast(entry);
                                }
                            } catch (Exception ignored) {}
                        }
                        return null;
                    }
                );
                ReflectiveJavaFxSupport.invokeExact(valueProp, "addListener",
                    new Class<?>[]{ changeListenerClass }, listener);
                datePickerListeners.add(new Object[]{ valueProp, listener });
            }
        } catch (Exception ignored) {}
    }


    @Override
    public List<Map<String, Object>> stopRecordingFlush() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            Object scene = sceneSupplier.get();
            if (scene != null && mouseEventFilter != null) {
                try {
                    Class<?> eventHandlerClass = ReflectiveJavaFxSupport.loadClass("javafx.event.EventHandler");
                    Class<?> mouseEventClass   = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.MouseEvent");
                    Class<?> keyEventClass     = ReflectiveJavaFxSupport.loadClass("javafx.scene.input.KeyEvent");
                    Class<?> actionEventClass  = ReflectiveJavaFxSupport.loadClass("javafx.event.ActionEvent");
                    Class<?> eventTypeClass    = ReflectiveJavaFxSupport.loadClass("javafx.event.EventType");
                    Object mouseClickedType  = mouseEventClass.getField("MOUSE_CLICKED").get(null);
                    Object mousePressedType  = mouseEventClass.getField("MOUSE_PRESSED").get(null);
                    Object mouseReleasedType = mouseEventClass.getField("MOUSE_RELEASED").get(null);
                    Object keyTypedType      = keyEventClass.getField("KEY_TYPED").get(null);
                    Object actionType        = actionEventClass.getField("ACTION").get(null);
                    scene.getClass().getMethod("removeEventFilter", eventTypeClass, eventHandlerClass)
                        .invoke(scene, mouseClickedType, mouseEventFilter);
                    scene.getClass().getMethod("removeEventFilter", eventTypeClass, eventHandlerClass)
                        .invoke(scene, keyTypedType, keyEventFilter);
                    if (mousePressFilter != null) {
                        scene.getClass().getMethod("removeEventFilter", eventTypeClass, eventHandlerClass)
                            .invoke(scene, mousePressedType, mousePressFilter);
                    }
                    if (mouseReleaseFilter != null) {
                        scene.getClass().getMethod("removeEventFilter", eventTypeClass, eventHandlerClass)
                            .invoke(scene, mouseReleasedType, mouseReleaseFilter);
                    }
                    if (dialogActionFilter != null) {
                        scene.getClass().getMethod("removeEventFilter", eventTypeClass, eventHandlerClass)
                            .invoke(scene, actionType, dialogActionFilter);
                    }
                } catch (Exception ignored) { /* best-effort */ }
            }
            mouseEventFilter   = null;
            keyEventFilter     = null;
            mousePressFilter   = null;
            mouseReleaseFilter = null;
            dialogActionFilter = null;
            dragPressId        = "";
            dragPressText      = "";
            dragJustFired      = false;

            // Detach ColorPicker value listeners
            try {
                Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
                for (Object[] pair : colorPickerListeners) {
                    ReflectiveJavaFxSupport.invokeExact(pair[0], "removeListener",
                        new Class<?>[]{ changeListenerClass }, pair[1]);
                }
            } catch (Exception ignored) {}
            colorPickerListeners.clear();

            // Detach ComboBox value listeners
            try {
                Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
                for (Object[] pair : comboBoxListeners) {
                    ReflectiveJavaFxSupport.invokeExact(pair[0], "removeListener",
                        new Class<?>[]{ changeListenerClass }, pair[1]);
                }
            } catch (Exception ignored) {}
            comboBoxListeners.clear();
            lastComboValues.clear();

            // Detach DatePicker value listeners
            try {
                Class<?> changeListenerClass = ReflectiveJavaFxSupport.loadClass("javafx.beans.value.ChangeListener");
                for (Object[] pair : datePickerListeners) {
                    ReflectiveJavaFxSupport.invokeExact(pair[0], "removeListener",
                        new Class<?>[]{ changeListenerClass }, pair[1]);
                }
            } catch (Exception ignored) {}
            datePickerListeners.clear();

            // Detach filters from overlay windows registered during recording
            for (Object regScene : registeredWindowScenes) {
                Object primaryScene = sceneSupplier.get();
                if (regScene == primaryScene) continue;
                detachFiltersFromScene(regScene);
            }
            registeredWindowScenes.clear();

            // Flush any pending key accumulations
            flushKeyAccumulator();
            // Stop title animation and restore original title
            stopTitleAnimation();

            List<Map<String, Object>> events = new ArrayList<>(recorderBuffer);
            recorderBuffer.clear();
            deliveredUpTo.set(0);
            keyAccumulator.clear();
            keyTimestamps.clear();
            return events;
        });
    }

    @Override
    public List<Map<String, Object>> pollEvents() {
        flushKeyAccumulator();
        List<Map<String, Object>> all = new ArrayList<>(recorderBuffer);
        int from = deliveredUpTo.get();
        if (from >= all.size()) return List.of();
        List<Map<String, Object>> slice = new ArrayList<>(all.subList(from, all.size()));
        deliveredUpTo.set(all.size());
        return slice;
    }

    private void onMouseClicked(Object event) {
        if (recorderBuffer.size() >= MAX_RECORDER_EVENTS) return;
        // Suppress click that immediately follows a recorded drag
        if (dragJustFired) { dragJustFired = false; return; }
        // Flush pending key accumulations before recording the click
        flushKeyAccumulator();
        try {
            Object target = ReflectiveJavaFxSupport.invoke(event, "getTarget");
            if (target == null) return;
            Object node = nearestNode(target);
            if (node == null) return;
            String fxId     = nullToEmpty(safeString(node, "getId"));
            String nodeType = node.getClass().getSimpleName();
            // ColorPicker clicks just open the popup; the actual color change is recorded
            // via the valueProperty listener registered in attachColorPickerListeners().
            // Also check ancestor chain: the clicked node may be an internal child of the
            // ColorPicker (e.g. ColorPickerLabel, a Rectangle) rather than the picker itself.
            if (isInsideColorPicker(node)) return;
            // ComboBox clicks (arrow-button etc.) just open the dropdown; the actual selection
            // is recorded via valueProperty listener in attachComboBoxListeners().
            if (isInsideComboBox(node)) return;
            // DatePicker clicks open the calendar popup; the actual date is recorded
            // via valueProperty listener in attachDatePickerListeners().
            if (isInsideDatePicker(node)) return;
            // Dialog button clicks (OK / Cancel) → record as dismiss_dialog.
            // Use isInsideDialogButton (not isInsideDialogPane) to avoid matching
            // non-button nodes (TextField, etc.) that are also inside the dialog.
            if (isInsideDialogButton(node)) {
                String buttonText = getDialogButtonTextFromEvent(node, event);
                Map<String, Object> de = new LinkedHashMap<>();
                de.put("type",      "dismiss_dialog");
                de.put("fxId",      "");
                de.put("text",      buttonText);
                de.put("nodeType",  "Button");
                de.put("nodeIndex", 0);
                de.put("timestamp", System.currentTimeMillis() / 1000.0);
                recorderBuffer.addLast(de);
                return;
            }
            // Skip non-actionable layout nodes (Pane, HBox, VBox, ButtonBar, …).
            // Walk up to find the nearest actionable ancestor; if none, drop the event.
            if (!isActionableNode(node)) {
                Object ancestor = nearestActionableAncestor(node);
                if (ancestor == null) return;
                node     = ancestor;
                fxId     = nullToEmpty(safeString(node, "getId"));
                nodeType = node.getClass().getSimpleName();
            }
            int    nodeIdx  = nodeIndexOf(node);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type",       "click");
            entry.put("fxId",       fxId);
            entry.put("text",       nullToEmpty(ReflectiveJavaFxSupport.textOf(node)));
            entry.put("nodeType",   nodeType);
            entry.put("nodeIndex",  nodeIdx);
            entry.put("timestamp",  System.currentTimeMillis() / 1000.0);
            recorderBuffer.addLast(entry);
            // After every click, schedule a delayed window scan to pick up newly opened dialogs
            runLaterFx(() -> runLaterFx(this::checkAndAttachNewWindows));
        } catch (Exception ignored) { /* best-effort: never crash the app */ }
    }

    /**
     * ActionEvent filter — fires when any action is performed (button click, Enter on button, etc.).
     * We only care about buttons inside a DialogPane (OK / Cancel / custom dialog buttons).
     */
    private void onDialogAction(Object event) {
        if (recorderBuffer.size() >= MAX_RECORDER_EVENTS) return;
        try {
            Object source = ReflectiveJavaFxSupport.invoke(event, "getSource");
            if (source == null) return;
            if (!isInsideDialogButton(source)) return;
            // Deduplicate: ignore if the very last buffered event is already this dismiss_dialog
            // (can happen if both MOUSE_CLICKED and ACTION fire for the same button press)
            if (!recorderBuffer.isEmpty()) {
                Map<String, Object> last = recorderBuffer.peekLast();
                if ("dismiss_dialog".equals(last.get("type"))) return;
            }
            String buttonText = nullToEmpty(ReflectiveJavaFxSupport.textOf(source));
            if (buttonText.isEmpty()) buttonText = "OK";
            Map<String, Object> de = new LinkedHashMap<>();
            de.put("type",      "dismiss_dialog");
            de.put("fxId",      "");
            de.put("text",      buttonText);
            de.put("nodeType",  "Button");
            de.put("nodeIndex", 0);
            de.put("timestamp", System.currentTimeMillis() / 1000.0);
            recorderBuffer.addLast(de);
            // Schedule window scan to pick up any new dialogs opened by the action
            runLaterFx(() -> runLaterFx(this::checkAndAttachNewWindows));
        } catch (Exception ignored) { /* best-effort */ }
    }

    private void onMousePressed(Object event) {
        // Record press info for drag detection (consumed by onMouseReleased)
        dragJustFired = false;
        try {
            Object xObj = ReflectiveJavaFxSupport.invoke(event, "getSceneX");
            Object yObj = ReflectiveJavaFxSupport.invoke(event, "getSceneY");
            dragPressX    = xObj instanceof Number n ? n.doubleValue() : 0;
            dragPressY    = yObj instanceof Number n ? n.doubleValue() : 0;
            dragPressTime = System.currentTimeMillis() / 1000.0;

            Object target = ReflectiveJavaFxSupport.invoke(event, "getTarget");
            Object node   = target != null ? nearestNode(target) : null;
            if (node != null) {
                dragPressId    = nullToEmpty(safeString(node, "getId"));
                dragPressText  = nullToEmpty(ReflectiveJavaFxSupport.textOf(node));
                dragPressType  = node.getClass().getSimpleName();
                dragPressIndex = nodeIndexOf(node);
            } else {
                dragPressId = dragPressText = dragPressType = "";
                dragPressIndex = 0;
            }
        } catch (Exception ignored) { /* best-effort */ }
    }

    private void onMouseReleased(Object event) {
        if (recorderBuffer.size() >= MAX_RECORDER_EVENTS) return;
        try {
            Object xObj = ReflectiveJavaFxSupport.invoke(event, "getSceneX");
            Object yObj = ReflectiveJavaFxSupport.invoke(event, "getSceneY");
            double relX = xObj instanceof Number n ? n.doubleValue() : 0;
            double relY = yObj instanceof Number n ? n.doubleValue() : 0;
            double dist = Math.sqrt(Math.pow(relX - dragPressX, 2) + Math.pow(relY - dragPressY, 2));

            // Only record as drag if movement exceeds threshold
            if (dist < DRAG_MIN_DIST) return;

            // Use PickResult to get the node actually under the cursor at release time.
            // MOUSE_RELEASED target is always the original press node in JavaFX, so we
            // must use getPickResult().getIntersectedNode() for the drop target.
            Object toNode = null;
            try {
                Object pickResult = ReflectiveJavaFxSupport.invoke(event, "getPickResult");
                if (pickResult != null) {
                    Object intersected = ReflectiveJavaFxSupport.invoke(pickResult, "getIntersectedNode");
                    toNode = intersected != null ? nearestNode(intersected) : null;
                }
            } catch (Exception ignored2) { /* best-effort */ }

            // Fallback: use event target (original press node — less accurate for drag-to)
            if (toNode == null) {
                Object target = ReflectiveJavaFxSupport.invoke(event, "getTarget");
                toNode = target != null ? nearestNode(target) : null;
            }

            String toFxId = toNode != null ? nullToEmpty(safeString(toNode, "getId"))           : "";
            String toText = toNode != null ? nullToEmpty(ReflectiveJavaFxSupport.textOf(toNode)) : "";
            String toType = toNode != null ? toNode.getClass().getSimpleName()                   : "";
            int    toIdx  = toNode != null ? nodeIndexOf(toNode)                                 : 0;

            // Flush pending key accumulations before emitting drag
            flushKeyAccumulator();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type",        "drag");
            entry.put("fxId",        dragPressId);
            entry.put("text",        dragPressText);
            entry.put("nodeType",    dragPressType);
            entry.put("nodeIndex",   dragPressIndex);
            entry.put("toFxId",      toFxId);
            entry.put("toText",      toText);
            entry.put("toNodeType",  toType);
            entry.put("toNodeIndex", toIdx);
            entry.put("timestamp",   dragPressTime);
            recorderBuffer.addLast(entry);

            // Signal onMouseClicked to skip the click that JavaFX fires after a drag
            dragJustFired = true;
        } catch (Exception ignored) { /* best-effort */ }
    }

    private void onKeyTyped(Object event) {
        try {
            Object target = ReflectiveJavaFxSupport.invoke(event, "getTarget");
            if (target == null) return;
            Object node = nearestNode(target);
            if (node == null) return;
            String nodeKey = nodeKeyFor(node);
            Object charObj = ReflectiveJavaFxSupport.invoke(event, "getCharacter");
            if (charObj == null) return;
            String ch = charObj.toString();
            if (ch.isEmpty() || ch.charAt(0) < 32) return; // ignore control chars
            keyAccumulator.computeIfAbsent(nodeKey, k -> new StringBuilder()).append(ch);
            keyTimestamps.put(nodeKey, System.currentTimeMillis());
        } catch (Exception ignored) { /* best-effort */ }
    }

    /**
     * Handle KEY_PRESSED events. Only acts on Enter/Space inside a dialog scene,
     * recording dismiss_dialog with the focused button's text. This covers the common
     * case where the user presses Enter rather than clicking the OK/Cancel button.
     */
    private void onKeyPressed(Object event) {
        if (recorderBuffer.size() >= MAX_RECORDER_EVENTS) return;
        try {
            // Only handle Enter and Space
            Object codeObj = ReflectiveJavaFxSupport.invoke(event, "getCode");
            if (codeObj == null) return;
            String code = codeObj.toString();
            if (!"ENTER".equals(code) && !"SPACE".equals(code)) return;

            // Only act inside a dialog scene (scene root contains/is a DialogPane)
            Object source = ReflectiveJavaFxSupport.invoke(event, "getSource");
            if (!isDialogScene(source)) return;

            // Find text from focused node (should be the focused Button)
            String buttonText = "OK";
            try {
                Object focusedNode = ReflectiveJavaFxSupport.invoke(source, "getFocusOwner");
                if (focusedNode != null) {
                    String t = nullToEmpty(ReflectiveJavaFxSupport.textOf(focusedNode));
                    if (!t.isEmpty()) buttonText = t;
                }
            } catch (Exception ignored) {}

            flushKeyAccumulator();
            Map<String, Object> de = new LinkedHashMap<>();
            de.put("type",      "dismiss_dialog");
            de.put("fxId",      "");
            de.put("text",      buttonText);
            de.put("nodeType",  "Button");
            de.put("nodeIndex", 0);
            de.put("timestamp", System.currentTimeMillis() / 1000.0);
            recorderBuffer.addLast(de);
        } catch (Exception ignored) { /* best-effort */ }
    }

    /** Returns true if {@code scene} is a JavaFX scene whose root is (or contains) a DialogPane. */
    private boolean isDialogScene(Object scene) {
        if (scene == null) return false;
        try {
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            return root != null && root.getClass().getName().contains("DialogPane");
        } catch (Exception ex) { return false; }
    }

    private void flushKeyAccumulator() {
        for (Map.Entry<String, StringBuilder> kv : keyAccumulator.entrySet()) {
            if (recorderBuffer.size() >= MAX_RECORDER_EVENTS) break;
            String text = kv.getValue().toString();
            if (text.isEmpty()) continue;
            String nodeKey = kv.getKey();
            // nodeKey is either fxId or a synthetic handle-like string
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type",       "type");
            entry.put("fxId",       nodeKey.startsWith("_handle_") ? "" : nodeKey);
            entry.put("text",       text);
            entry.put("nodeType",   "");
            entry.put("nodeIndex",  0);
            entry.put("timestamp",  keyTimestamps.getOrDefault(nodeKey, System.currentTimeMillis()) / 1000.0);
            recorderBuffer.addLast(entry);
        }
        keyAccumulator.clear();
        keyTimestamps.clear();
    }

    private Object nearestNode(Object target) {
        // Walk up looking for a Node (not a sub-shape)
        Object current = target;
        int limit = 10;
        while (current != null && limit-- > 0) {
            if (current.getClass().getName().startsWith("javafx.scene.")) {
                return current;
            }
            try {
                current = ReflectiveJavaFxSupport.invoke(current, "getParent");
            } catch (Exception ex) {
                break;
            }
        }
        return target;
    }

    private static final java.util.Set<String> ACTIONABLE_TYPES = java.util.Set.of(
        "Button", "ButtonBase", "ToggleButton", "CheckBox", "RadioButton",
        "TextField", "TextArea", "PasswordField",
        "ComboBox", "ChoiceBox", "Slider", "Spinner",
        "DatePicker", "ColorPicker",
        "ListView", "TreeView", "TableView", "TableCell",
        "Hyperlink", "Label", "MenuButton", "MenuItem"
    );

    /** Returns true if {@code node} is a directly interactable control (not a layout container). */
    private boolean isActionableNode(Object node) {
        if (node == null) return false;
        String simple = node.getClass().getSimpleName();
        if (ACTIONABLE_TYPES.contains(simple)) return true;
        // Any node whose class name ends in "Pane" or "Bar" is layout, not actionable
        return !simple.endsWith("Pane") && !simple.endsWith("Bar") && !simple.endsWith("Box");
    }

    /**
     * Walk up the parent chain (up to 15 levels) and return the nearest actionable ancestor,
     * or {@code null} if none is found.
     */
    private Object nearestActionableAncestor(Object node) {
        Object current = node;
        int limit = 15;
        while (current != null && limit-- > 0) {
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
            if (current != null && isActionableNode(current)) return current;
        }
        return null;
    }

    /** Returns true if {@code node} is a ColorPicker or an internal child of one. */
    private boolean isInsideColorPicker(Object node) {
        Object current = node;
        int limit = 12;
        while (current != null && limit-- > 0) {
            if ("ColorPicker".equals(current.getClass().getSimpleName())) return true;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return false;
    }

    /** Returns true if {@code node} is a ComboBox or an internal child of one. */
    private boolean isInsideComboBox(Object node) {
        Object current = node;
        int limit = 20;
        while (current != null && limit-- > 0) {
            // Match ComboBox class or any skin/bridge class containing "ComboBox" in name
            String name = current.getClass().getName();
            if (name.contains("ComboBox")) return true;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return false;
    }

    /** Returns true if {@code node} is a DatePicker or an internal child of one. */
    private boolean isInsideDatePicker(Object node) {
        Object current = node;
        int limit = 20;
        while (current != null && limit-- > 0) {
            String name = current.getClass().getName();
            if (name.contains("DatePicker")) return true;
            try { current = ReflectiveJavaFxSupport.invoke(current, "getParent"); }
            catch (Exception ex) { break; }
        }
        return false;
    }

    private String nodeKeyFor(Object node) {
        String fxId = nullToEmpty(safeString(node, "getId"));
        return fxId.isEmpty() ? "_handle_" + System.identityHashCode(node) : fxId;
    }

    private int nodeIndexOf(Object node) {
        try {
            Object parent = ReflectiveJavaFxSupport.invoke(node, "getParent");
            if (parent == null) return 0;
            List<Object> siblings = childrenOf(parent);
            String nodeType = node.getClass().getSimpleName();
            int idx = 0;
            for (Object sibling : siblings) {
                if (sibling.getClass().getSimpleName().equals(nodeType)) {
                    if (sibling == node) return idx;
                    idx++;
                }
            }
        } catch (Exception ignored) { /* ignore */ }
        return 0;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
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
        // Handle scoped selector: if selector has a "scope" field, restrict lookup to that subtree
        DiscoverySnapshot snapshot;
        JsonObject effectiveSelector = selector;
        if (selector != null && selector.has("scope") && !selector.get("scope").isJsonNull()) {
            JsonObject scopeSel = selector.getAsJsonObject("scope");
            DiscoverySnapshot fullSnapshot = snapshot();
            Optional<NodeRef> scopeRef = resolve(scopeSel, fullSnapshot);
            if (scopeRef.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "scope_not_found"));
            }
            snapshot = snapshotFromRoot(scopeRef.get().node());
            // Strip the "scope" field from the selector before matching
            effectiveSelector = selector.deepCopy();
            effectiveSelector.remove("scope");
        } else {
            snapshot = snapshot();
        }
        Optional<NodeRef> match = resolve(effectiveSelector, snapshot);
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
            case "focus"        -> doFocus(node, fxId, handle);
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
            case "get_image_url" -> {
                Object img = safeInvoke(node, "getImage");
                String url = "";
                if (img != null) {
                    Object u = safeInvoke(img, "getUrl");
                    url = u != null ? u.toString() : "";
                }
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), url);
            }
            case "is_image_loaded" -> {
                Object img = safeInvoke(node, "getImage");
                boolean loaded = false;
                if (img != null) {
                    Object err = safeInvoke(img, "isError");
                    loaded = !Boolean.TRUE.equals(err);
                }
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), loaded);
            }
            case "get_tabs"     -> handleGetTabs(node, fxId, handle);
            case "get_toolbar_items" -> handleGetToolbarItems(node, fxId, handle);
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
            case "get_cell"    -> doGetCell(node, fxId, handle, payload);
            case "click_cell"  -> doClickCell(node, fxId, handle, payload);
            case "edit_cell"   -> doEditCell(node, fxId, handle, payload);
            case "sort_column" -> doSortColumn(node, fxId, handle, payload);
            case "get_scroll_position" -> handleGetScrollPosition(node, fxId, handle);
            case "set_scroll_position" -> handleSetScrollPosition(node, fxId, handle, payload);
            case "get_page" -> handleGetPage(node, fxId, handle);
            case "set_page" -> handleSetPage(node, fxId, handle, payload);
            case "next_page" -> {
                if (node == null || !node.getClass().getSimpleName().equals("Pagination"))
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
                int cur  = ((Number) safeInvoke(node, "getCurrentPageIndex")).intValue();
                int cnt  = ((Number) safeInvoke(node, "getPageCount")).intValue();
                ReflectiveJavaFxSupport.invoke(node, "setCurrentPageIndex", Math.min(cur + 1, cnt - 1));
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), Math.min(cur + 1, cnt - 1));
            }
            case "prev_page" -> {
                if (node == null || !node.getClass().getSimpleName().equals("Pagination"))
                    yield ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
                int cur2 = ((Number) safeInvoke(node, "getCurrentPageIndex")).intValue();
                ReflectiveJavaFxSupport.invoke(node, "setCurrentPageIndex", Math.max(cur2 - 1, 0));
                yield ActionResult.success("javafx", handle, Map.of("fxId", fxId), Math.max(cur2 - 1, 0));
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

    private ActionResult doFocus(Object node, String fxId, String handle) {
        safeInvoke(node, "requestFocus");
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), null);
    }

    private ActionResult doGetFocused() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            Object scene = sceneSupplier.get();
            if (scene == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scene"));
            }
            Object focusOwner = safeInvoke(scene, "getFocusOwner");
            String focusFxId    = focusOwner != null ? safeString(focusOwner, "getId") : null;
            String focusType    = focusOwner != null ? focusOwner.getClass().getSimpleName() : null;
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("fxId",     focusFxId);
            data.put("nodeType", focusType);
            return ActionResult.success("javafx", null, Map.of(), data);
        });
    }

    private ActionResult doGetClipboard() {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            try {
                Class<?> clipboardClass = Class.forName("javafx.scene.input.Clipboard");
                Object clipboard = clipboardClass.getMethod("getSystemClipboard").invoke(null);
                Object text = clipboard.getClass().getMethod("getString").invoke(clipboard);
                String value = text != null ? text.toString() : "";
                return ActionResult.success("javafx", null, Map.of(), value);
            } catch (Exception e) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", e.getMessage()));
            }
        });
    }

    private ActionResult doSetClipboard(JsonObject payload) {
        String text = payload != null && payload.has("text") ? payload.get("text").getAsString() : "";
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            try {
                Class<?> clipboardClass = Class.forName("javafx.scene.input.Clipboard");
                Class<?> contentClass   = Class.forName("javafx.scene.input.ClipboardContent");
                Object clipboard = clipboardClass.getMethod("getSystemClipboard").invoke(null);
                Object content   = contentClass.getDeclaredConstructor().newInstance();
                contentClass.getMethod("putString", String.class).invoke(content, text);
                clipboardClass.getMethod("setContent", java.util.Map.class).invoke(clipboard, content);
                return ActionResult.success("javafx", null, Map.of(), null);
            } catch (Exception e) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", e.getMessage()));
            }
        });
    }

    private ActionResult doClickAt(JsonObject payload) {
        double x = payload != null && payload.has("x") ? payload.get("x").getAsDouble() : 0;
        double y = payload != null && payload.has("y") ? payload.get("y").getAsDouble() : 0;
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            Object scene = sceneSupplier.get();
            if (scene == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_scene"));
            }
            Object root = ReflectiveJavaFxSupport.invoke(scene, "getRoot");
            if (root == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_root"));
            }
            try {
                // Convert scene coordinates to screen coordinates for the MouseEvent
                Object scenePt = safeInvoke(scene, "localToScreen", x, y);
                double screenX = x, screenY = y;
                if (scenePt != null) {
                    Object sx = safeInvoke(scenePt, "getX");
                    Object sy = safeInvoke(scenePt, "getY");
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
                    x, y, screenX, screenY,
                    primary, 1,
                    false, false, false, false,
                    false, false, false,
                    true, false, true,
                    (Object) null
                );

                Class<?> eventCls  = Class.forName("javafx.event.Event");
                Class<?> targetCls = Class.forName("javafx.event.EventTarget");
                java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);
                fireMethod.invoke(null, root, event);

                return ActionResult.success("javafx", null, Map.of(), Map.of("x", x, "y", y));
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of(
                    "reason", "click_at_failed",
                    "message", ex.getMessage() == null ? "" : ex.getMessage()
                ));
            }
        });
    }

    // ── Drag & Drop ──────────────────────────────────────────────────────────

    /**
     * Drag source node to target node by firing MOUSE_PRESSED → MOUSE_DRAGGED (steps) → MOUSE_RELEASED.
     * Both source and target are resolved by selector; target selector is in payload.target.
     */
    private ActionResult doDrag(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            DiscoverySnapshot snap = snapshot();

            Optional<NodeRef> srcRef = resolve(selector, snap);
            if (srcRef.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "source_not_found"));
            }

            JsonObject targetSel = payload != null && payload.has("target") && payload.get("target").isJsonObject()
                ? payload.getAsJsonObject("target") : null;
            if (targetSel == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "missing_target_selector"));
            }
            Optional<NodeRef> dstRef = resolve(targetSel, snap);
            if (dstRef.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "target_not_found"));
            }

            double[] srcScene = nodeCenterInScene(srcRef.get().node());
            double[] dstScene = nodeCenterInScene(dstRef.get().node());
            if (srcScene == null || dstScene == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "bounds_unavailable"));
            }

            String srcFxId = asString(srcRef.get().metadata().get("fxId"));
            String dstFxId = asString(dstRef.get().metadata().get("fxId"));
            try {
                fireDragSequence(srcScene[0], srcScene[1], dstScene[0], dstScene[1]);
                return ActionResult.success("javafx", null,
                    Map.of("sourceFxId", srcFxId == null ? "" : srcFxId,
                           "targetFxId", dstFxId == null ? "" : dstFxId), null);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of(
                    "reason", "drag_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
            }
        });
    }

    /**
     * Drag source node to absolute scene coordinates (to_x, to_y).
     */
    private ActionResult doDragTo(JsonObject selector, JsonObject payload) {
        return ReflectiveJavaFxSupport.onFxThread(() -> {
            DiscoverySnapshot snap = snapshot();
            Optional<NodeRef> srcRef = resolve(selector, snap);
            if (srcRef.isEmpty()) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "source_not_found"));
            }
            double toX = payload != null && payload.has("to_x") ? payload.get("to_x").getAsDouble() : 0;
            double toY = payload != null && payload.has("to_y") ? payload.get("to_y").getAsDouble() : 0;
            double[] srcScene = nodeCenterInScene(srcRef.get().node());
            if (srcScene == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "bounds_unavailable"));
            }
            String srcFxId = asString(srcRef.get().metadata().get("fxId"));
            try {
                fireDragSequence(srcScene[0], srcScene[1], toX, toY);
                return ActionResult.success("javafx", null,
                    Map.of("sourceFxId", srcFxId == null ? "" : srcFxId,
                           "toX", toX, "toY", toY), null);
            } catch (Exception ex) {
                return ActionResult.failure(List.of("javafx"), Map.of(
                    "reason", "drag_to_failed", "message", ex.getMessage() == null ? "" : ex.getMessage()));
            }
        });
    }

    /** Returns [sceneX, sceneY] of the node's centre, or null on failure. */
    private double[] nodeCenterInScene(Object node) {
        Object bounds = safeInvoke(node, "getBoundsInLocal");
        if (bounds == null) return null;
        Object w = safeInvoke(bounds, "getWidth");
        Object h = safeInvoke(bounds, "getHeight");
        double localX = (w instanceof Number nw) ? nw.doubleValue() / 2 : 5;
        double localY = (h instanceof Number nh) ? nh.doubleValue() / 2 : 5;
        Object scenePt = safeInvoke(node, "localToScene", localX, localY);
        if (scenePt == null) return null;
        Object sx = safeInvoke(scenePt, "getX");
        Object sy = safeInvoke(scenePt, "getY");
        if (!(sx instanceof Number) || !(sy instanceof Number)) return null;
        return new double[]{ ((Number) sx).doubleValue(), ((Number) sy).doubleValue() };
    }

    /** Returns [screenX, screenY] from scene coordinates, or falls back to scene coords. */
    private double[] sceneToScreen(double sceneX, double sceneY) {
        Object scene = sceneSupplier.get();
        if (scene == null) return new double[]{ sceneX, sceneY };
        Object screenPt = safeInvoke(scene, "localToScreen", sceneX, sceneY);
        if (screenPt == null) return new double[]{ sceneX, sceneY };
        Object sx = safeInvoke(screenPt, "getX");
        Object sy = safeInvoke(screenPt, "getY");
        if (sx instanceof Number nx && sy instanceof Number ny) {
            return new double[]{ nx.doubleValue(), ny.doubleValue() };
        }
        return new double[]{ sceneX, sceneY };
    }

    /**
     * Fires MOUSE_PRESSED → 5 MOUSE_DRAGGED steps → MOUSE_RELEASED on the scene root,
     * simulating a drag from (fromX, fromY) to (toX, toY) in scene coordinates.
     */
    private void fireDragSequence(double fromX, double fromY, double toX, double toY) throws Exception {
        Object scene = sceneSupplier.get();
        Object root  = scene != null ? ReflectiveJavaFxSupport.invoke(scene, "getRoot") : null;
        if (root == null) throw new IllegalStateException("no scene root");

        Class<?> mouseCls  = Class.forName("javafx.scene.input.MouseEvent");
        Class<?> btnCls    = Class.forName("javafx.scene.input.MouseButton");
        Class<?> pickCls   = Class.forName("javafx.scene.input.PickResult");
        Class<?> etCls     = Class.forName("javafx.event.EventType");
        Class<?> eventCls  = Class.forName("javafx.event.Event");
        Class<?> targetCls = Class.forName("javafx.event.EventTarget");

        Object primary  = btnCls.getField("PRIMARY").get(null);
        Object pressed  = mouseCls.getField("MOUSE_PRESSED").get(null);
        Object dragged  = mouseCls.getField("MOUSE_DRAGGED").get(null);
        Object released = mouseCls.getField("MOUSE_RELEASED").get(null);

        java.lang.reflect.Constructor<?> ctor = mouseCls.getConstructor(
            etCls,
            double.class, double.class, double.class, double.class,
            btnCls, int.class,
            boolean.class, boolean.class, boolean.class, boolean.class,
            boolean.class, boolean.class, boolean.class,
            boolean.class, boolean.class, boolean.class,
            pickCls
        );
        java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);

        // MOUSE_PRESSED at source
        double[] fromScreen = sceneToScreen(fromX, fromY);
        Object evPressed = ctor.newInstance(pressed,
            fromX, fromY, fromScreen[0], fromScreen[1],
            primary, 1,
            false, false, false, false,
            true, false, false,          // primaryButtonDown=true
            false, false, true,
            (Object) null);
        fireMethod.invoke(null, root, evPressed);

        // MOUSE_DRAGGED steps (interpolate in 5 steps)
        int steps = 5;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            double ix = fromX + (toX - fromX) * t;
            double iy = fromY + (toY - fromY) * t;
            double[] iScreen = sceneToScreen(ix, iy);
            Object evDragged = ctor.newInstance(dragged,
                ix, iy, iScreen[0], iScreen[1],
                primary, 1,
                false, false, false, false,
                true, false, false,      // primaryButtonDown=true
                false, false, false,     // stillSincePress=false
                (Object) null);
            fireMethod.invoke(null, root, evDragged);
        }

        // MOUSE_RELEASED at target
        double[] toScreen = sceneToScreen(toX, toY);
        Object evReleased = ctor.newInstance(released,
            toX, toY, toScreen[0], toScreen[1],
            primary, 1,
            false, false, false, false,
            false, false, false,         // primaryButtonDown=false (released)
            false, false, true,
            (Object) null);
        fireMethod.invoke(null, root, evReleased);
    }



    private ActionResult handleDoubleClick(Object node, String fxId, String handle) {
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

    // ---- TableView helpers --------------------------------------------------

    @SuppressWarnings("unchecked")
    private ActionResult doGetCell(Object node, String fxId, String handle, JsonObject payload) {
        int row = payload != null && payload.has("row") ? payload.get("row").getAsInt() : -1;
        int col = payload != null && payload.has("column") ? payload.get("column").getAsInt() : -1;
        Object items = safeInvoke(node, "getItems");
        if (!(items instanceof List<?> rows)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "not_a_table", "fxId", fxId));
        }
        if (row < 0 || row >= rows.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "row_out_of_bounds", "fxId", fxId, "row", row));
        }
        Object columns = safeInvoke(node, "getColumns");
        if (!(columns instanceof List<?> cols)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_columns", "fxId", fxId));
        }
        if (col < 0 || col >= cols.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "column_out_of_bounds", "fxId", fxId, "column", col));
        }
        Object rowItem = rows.get(row);
        Object colObj = cols.get(col);
        Object observable = safeInvoke(colObj, "getCellObservableValue", rowItem);
        if (observable == null) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_cell_value", "fxId", fxId));
        }
        Object cellValue = safeInvoke(observable, "getValue");
        String strValue = cellValue == null ? "" : cellValue.toString();
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), strValue);
    }

    private ActionResult doClickCell(Object node, String fxId, String handle, JsonObject payload) {
        int row = payload != null && payload.has("row") ? payload.get("row").getAsInt() : -1;
        int col = payload != null && payload.has("column") ? payload.get("column").getAsInt() : -1;
        Object items = safeInvoke(node, "getItems");
        if (!(items instanceof List<?> rows)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "not_a_table", "fxId", fxId));
        }
        if (row < 0 || row >= rows.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "row_out_of_bounds", "fxId", fxId, "row", row));
        }
        Object columns = safeInvoke(node, "getColumns");
        if (!(columns instanceof List<?> cols)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_columns", "fxId", fxId));
        }
        if (col < 0 || col >= cols.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "column_out_of_bounds", "fxId", fxId, "column", col));
        }
        // Scroll into view
        safeInvoke(node, "scrollTo", row);
        Object colObj = cols.get(col);
        safeInvoke(node, "scrollToColumn", colObj);
        // Find the visual cell via VirtualFlow
        try {
            Object skin = safeInvoke(node, "getSkin");
            if (skin == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_skin", "fxId", fxId));
            }
            // TableViewSkin children contains VirtualFlow
            Object children = ReflectiveJavaFxSupport.invoke(skin, "getChildren");
            if (!(children instanceof List<?> childList)) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_children", "fxId", fxId));
            }
            Object vflow = null;
            for (Object child : childList) {
                if (child.getClass().getSimpleName().equals("VirtualFlow")) {
                    vflow = child;
                    break;
                }
            }
            if (vflow == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_virtual_flow", "fxId", fxId));
            }
            Object cell = ReflectiveJavaFxSupport.invoke(vflow, "getCell", row);
            if (cell == null) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "cell_not_visible", "fxId", fxId));
            }
            // Get the cell's children (TableCells for each column)
            Object cellChildren = ReflectiveJavaFxSupport.invoke(cell, "getChildrenUnmodifiable");
            if (!(cellChildren instanceof List<?> cellChildList)) {
                return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_cell_children", "fxId", fxId));
            }
            // Find the TableCell matching column index
            Object targetCell = null;
            int cellIdx = 0;
            for (Object c : cellChildList) {
                if (c.getClass().getSimpleName().endsWith("Cell")) {
                    if (cellIdx == col) { targetCell = c; break; }
                    cellIdx++;
                }
            }
            if (targetCell == null) targetCell = cell;
            handleClick(targetCell, fxId, handle, null);
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "row", row, "column", col), null);
        } catch (Exception e) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "click_cell_failed", "error", e.getMessage(), "fxId", fxId));
        }
    }

    @SuppressWarnings("unchecked")
    private ActionResult doEditCell(Object node, String fxId, String handle, JsonObject payload) {
        int row = payload != null && payload.has("row") ? payload.get("row").getAsInt() : -1;
        int col = payload != null && payload.has("column") ? payload.get("column").getAsInt() : -1;
        String value = payload != null && payload.has("value") ? payload.get("value").getAsString() : "";
        Object items = safeInvoke(node, "getItems");
        if (!(items instanceof List<?> rows)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "not_a_table", "fxId", fxId));
        }
        if (row < 0 || row >= rows.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "row_out_of_bounds", "fxId", fxId, "row", row));
        }
        Object columns = safeInvoke(node, "getColumns");
        if (!(columns instanceof List<?> cols)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_columns", "fxId", fxId));
        }
        if (col < 0 || col >= cols.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "column_out_of_bounds", "fxId", fxId, "column", col));
        }
        Object colObj = cols.get(col);
        Object editable = safeInvoke(colObj, "isEditable");
        if (Boolean.FALSE.equals(editable)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "not_editable", "fxId", fxId, "column", col));
        }
        // Scroll and enter edit mode
        safeInvoke(node, "scrollTo", row);
        safeInvoke(node, "scrollToColumn", colObj);
        safeInvoke(node, "edit", row, colObj);
        // Find the inline editor: the focused node in the scene should be the TextField
        try {
            Object scene = sceneSupplier.get();
            if (scene != null) {
                Object focusOwner = safeInvoke(scene, "getFocusOwner");
                if (focusOwner != null && "TextField".equals(focusOwner.getClass().getSimpleName())) {
                    safeInvoke(focusOwner, "setText", value);
                    safeInvoke(focusOwner, "selectAll");
                }
            }
            // Fire Enter key to commit
            fireEnterKey(node);
        } catch (Exception e) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "edit_cell_failed", "error", e.getMessage(), "fxId", fxId));
        }
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "row", row, "column", col), value);
    }

    private void fireEnterKey(Object target) {
        try {
            Class<?> keyCls    = Class.forName("javafx.scene.input.KeyEvent");
            Class<?> keyCodeCls = Class.forName("javafx.scene.input.KeyCode");
            Class<?> etCls     = Class.forName("javafx.event.EventType");
            Object keyPressed  = keyCls.getField("KEY_PRESSED").get(null);
            Object enterCode   = keyCodeCls.getField("ENTER").get(null);
            java.lang.reflect.Constructor<?> ctor = keyCls.getConstructor(
                Object.class, String.class, String.class, keyCodeCls,
                boolean.class, boolean.class, boolean.class, boolean.class);
            Object pressEvent = ctor.newInstance(target, "\r", "\r", enterCode,
                false, false, false, false);
            Class<?> eventCls  = Class.forName("javafx.event.Event");
            Class<?> targetCls = Class.forName("javafx.event.EventTarget");
            java.lang.reflect.Method fireMethod = eventCls.getMethod("fireEvent", targetCls, eventCls);
            fireMethod.invoke(null, target, pressEvent);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private ActionResult doSortColumn(Object node, String fxId, String handle, JsonObject payload) {
        int col = payload != null && payload.has("column") ? payload.get("column").getAsInt() : -1;
        String direction = payload != null && payload.has("direction") && !payload.get("direction").isJsonNull()
            ? payload.get("direction").getAsString() : null;
        Object columns = safeInvoke(node, "getColumns");
        if (!(columns instanceof List<?> cols)) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "no_columns", "fxId", fxId));
        }
        if (col < 0 || col >= cols.size()) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "column_out_of_bounds", "fxId", fxId, "column", col));
        }
        Object colObj = cols.get(col);
        // Find the column header node and click it
        // Use TableView's sort() infrastructure: add to sortOrder and toggle sort type
        Object sortOrder = safeInvoke(node, "getSortOrder");
        if (direction == null) {
            // Single click: use JavaFX sort API
            if (sortOrder instanceof List<?> so && !so.contains(colObj)) {
                ((java.util.List<Object>) so).clear();
                ((java.util.List<Object>) so).add(colObj);
            }
            safeInvoke(node, "sort");
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "column", col), null);
        }
        // Set desired sort type
        boolean wantAsc = "asc".equalsIgnoreCase(direction);
        Object ascending;
        Object descending;
        try {
            Class<?> sortType = Class.forName("javafx.scene.control.TableColumn$SortType");
            ascending  = sortType.getField("ASCENDING").get(null);
            descending = sortType.getField("DESCENDING").get(null);
        } catch (Exception e) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "sort_type_not_found"));
        }
        Object desired = wantAsc ? ascending : descending;
        safeInvoke(colObj, "setSortType", desired);
        if (sortOrder instanceof List<?> so) {
            ((java.util.List<Object>) so).clear();
            ((java.util.List<Object>) so).add(colObj);
        }
        safeInvoke(node, "sort");
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId, "column", col, "direction", direction), direction);
    }

    private com.google.gson.JsonObject buildPayload(String json) {
        return com.google.gson.JsonParser.parseString(json).getAsJsonObject();
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
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

        // Walk primary scene
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

        // Also walk overlay windows (dialogs, popups, etc.) so actions can target dialog nodes
        Object primaryWindow = safeInvoke(scene, "getWindow");
        for (Object window : getAllWindows()) {
            if (window == primaryWindow) continue;
            Object overlayScene = safeInvoke(window, "getScene");
            if (overlayScene == null) continue;
            Object overlayRoot = safeInvoke(overlayScene, "getRoot");
            if (overlayRoot == null) continue;
            stack.push(new TraversalFrame(overlayRoot, "/Overlay"));
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
        }

        return new DiscoverySnapshot(nodes);
    }

    private DiscoverySnapshot snapshotFromRoot(Object root) {
        if (root == null) return new DiscoverySnapshot(List.of());
        IdentityHashMap<Object, String> handles = new IdentityHashMap<>();
        AtomicInteger counter = new AtomicInteger();
        List<NodeRef> nodes = new ArrayList<>();
        Deque<TraversalFrame> stack = new ArrayDeque<>();
        stack.push(new TraversalFrame(root, "/Scope"));
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

    // ── Window / Stage helpers ────────────────────────────────────────────────

    private Object findStageByTitle(String title) {
        try {
            Class<?> windowClass = Class.forName("javafx.stage.Window");
            Object windows = windowClass.getMethod("getWindows").invoke(null);
            java.util.List<?> list = (java.util.List<?>) windows;
            for (Object w : list) {
                if (!"Stage".equals(w.getClass().getSimpleName())) continue;
                Object t = w.getClass().getMethod("getTitle").invoke(w);
                if (title == null || title.equals(t)) return w;
                if (title.equals(t != null ? t.toString() : null)) return w;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ActionResult handleGetWindows() {
        try {
            Class<?> windowClass = Class.forName("javafx.stage.Window");
            Object windows = windowClass.getMethod("getWindows").invoke(null);
            java.util.List<?> list = (java.util.List<?>) windows;
            List<String> titles = new ArrayList<>();
            for (Object w : list) {
                if (!"Stage".equals(w.getClass().getSimpleName())) continue;
                Object t = safeInvoke(w, "getTitle");
                titles.add(t != null ? t.toString() : "");
            }
            return ActionResult.success("javafx", null, Map.of(), titles);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "get_windows_failed", "error", ex.getMessage()));
        }
    }

    private ActionResult handleWindowAction(String action, JsonObject payload) {
        String title = payload != null && payload.has("title") ? payload.get("title").getAsString() : null;
        Object stage = findStageByTitle(title);
        if (stage == null) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "window_not_found", "title", title != null ? title : ""));
        }
        try {
            return switch (action) {
                case "focus_window"    -> { ReflectiveJavaFxSupport.invoke(stage, "toFront"); yield ActionResult.success("javafx", null, Map.of("title", title), null); }
                case "maximize_window" -> { ReflectiveJavaFxSupport.invoke(stage, "setMaximized", true);  yield ActionResult.success("javafx", null, Map.of("title", title), null); }
                case "minimize_window" -> { ReflectiveJavaFxSupport.invoke(stage, "setIconified", true);  yield ActionResult.success("javafx", null, Map.of("title", title), null); }
                case "restore_window"  -> {
                    ReflectiveJavaFxSupport.invoke(stage, "setMaximized", false);
                    ReflectiveJavaFxSupport.invoke(stage, "setIconified", false);
                    yield ActionResult.success("javafx", null, Map.of("title", title), null);
                }
                case "is_maximized" -> { Object v = safeInvoke(stage, "isMaximized"); yield ActionResult.success("javafx", null, Map.of("title", title), v instanceof Boolean b ? b : false); }
                case "is_minimized" -> { Object v = safeInvoke(stage, "isIconified"); yield ActionResult.success("javafx", null, Map.of("title", title), v instanceof Boolean b ? b : false); }
                case "set_window_size" -> {
                    double w = payload.has("width")  ? payload.get("width").getAsDouble()  : 0;
                    double h = payload.has("height") ? payload.get("height").getAsDouble() : 0;
                    ReflectiveJavaFxSupport.invoke(stage, "setWidth",  w);
                    ReflectiveJavaFxSupport.invoke(stage, "setHeight", h);
                    yield ActionResult.success("javafx", null, Map.of("title", title), null);
                }
                case "set_window_position" -> {
                    double x = payload.has("x") ? payload.get("x").getAsDouble() : 0;
                    double y = payload.has("y") ? payload.get("y").getAsDouble() : 0;
                    ReflectiveJavaFxSupport.invoke(stage, "setX", x);
                    ReflectiveJavaFxSupport.invoke(stage, "setY", y);
                    yield ActionResult.success("javafx", null, Map.of("title", title), null);
                }
                case "get_window_size" -> {
                    Object w = safeInvoke(stage, "getWidth");
                    Object h = safeInvoke(stage, "getHeight");
                    Map<String, Object> sz = new LinkedHashMap<>();
                    sz.put("width",  w instanceof Number n ? n.doubleValue() : 0.0);
                    sz.put("height", h instanceof Number n ? n.doubleValue() : 0.0);
                    yield ActionResult.success("javafx", null, Map.of("title", title), sz);
                }
                case "get_window_position" -> {
                    Object x = safeInvoke(stage, "getX");
                    Object y = safeInvoke(stage, "getY");
                    Map<String, Object> pos = new LinkedHashMap<>();
                    pos.put("x", x instanceof Number n ? n.doubleValue() : 0.0);
                    pos.put("y", y instanceof Number n ? n.doubleValue() : 0.0);
                    yield ActionResult.success("javafx", null, Map.of("title", title), pos);
                }
                default -> ActionResult.failure(List.of("javafx"), Map.of("reason", "unsupported_window_action", "action", action));
            };
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "window_action_failed", "action", action, "error", ex.getMessage()));
        }
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

    private ActionResult handleGetToolbarItems(Object node, String fxId, String handle) {
        try {
            java.util.List<?> items = (java.util.List<?>) ReflectiveJavaFxSupport.invoke(node, "getItems");
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : items) {
                Map<String, Object> desc = new LinkedHashMap<>();
                desc.put("type", item.getClass().getSimpleName());
                Object id2 = safeInvoke(item, "getId");
                desc.put("fxId", id2 == null ? null : id2.toString());
                Object txt = safeInvoke(item, "getText");
                desc.put("text", txt == null ? "" : txt.toString());
                Object dis = safeInvoke(item, "isDisabled");
                desc.put("disabled", dis instanceof Boolean b ? b : false);
                result.add(desc);
            }
            return ActionResult.success("javafx", handle, Map.of("fxId", fxId), result);
        } catch (Exception ex) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "get_toolbar_items_failed", "error", ex.getMessage()));
        }
    }

    private ActionResult handleGetPage(Object node, String fxId, String handle) {
        if (node == null || !node.getClass().getSimpleName().equals("Pagination")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
        }
        int page  = ((Number) safeInvoke(node, "getCurrentPageIndex")).intValue();
        int count = ((Number) safeInvoke(node, "getPageCount")).intValue();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("page_count", count);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), result);
    }

    private ActionResult handleSetPage(Object node, String fxId, String handle, JsonObject payload) {
        if (node == null || !node.getClass().getSimpleName().equals("Pagination")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
        }
        int requested = payload != null && payload.has("page") ? payload.get("page").getAsInt() : 0;
        int count = ((Number) safeInvoke(node, "getPageCount")).intValue();
        int clamped = Math.max(0, Math.min(count - 1, requested));
        ReflectiveJavaFxSupport.invoke(node, "setCurrentPageIndex", clamped);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), clamped);
    }

    private ActionResult handleGetScrollPosition(Object node, String fxId, String handle) {
        if (node == null || !node.getClass().getSimpleName().equals("ScrollBar")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
        }
        Object val = safeInvoke(node, "getValue");
        Object min = safeInvoke(node, "getMin");
        Object max = safeInvoke(node, "getMax");
        Map<String, Object> pos = new LinkedHashMap<>();
        pos.put("value", val instanceof Number n ? n.doubleValue() : 0.0);
        pos.put("min",   min instanceof Number n ? n.doubleValue() : 0.0);
        pos.put("max",   max instanceof Number n ? n.doubleValue() : 100.0);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), pos);
    }

    private ActionResult handleSetScrollPosition(Object node, String fxId, String handle, JsonObject payload) {
        if (node == null || !node.getClass().getSimpleName().equals("ScrollBar")) {
            return ActionResult.failure(List.of("javafx"), Map.of("reason", "action_not_supported", "fxId", fxId));
        }
        double rawValue = payload != null && payload.has("value") ? payload.get("value").getAsDouble() : 0.0;
        Object minObj = safeInvoke(node, "getMin");
        Object maxObj = safeInvoke(node, "getMax");
        double minVal = minObj instanceof Number n ? n.doubleValue() : 0.0;
        double maxVal = maxObj instanceof Number n ? n.doubleValue() : 100.0;
        double clamped = Math.max(minVal, Math.min(maxVal, rawValue));
        ReflectiveJavaFxSupport.invoke(node, "setValue", clamped);
        return ActionResult.success("javafx", handle, Map.of("fxId", fxId), clamped);
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
