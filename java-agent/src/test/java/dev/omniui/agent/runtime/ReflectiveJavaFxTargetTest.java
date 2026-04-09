package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectiveJavaFxTargetTest {

    @AfterEach
    void resetWindowSupplier() {
        ReflectiveJavaFxTarget.resetWindowSupplierForTest();
    }

    @Test
    void resolveMatchesByIdTypeAndIndex() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        Object snapshot = discoverySnapshot(
            nodeRef(new ButtonNode("loginBtn", "Login"), metadata("node-1", "loginBtn", "Button", "Login", "/Scene/Button[1]")),
            nodeRef(new ButtonNode("otherBtn", "Login"), metadata("node-2", "otherBtn", "Button", "Login", "/Scene/Button[2]")),
            nodeRef(new LabelNode("status", "Ready"), metadata("node-3", "status", "Label", "Ready", "/Scene/Label[1]"))
        );
        Class<?>[] resolveParams = new Class<?>[]{JsonObject.class, snapshot.getClass()};

        Optional<?> byId = castOptional(invokePrivate(target, "resolve", resolveParams, selector("id", "loginBtn"), snapshot));
        Optional<?> byTypeText = castOptional(invokePrivate(target, "resolve", resolveParams, selector(Map.of("type", "Button", "text", "Login", "index", 1)), snapshot));
        Optional<?> byTypeOnly = castOptional(invokePrivate(target, "resolve", resolveParams, selector(Map.of("type", "Label")), snapshot));
        Optional<?> empty = castOptional(invokePrivate(target, "resolve", resolveParams, null, snapshot));

        assertTrue(byId.isPresent());
        assertEquals("loginBtn", nodeMetadata(byId.get()).get("fxId"));

        assertTrue(byTypeText.isPresent());
        assertEquals("otherBtn", nodeMetadata(byTypeText.get()).get("fxId"));

        assertTrue(byTypeOnly.isPresent());
        assertEquals("status", nodeMetadata(byTypeOnly.get()).get("fxId"));

        assertFalse(empty.isPresent());
    }

    @Test
    void snapshotFromRootBuildsMetadataAndPaths() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        ParentNode root = new ParentNode("root", "Root");
        ButtonNode child = new ButtonNode("loginBtn", "Login");
        root.addChild(child);

        Object snapshot = invokePrivate(target, "snapshotFromRoot", new Class<?>[]{Object.class}, root);
        List<?> nodes = snapshotNodes(snapshot);

        assertEquals(2, nodes.size());

        Map<String, Object> rootMetadata = nodeMetadata(nodes.get(0));
        assertEquals("root", rootMetadata.get("fxId"));
        assertEquals("/Scope", rootMetadata.get("hierarchyPath"));
        assertEquals("ParentNode", rootMetadata.get("nodeType"));
        assertEquals("Root", rootMetadata.get("text"));
        assertEquals(true, rootMetadata.get("visible"));
        assertEquals(true, rootMetadata.get("enabled"));

        Map<String, Object> childMetadata = nodeMetadata(nodes.get(1));
        assertEquals("loginBtn", childMetadata.get("fxId"));
        assertEquals("/Scope/ButtonNode[1]", childMetadata.get("hierarchyPath"));
        assertEquals("ButtonNode", childMetadata.get("nodeType"));
    }

    @Test
    void handleSetSliderValidatesRangeAndUpdatesValue() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        SliderNode slider = new SliderNode(0.0, 10.0);

        ActionResult missing = invokeAction(target, "handleSetSlider", slider, "volume", "node-1", new JsonObject());
        ActionResult outOfRange = invokeAction(target, "handleSetSlider", slider, "volume", "node-1", selector("value", 12.0));
        ActionResult success = invokeAction(target, "handleSetSlider", slider, "volume", "node-1", selector("value", 7.5));

        assertFalse(missing.ok());
        assertEquals("missing_value", traceReason(missing));

        assertFalse(outOfRange.ok());
        assertEquals("value_out_of_range", traceReason(outOfRange));

        assertTrue(success.ok());
        assertEquals(7.5, slider.getValue());
        assertEquals(7.5, success.value());
    }

    @Test
    void handleSetSpinnerCoversNoFactorySuccessAndFailure() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);

        SpinnerNode noFactory = new SpinnerNode(null);
        ActionResult missingFactory = invokeAction(target, "handleSetSpinner", noFactory, "spinner", "node-1", selector("value", "42"));
        assertFalse(missingFactory.ok());
        assertEquals("no_value_factory", traceReason(missingFactory));

        SpinnerValueFactory factory = new SpinnerValueFactory(new IntegerConverter());
        SpinnerNode okNode = new SpinnerNode(factory);
        ActionResult success = invokeAction(target, "handleSetSpinner", okNode, "spinner", "node-2", selector("value", "21"));
        assertTrue(success.ok());
        assertEquals(21, factory.getValue());
        assertEquals("21", success.value());

        SpinnerNode badNode = new SpinnerNode(new SpinnerValueFactory(new FailingConverter()));
        ActionResult failure = invokeAction(target, "handleSetSpinner", badNode, "spinner", "node-3", selector("value", "boom"));
        assertFalse(failure.ok());
        assertEquals("set_spinner_failed", traceReason(failure));
    }

    @Test
    void handleStepSpinnerSupportsIncrementAndDecrement() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        StepSpinnerNode spinner = new StepSpinnerNode();

        ActionResult missing = invokeAction(target, "handleStepSpinner", spinner, "spinner", "node-1", new JsonObject());
        ActionResult increment = invokeAction(target, "handleStepSpinner", spinner, "spinner", "node-1", selector("steps", 3));
        ActionResult decrement = invokeAction(target, "handleStepSpinner", spinner, "spinner", "node-1", selector("steps", -2));

        assertFalse(missing.ok());
        assertEquals("missing_steps", traceReason(missing));

        assertTrue(increment.ok());
        assertEquals(3, spinner.incrementCalls);

        assertTrue(decrement.ok());
        assertEquals(2, spinner.decrementCalls);
    }

    @Test
    void pageHandlersValidateTypeReadAndClampRequestedPage() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);

        ActionResult unsupported = invokeAction(target, "handleGetPage", new Object(), "pager", "node-1");
        assertFalse(unsupported.ok());
        assertEquals("action_not_supported", traceReason(unsupported));

        Pagination pagination = new Pagination(2, 5);
        ActionResult getPage = invokeAction(target, "handleGetPage", pagination, "pager", "node-2");
        assertTrue(getPage.ok());
        Map<?, ?> pageInfo = assertInstanceOf(Map.class, getPage.value());
        assertEquals(2, pageInfo.get("page"));
        assertEquals(5, pageInfo.get("page_count"));

        ActionResult setPage = invokeAction(target, "handleSetPage", pagination, "pager", "node-2", selector("page", 99));
        assertTrue(setPage.ok());
        assertEquals(4, pagination.currentPageIndex);
        assertEquals(4, setPage.value());
    }

    @Test
    void scrollBarHandlersReadAndClampValues() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);

        ActionResult unsupported = invokeAction(target, "handleGetScrollPosition", new Object(), "scroll", "node-1");
        assertFalse(unsupported.ok());
        assertEquals("action_not_supported", traceReason(unsupported));

        ScrollBar scrollBar = new ScrollBar(5.0, 0.0, 10.0);
        ActionResult getPosition = invokeAction(target, "handleGetScrollPosition", scrollBar, "scroll", "node-2");
        assertTrue(getPosition.ok());
        Map<?, ?> pos = assertInstanceOf(Map.class, getPosition.value());
        assertEquals(5.0, pos.get("value"));
        assertEquals(0.0, pos.get("min"));
        assertEquals(10.0, pos.get("max"));

        ActionResult setPosition = invokeAction(target, "handleSetScrollPosition", scrollBar, "scroll", "node-2", selector("value", 99.0));
        assertTrue(setPosition.ok());
        assertEquals(10.0, scrollBar.value);
        assertEquals(10.0, setPosition.value());
    }

    @Test
    void treeHelpersFindNestedItemsAndHandleMissingChildren() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        TreeItemNode leaf = new TreeItemNode("Leaf");
        TreeItemNode branch = new TreeItemNode("Branch", leaf);
        TreeItemNode root = new TreeItemNode("Root", branch);

        Object found = invokePrivate(target, "findTreeItem", new Class<?>[]{Object.class, String.class}, root, "Leaf");
        Object missing = invokePrivate(target, "findTreeItem", new Class<?>[]{Object.class, String.class}, new TreeItemWithoutChildren("Solo"), "Leaf");

        assertEquals(leaf, found);
        assertNull(missing);
    }

    @Test
    void treeTableHelpersMatchByValueColumnAndFallbackGetter() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        TreeTableColumn nameColumn = new TreeTableColumn("Name", item -> ((PersonRow) item.getValue()).name());
        TreeTableColumn roleColumn = new TreeTableColumn("Role", item -> ((PersonRow) item.getValue()).role());
        TreeTableView treeTable = new TreeTableView(List.of(nameColumn, roleColumn));

        TreeTableItemNode root = new TreeTableItemNode(new PersonRow("Root", "Container"));
        TreeTableItemNode alice = new TreeTableItemNode(new PersonRow("Alice", "Admin"));
        TreeTableItemNode bob = new TreeTableItemNode(new PersonRow("Bob", "User"));
        root.addChild(alice);
        root.addChild(bob);
        treeTable.setRoot(root);

        Object byValue = invokePrivate(target, "findTreeTableItem", new Class<?>[]{Object.class, String.class}, root, "Bob / User");
        Object byCell = invokePrivate(target, "findTreeTableItemByCell", new Class<?>[]{Object.class, Object.class, String.class, String.class}, treeTable, root, "Admin", "Role");
        Object byFallbackGetter = invokePrivate(target, "findTreeTableItemByCell", new Class<?>[]{Object.class, Object.class, String.class, String.class}, treeTable, root, "Bob", "");
        Object missing = invokePrivate(target, "findTreeTableItemByCell", new Class<?>[]{Object.class, Object.class, String.class, String.class}, treeTable, root, "Missing", "Role");

        assertEquals(bob, byValue);
        assertEquals(alice, byCell);
        assertEquals(bob, byFallbackGetter);
        assertNull(missing);
    }

    @Test
    void getTreeTableCellReturnsRequestedColumnValue() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        TreeTableColumn nameColumn = new TreeTableColumn("Name", item -> ((PersonRow) item.getValue()).name());
        TreeTableColumn roleColumn = new TreeTableColumn("Role", item -> ((PersonRow) item.getValue()).role());
        TreeTableView treeTable = new TreeTableView(List.of(nameColumn, roleColumn));

        TreeTableItemNode root = new TreeTableItemNode(new PersonRow("Root", "Container"));
        TreeTableItemNode alice = new TreeTableItemNode(new PersonRow("Alice", "Admin"));
        root.addChild(alice);
        treeTable.setRoot(root);

        String role = assertInstanceOf(
            String.class,
            invokePrivate(target, "getTreeTableCell", new Class<?>[]{Object.class, String.class, String.class}, treeTable, "Alice", "Role")
        );
        Object missing = invokePrivate(target, "getTreeTableCell", new Class<?>[]{Object.class, String.class, String.class}, treeTable, "Alice", "State");

        assertEquals("Admin", role);
        assertNull(missing);
    }

    @Test
    void tableHelpersMatchRowsAndLocateIdentityIndex() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        PersonRow alice = new PersonRow("Alice", "Admin");
        PersonRow bob = new PersonRow("Bob", "User");
        List<PersonRow> rows = List.of(alice, bob);

        boolean byToString = (boolean) invokePrivate(target, "rowMatches", new Class<?>[]{Object.class, String.class, String.class}, alice, "Alice / Admin", null);
        boolean byProperty = (boolean) invokePrivate(target, "rowMatches", new Class<?>[]{Object.class, String.class, String.class}, bob, "User", "role");
        boolean byFallbackProperty = (boolean) invokePrivate(target, "rowMatches", new Class<?>[]{Object.class, String.class, String.class}, alice, "Alice", null);
        boolean missing = (boolean) invokePrivate(target, "rowMatches", new Class<?>[]{Object.class, String.class, String.class}, bob, "Missing", null);

        Integer aliceIndex = assertInstanceOf(
            Integer.class,
            invokePrivate(target, "findRowIndex", new Class<?>[]{java.util.Collection.class, Object.class}, rows, alice)
        );
        Object absentIndex = invokePrivate(target, "findRowIndex", new Class<?>[]{java.util.Collection.class, Object.class}, rows, new PersonRow("Bob", "User"));

        assertTrue(byToString);
        assertTrue(byProperty);
        assertTrue(byFallbackProperty);
        assertFalse(missing);
        assertEquals(0, aliceIndex);
        assertNull(absentIndex);
    }

    @Test
    void menuHelpersFindItemsByTextIdAndPath() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        MenuItemNode open = new MenuItemNode("Open", "openItem");
        MenuItemNode export = new MenuItemNode("Export", "exportItem");
        MenuItemNode file = new MenuItemNode("File", "fileMenu", open, export);
        MenuItemNode edit = new MenuItemNode("Edit", "editMenu");
        List<MenuItemNode> items = List.of(file, edit);

        Object byText = invokePrivate(target, "findMenuItemInList", new Class<?>[]{List.class, String.class, String.class}, items, "Edit", null);
        Object byId = invokePrivate(target, "findMenuItemInList", new Class<?>[]{List.class, String.class, String.class}, items, null, "fileMenu");
        Object byPath = invokePrivate(target, "findMenuItemByPath", new Class<?>[]{Object.class, String[].class}, new ContextMenuWindow(items), new String[]{"File", "Export"});
        Object missing = invokePrivate(target, "findMenuItemByPath", new Class<?>[]{Object.class, String[].class}, new ContextMenuWindow(items), new String[]{"File", "Missing"});

        assertEquals(edit, byText);
        assertEquals(file, byId);
        assertEquals(export, byPath);
        assertNull(missing);
    }

    @Test
    void dialogHelpersBuildDescriptorAndRecognizePane() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        ButtonBarNode buttonBar = new ButtonBarNode(ok, cancel);
        DialogPaneNode dialogPane = new DialogPaneNode("Confirm", "Proceed?", new AlertDialog("CONFIRMATION"), buttonBar);
        DialogWindow window = new DialogWindow("Delete File");

        @SuppressWarnings("unchecked")
        Map<String, Object> descriptor = (Map<String, Object>) invokePrivate(
            target,
            "buildDialogDescriptor",
            new Class<?>[]{Object.class, Object.class},
            window,
            dialogPane
        );
        boolean pane = (boolean) invokePrivate(target, "isDialogPane", new Class<?>[]{Object.class}, dialogPane);
        boolean notPane = (boolean) invokePrivate(target, "isDialogPane", new Class<?>[]{Object.class}, buttonBar);

        assertEquals("Delete File", descriptor.get("title"));
        assertEquals("Confirm", descriptor.get("header"));
        assertEquals("Proceed?", descriptor.get("content"));
        assertEquals("CONFIRMATION", descriptor.get("alertType"));
        assertEquals(List.of("OK", "Cancel"), descriptor.get("buttons"));
        assertTrue(pane);
        assertFalse(notPane);
    }

    @Test
    void dialogButtonTextFallsBackToAncestorTextOrDefault() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        Button ok = new Button("OK");
        ButtonGraphic graphic = new ButtonGraphic();
        ok.addChild(graphic);

        String fromAncestor = assertInstanceOf(
            String.class,
            invokePrivate(target, "getDialogButtonText", new Class<?>[]{Object.class}, graphic)
        );
        String defaultLabel = assertInstanceOf(
            String.class,
            invokePrivate(target, "getDialogButtonText", new Class<?>[]{Object.class}, new ParentNode("plain", ""))
        );

        assertEquals("OK", fromAncestor);
        assertEquals("OK", defaultLabel);
    }

    @Test
    void windowHelpersListTitlesAndHandleStateActions() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        Stage primary = new Stage("Primary");
        Stage secondary = new Stage("Secondary");
        PopupWindow popup = new PopupWindow("Popup");
        ReflectiveJavaFxTarget.setWindowSupplierForTest(() -> List.of(primary, secondary, popup));

        ActionResult getWindows = assertInstanceOf(
            ActionResult.class,
            invokePrivate(target, "handleGetWindows")
        );
        ActionResult maximize = assertInstanceOf(
            ActionResult.class,
            invokePrivate(target, "handleWindowAction", new Class<?>[]{String.class, JsonObject.class}, "maximize_window", selector("title", "Primary"))
        );
        ActionResult size = assertInstanceOf(
            ActionResult.class,
            invokePrivate(target, "handleWindowAction", new Class<?>[]{String.class, JsonObject.class}, "get_window_size", selector("title", "Primary"))
        );
        ActionResult position = assertInstanceOf(
            ActionResult.class,
            invokePrivate(target, "handleWindowAction", new Class<?>[]{String.class, JsonObject.class}, "set_window_position", selector(Map.of("title", "Primary", "x", 32.5, "y", 48.0)))
        );
        ActionResult missing = assertInstanceOf(
            ActionResult.class,
            invokePrivate(target, "handleWindowAction", new Class<?>[]{String.class, JsonObject.class}, "focus_window", selector("title", "Missing"))
        );

        assertTrue(getWindows.ok());
        assertEquals(List.of("Primary", "Secondary"), getWindows.value());

        assertTrue(maximize.ok());
        assertTrue(primary.maximized);

        assertTrue(size.ok());
        Map<?, ?> sizeValue = assertInstanceOf(Map.class, size.value());
        assertEquals(640.0, sizeValue.get("width"));
        assertEquals(480.0, sizeValue.get("height"));

        assertTrue(position.ok());
        assertEquals(32.5, primary.x);
        assertEquals(48.0, primary.y);

        assertFalse(missing.ok());
        assertEquals("window_not_found", traceReason(missing));
    }

    @Test
    void parseKeyStringNormalizesAliasesAndCase() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);

        String[] ctrlShift = assertInstanceOf(
            String[].class,
            invokePrivate(target, "parseKeyString", new Class<?>[]{String.class}, "ctrl+Shift+z")
        );
        String[] winEnter = assertInstanceOf(
            String[].class,
            invokePrivate(target, "parseKeyString", new Class<?>[]{String.class}, "win+enter")
        );

        assertEquals(List.of("CONTROL", "SHIFT", "Z"), List.of(ctrlShift));
        assertEquals(List.of("META", "ENTER"), List.of(winEnter));
    }

    @Test
    void flushKeyAccumulatorEmitsTypedEventsForIdsAndSyntheticHandles() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        accumulator(target).put("loginField", new StringBuilder("abc"));
        accumulator(target).put("_handle_7", new StringBuilder("xyz"));
        timestamps(target).put("loginField", 2_000L);
        timestamps(target).put("_handle_7", 4_000L);

        invokePrivate(target, "flushKeyAccumulator");

        Deque<Map<String, Object>> buffer = recorderBuffer(target);
        assertEquals(2, buffer.size());

        List<Map<String, Object>> entries = List.copyOf(buffer);
        assertTrue(entries.stream().anyMatch(entry ->
            "type".equals(entry.get("type"))
                && "loginField".equals(entry.get("fxId"))
                && "abc".equals(entry.get("text"))
                && Double.valueOf(2.0).equals(entry.get("timestamp"))
        ));
        assertTrue(entries.stream().anyMatch(entry ->
            "type".equals(entry.get("type"))
                && "".equals(entry.get("fxId"))
                && "xyz".equals(entry.get("text"))
                && Double.valueOf(4.0).equals(entry.get("timestamp"))
        ));

        assertTrue(accumulator(target).isEmpty());
        assertTrue(timestamps(target).isEmpty());
    }

    @Test
    void keyPressedInDialogFlushesTypingAndRecordsDismissDialog() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        accumulator(target).put("searchField", new StringBuilder("query"));
        timestamps(target).put("searchField", 1_500L);

        Button ok = new Button("Confirm");
        DialogScene scene = new DialogScene(new DialogPaneNode("Header", "Body", null), ok);
        KeyPressedEvent event = new KeyPressedEvent("ENTER", scene);

        invokePrivate(target, "onKeyPressed", new Class<?>[]{Object.class}, event);

        Deque<Map<String, Object>> buffer = recorderBuffer(target);
        assertEquals(2, buffer.size());

        Map<String, Object> typed = buffer.removeFirst();
        assertEquals("type", typed.get("type"));
        assertEquals("searchField", typed.get("fxId"));
        assertEquals("query", typed.get("text"));

        Map<String, Object> dismiss = buffer.removeFirst();
        assertEquals("dismiss_dialog", dismiss.get("type"));
        assertEquals("Confirm", dismiss.get("text"));
        assertEquals("Button", dismiss.get("nodeType"));
    }

    @Test
    void actionableAndInsideHelpersWalkAncestorChains() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);

        Button button = new Button("Save");
        ParentNode text = new ParentNode("text", "");
        button.addChild(text);

        ColorPicker colorPicker = new ColorPicker();
        ParentNode colorGraphic = new ParentNode("graphic", "");
        colorPicker.addChild(colorGraphic);

        ComboBox comboBox = new ComboBox();
        ParentNode comboGraphic = new ParentNode("graphic", "");
        comboBox.addChild(comboGraphic);

        DatePicker datePicker = new DatePicker();
        ParentNode dateGraphic = new ParentNode("graphic", "");
        datePicker.addChild(dateGraphic);

        boolean actionableButton = (boolean) invokePrivate(target, "isActionableNode", new Class<?>[]{Object.class}, button);
        boolean actionablePane = (boolean) invokePrivate(target, "isActionableNode", new Class<?>[]{Object.class}, new VBoxPane());
        Object nearest = invokePrivate(target, "nearestActionableAncestor", new Class<?>[]{Object.class}, text);
        boolean insideColor = (boolean) invokePrivate(target, "isInsideColorPicker", new Class<?>[]{Object.class}, colorGraphic);
        boolean insideCombo = (boolean) invokePrivate(target, "isInsideComboBox", new Class<?>[]{Object.class}, comboGraphic);
        boolean insideDate = (boolean) invokePrivate(target, "isInsideDatePicker", new Class<?>[]{Object.class}, dateGraphic);

        assertTrue(actionableButton);
        assertFalse(actionablePane);
        assertEquals(button, nearest);
        assertTrue(insideColor);
        assertTrue(insideCombo);
        assertTrue(insideDate);
    }

    @Test
    void scrollHelpersClampAndFindNearestScrollPane() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        ScrollPane scrollPane = new ScrollPane();
        ParentNode content = new ParentNode("content", "");
        ParentNode child = new ParentNode("child", "");
        scrollPane.addChild(content);
        content.addChild(child);

        Object ancestor = invokePrivate(target, "findScrollPaneAncestor", new Class<?>[]{Object.class}, child);
        invokePrivate(target, "scrollByDelta", new Class<?>[]{Object.class, double.class, double.class}, scrollPane, 0.3, 0.8);
        invokePrivate(target, "scrollByDelta", new Class<?>[]{Object.class, double.class, double.class}, scrollPane, -2.0, -2.0);

        Object snapshot = discoverySnapshot(
            nodeRef(new ParentNode("plain", ""), metadata("node-1", "plain", "Pane", "", "/Scene/Pane[1]")),
            nodeRef(scrollPane, metadata("node-2", "scroll", "ScrollPane", "", "/Scene/ScrollPane[1]"))
        );
        Object firstScrollPane = invokePrivate(target, "findFirstScrollPane", new Class<?>[]{snapshot.getClass()}, snapshot);

        assertEquals(scrollPane, ancestor);
        assertEquals(scrollPane, firstScrollPane);
        assertEquals(0.0, scrollPane.getHvalue());
        assertEquals(0.0, scrollPane.getVvalue());
    }

    @Test
    void assertContextBuildsDescriptorAndMasksPasswordText() {
        ReflectiveJavaFxTarget textTarget = new ReflectiveJavaFxTarget("TestApp", () -> new PickingScene(new PickResult(new Button("Launch"))));
        Map<String, Object> textContext = textTarget.assertContext(10.0, 20.0);

        assertEquals("button", textContext.get("fxId"));
        assertEquals("Button", textContext.get("nodeType"));
        assertEquals("Launch", textContext.get("currentText"));
        assertEquals(true, textContext.get("visible"));
        assertEquals(true, textContext.get("enabled"));
        assertEquals(List.of("verify_text", "verify_visible", "verify_enabled"), textContext.get("availableAssertions"));

        ReflectiveJavaFxTarget passwordTarget = new ReflectiveJavaFxTarget("TestApp", () -> new PickingScene(new PickResult(new PasswordField("secretField", "hidden"))));
        Map<String, Object> passwordContext = passwordTarget.assertContext(1.0, 1.0);

        assertEquals("secretField", passwordContext.get("fxId"));
        assertEquals("PasswordField", passwordContext.get("nodeType"));
        assertNull(passwordContext.get("currentText"));
        assertEquals(List.of("verify_visible", "verify_enabled"), passwordContext.get("availableAssertions"));
    }

    @Test
    void pollEventsFlushesPendingTypingAndAdvancesDeliveredCursor() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        recorderBuffer(target).add(Map.of("type", "click", "fxId", "login"));
        accumulator(target).put("searchField", new StringBuilder("abc"));
        timestamps(target).put("searchField", 2_500L);

        List<Map<String, Object>> firstPoll = target.pollEvents();
        List<Map<String, Object>> secondPoll = target.pollEvents();

        assertEquals(2, firstPoll.size());
        assertTrue(firstPoll.stream().anyMatch(event -> "click".equals(event.get("type"))));
        assertTrue(firstPoll.stream().anyMatch(event -> "type".equals(event.get("type")) && "searchField".equals(event.get("fxId"))));
        assertTrue(secondPoll.isEmpty());
        assertEquals(2, deliveredUpTo(target));
    }

    @Test
    void stopRecordingFlushClearsBufferAndResetsDeliveredCursor() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        invokePrivate(target, "startRecordingWatchdog", new Class<?>[]{});
        recorderBuffer(target).add(Map.of("type", "click", "fxId", "login"));
        deliveredUpTo(target, 1);

        List<Map<String, Object>> flush = target.stopRecordingFlush();

        assertEquals(1, flush.size());
        assertEquals("click", flush.get(0).get("type"));
        assertTrue(recorderBuffer(target).isEmpty());
        assertEquals(0, deliveredUpTo(target));
        assertNull(recordingWatchdogTimer(target));
        assertEquals(0L, recordingLastPollMillis(target));
    }

    @Test
    void recordingWatchdogArmsAndPollRefreshesHeartbeat() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        invokePrivate(target, "startRecordingWatchdog", new Class<?>[]{});

        long armedAt = recordingLastPollMillis(target);
        assertTrue(armedAt > 0L);
        assertNotNull(recordingWatchdogTimer(target));

        setField(target, "recordingLastPollMillis", 1L);
        target.pollEvents();

        assertTrue(recordingLastPollMillis(target) > 1L);

        invokePrivate(target, "stopRecordingWatchdog", new Class<?>[]{});
        assertNull(recordingWatchdogTimer(target));
    }

    @Test
    void recordingWatchdogIgnoresFreshHeartbeat() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        invokePrivate(target, "startRecordingWatchdog", new Class<?>[]{});
        recorderBuffer(target).add(Map.of("type", "click", "fxId", "login"));
        setField(target, "mouseEventFilter", new Object());
        setField(target, "recordingTitleTimer", new java.util.Timer("test-title", true));
        setField(target, "recordingTitleWindow", new RecordingWindow("Recorder"));
        setField(target, "recordingLastPollMillis", System.currentTimeMillis());

        invokePrivate(target, "handleRecordingWatchdog", new Class<?>[]{long.class}, System.currentTimeMillis() + 1_000L);

        assertNotNull(mouseEventFilter(target));
        assertNotNull(recordingWatchdogTimer(target));
        assertFalse(recorderBuffer(target).isEmpty());
        assertNotNull(recordingTitleTimer(target));

        invokePrivate(target, "stopRecordingWatchdog", new Class<?>[]{});
        java.util.Timer titleTimer = recordingTitleTimer(target);
        if (titleTimer != null) {
            titleTimer.cancel();
        }
    }

    @Test
    void recordingWatchdogStopsStaleRecordingAndClearsState() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        invokePrivate(target, "startRecordingWatchdog", new Class<?>[]{});
        recorderBuffer(target).add(Map.of("type", "click", "fxId", "login"));
        setField(target, "mouseEventFilter", new Object());
        setField(target, "recordingTitleTimer", new java.util.Timer("test-title", true));
        setField(target, "recordingTitleWindow", new RecordingWindow("Recorder"));
        setField(target, "recordingLastPollMillis", System.currentTimeMillis() - 10_000L);

        invokePrivate(target, "handleRecordingWatchdog", new Class<?>[]{long.class}, System.currentTimeMillis());

        assertNull(mouseEventFilter(target));
        assertNull(recordingWatchdogTimer(target));
        assertNull(recordingTitleTimer(target));
        assertNull(recordingTitleWindow(target));
        assertTrue(recorderBuffer(target).isEmpty());
        assertEquals(0, deliveredUpTo(target));
    }

    @Test
    void mousePressAndReleaseShapeRightClickAndDragEvents() throws Exception {
        ReflectiveJavaFxTarget target = new ReflectiveJavaFxTarget("TestApp", () -> null);
        Button source = new Button("Drag Me");
        Button destination = new Button("Drop Here");

        invokePrivate(target, "onMousePressed", new Class<?>[]{Object.class}, new MouseEvent("SECONDARY", 12.0, 24.0, source, null));
        Map<String, Object> rightClick = recorderBuffer(target).removeFirst();
        assertEquals("right_click", rightClick.get("type"));
        assertEquals(12.0, rightClick.get("sceneX"));
        assertEquals(24.0, rightClick.get("sceneY"));
        assertEquals("button", rightClick.get("fxId"));

        invokePrivate(target, "onMousePressed", new Class<?>[]{Object.class}, new MouseEvent("PRIMARY", 0.0, 0.0, source, null));
        invokePrivate(
            target,
            "onMouseReleased",
            new Class<?>[]{Object.class},
            new MouseEvent("PRIMARY", 40.0, 0.0, source, new PickResult(destination))
        );

        Map<String, Object> drag = recorderBuffer(target).removeFirst();
        assertEquals("drag", drag.get("type"));
        assertEquals("button", drag.get("fxId"));
        assertEquals("Drag Me", drag.get("text"));
        assertEquals("button", drag.get("toFxId"));
        assertEquals("Drop Here", drag.get("toText"));
        assertTrue(dragJustFired(target));
    }

    private ActionResult invokeAction(ReflectiveJavaFxTarget target, String method, Object node, String fxId, String handle) throws Exception {
        return invokeAction(target, method, node, fxId, handle, null);
    }

    private ActionResult invokeAction(
        ReflectiveJavaFxTarget target,
        String method,
        Object node,
        String fxId,
        String handle,
        JsonObject payload
    ) throws Exception {
        Method m;
        if (payload == null) {
            m = ReflectiveJavaFxTarget.class.getDeclaredMethod(method, Object.class, String.class, String.class);
            m.setAccessible(true);
            return (ActionResult) m.invoke(target, node, fxId, handle);
        }
        m = ReflectiveJavaFxTarget.class.getDeclaredMethod(method, Object.class, String.class, String.class, JsonObject.class);
        m.setAccessible(true);
        return (ActionResult) m.invoke(target, node, fxId, handle, payload);
    }

    private Object invokePrivate(ReflectiveJavaFxTarget target, String method, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                parameterTypes[i] = JsonObject.class;
            } else {
                parameterTypes[i] = arg.getClass();
            }
        }
        Method m = ReflectiveJavaFxTarget.class.getDeclaredMethod(method, parameterTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private Object invokePrivate(ReflectiveJavaFxTarget target, String method, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method m = ReflectiveJavaFxTarget.class.getDeclaredMethod(method, parameterTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    private Optional<?> castOptional(Object value) {
        return (Optional<?>) value;
    }

    private JsonObject selector(String key, Object value) {
        return selector(Map.of(key, value));
    }

    private JsonObject selector(Map<String, Object> values) {
        JsonObject selector = new JsonObject();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String str) {
                selector.addProperty(entry.getKey(), str);
            } else if (value instanceof Number num) {
                selector.addProperty(entry.getKey(), num);
            } else if (value instanceof Boolean bool) {
                selector.addProperty(entry.getKey(), bool);
            }
        }
        return selector;
    }

    private Map<String, Object> metadata(String handle, String fxId, String nodeType, String text, String path) {
        return Map.of(
            "handle", handle,
            "fxId", fxId,
            "nodeType", nodeType,
            "text", text,
            "hierarchyPath", path,
            "visible", true,
            "enabled", true
        );
    }

    private Object nodeRef(Object node, Map<String, Object> metadata) throws Exception {
        Class<?> nodeRefClass = Class.forName("dev.omniui.agent.runtime.ReflectiveJavaFxTarget$NodeRef");
        Constructor<?> ctor = nodeRefClass.getDeclaredConstructor(Object.class, Map.class);
        ctor.setAccessible(true);
        return ctor.newInstance(node, metadata);
    }

    private Object discoverySnapshot(Object... nodeRefs) throws Exception {
        Class<?> snapshotClass = Class.forName("dev.omniui.agent.runtime.ReflectiveJavaFxTarget$DiscoverySnapshot");
        Constructor<?> ctor = snapshotClass.getDeclaredConstructor(List.class);
        ctor.setAccessible(true);
        return ctor.newInstance(List.of(nodeRefs));
    }

    @SuppressWarnings("unchecked")
    private List<?> snapshotNodes(Object snapshot) throws Exception {
        Method nodes = snapshot.getClass().getDeclaredMethod("nodes");
        nodes.setAccessible(true);
        return (List<?>) nodes.invoke(snapshot);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nodeMetadata(Object nodeRef) throws Exception {
        Method metadata = nodeRef.getClass().getDeclaredMethod("metadata");
        metadata.setAccessible(true);
        return (Map<String, Object>) metadata.invoke(nodeRef);
    }

    @SuppressWarnings("unchecked")
    private String traceReason(ActionResult result) {
        return (String) ((Map<String, Object>) result.trace().get("details")).get("reason");
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, StringBuilder> accumulator(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("keyAccumulator");
        field.setAccessible(true);
        return (ConcurrentHashMap<String, StringBuilder>) field.get(target);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Long> timestamps(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("keyTimestamps");
        field.setAccessible(true);
        return (ConcurrentHashMap<String, Long>) field.get(target);
    }

    @SuppressWarnings("unchecked")
    private Deque<Map<String, Object>> recorderBuffer(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("recorderBuffer");
        field.setAccessible(true);
        return (Deque<Map<String, Object>>) field.get(target);
    }

    private int deliveredUpTo(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("deliveredUpTo");
        field.setAccessible(true);
        java.util.concurrent.atomic.AtomicInteger value = (java.util.concurrent.atomic.AtomicInteger) field.get(target);
        return value.get();
    }

    private void deliveredUpTo(ReflectiveJavaFxTarget target, int value) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("deliveredUpTo");
        field.setAccessible(true);
        java.util.concurrent.atomic.AtomicInteger atomic = (java.util.concurrent.atomic.AtomicInteger) field.get(target);
        atomic.set(value);
    }

    private boolean dragJustFired(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("dragJustFired");
        field.setAccessible(true);
        return field.getBoolean(target);
    }

    private java.util.Timer recordingWatchdogTimer(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("recordingWatchdogTimer");
        field.setAccessible(true);
        return (java.util.Timer) field.get(target);
    }

    private java.util.Timer recordingTitleTimer(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("recordingTitleTimer");
        field.setAccessible(true);
        return (java.util.Timer) field.get(target);
    }

    private Object recordingTitleWindow(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("recordingTitleWindow");
        field.setAccessible(true);
        return field.get(target);
    }

    private Object mouseEventFilter(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("mouseEventFilter");
        field.setAccessible(true);
        return field.get(target);
    }

    private long recordingLastPollMillis(ReflectiveJavaFxTarget target) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField("recordingLastPollMillis");
        field.setAccessible(true);
        return field.getLong(target);
    }

    private void setField(ReflectiveJavaFxTarget target, String name, Object value) throws Exception {
        Field field = ReflectiveJavaFxTarget.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    static class ParentNode {
        private final String id;
        private final String text;
        private final List<Object> children = new java.util.ArrayList<>();
        private Object parent;

        ParentNode(String id, String text) {
            this.id = id;
            this.text = text;
        }

        void addChild(Object child) {
            children.add(child);
            if (child instanceof ParentNode parentNode) {
                parentNode.setParent(this);
            }
        }

        void setParent(Object parent) {
            this.parent = parent;
        }

        public List<Object> getChildrenUnmodifiable() {
            return List.copyOf(children);
        }

        public Object getParent() {
            return parent;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public boolean isVisible() {
            return true;
        }

        public boolean isDisabled() {
            return false;
        }
    }

    static final class ButtonNode extends ParentNode {
        ButtonNode(String id, String text) {
            super(id, text);
        }
    }

    static final class LabelNode extends ParentNode {
        LabelNode(String id, String text) {
            super(id, text);
        }
    }

    static final class SliderNode {
        private final double min;
        private final double max;
        private double value;

        SliderNode(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    static final class SpinnerNode {
        private final Object valueFactory;

        SpinnerNode(Object valueFactory) {
            this.valueFactory = valueFactory;
        }

        public Object getValueFactory() {
            return valueFactory;
        }
    }

    static final class SpinnerValueFactory {
        private final Object converter;
        private Object value;

        SpinnerValueFactory(Object converter) {
            this.converter = converter;
        }

        public Object getConverter() {
            return converter;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    static final class IntegerConverter {
        public Integer fromString(String value) {
            return Integer.valueOf(value);
        }
    }

    static final class FailingConverter {
        public Integer fromString(String value) {
            throw new IllegalStateException("bad value: " + value);
        }
    }

    static final class StepSpinnerNode {
        int incrementCalls;
        int decrementCalls;

        public void increment(int steps) {
            incrementCalls += steps;
        }

        public void decrement(int steps) {
            decrementCalls += steps;
        }
    }

    static final class Pagination {
        private int currentPageIndex;
        private final int pageCount;

        Pagination(int currentPageIndex, int pageCount) {
            this.currentPageIndex = currentPageIndex;
            this.pageCount = pageCount;
        }

        public int getCurrentPageIndex() {
            return currentPageIndex;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setCurrentPageIndex(int currentPageIndex) {
            this.currentPageIndex = currentPageIndex;
        }
    }

    static final class ScrollBar {
        private double value;
        private final double min;
        private final double max;

        ScrollBar(double value, double min, double max) {
            this.value = value;
            this.min = min;
            this.max = max;
        }

        public double getValue() {
            return value;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    static class TreeItemNode {
        private final Object value;
        private final List<Object> children = new java.util.ArrayList<>();

        TreeItemNode(Object value, Object... children) {
            this.value = value;
            this.children.addAll(List.of(children));
        }

        public Object getValue() {
            return value;
        }

        public List<Object> getChildren() {
            return children;
        }

        void addChild(Object child) {
            children.add(child);
        }
    }

    static final class TreeItemWithoutChildren {
        private final Object value;

        TreeItemWithoutChildren(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public Object getChildren() {
            return value;
        }
    }

    static final class TreeTableItemNode extends TreeItemNode {
        TreeTableItemNode(Object value, Object... children) {
            super(value, children);
        }
    }

    static final class TreeTableView {
        private final List<Object> columns;
        private Object root;

        TreeTableView(List<Object> columns) {
            this.columns = columns;
        }

        public Object getRoot() {
            return root;
        }

        void setRoot(Object root) {
            this.root = root;
        }

        public List<Object> getColumns() {
            return columns;
        }
    }

    static final class TreeTableColumn {
        private final String text;
        private final java.util.function.Function<TreeTableItemNode, Object> resolver;

        TreeTableColumn(String text, java.util.function.Function<TreeTableItemNode, Object> resolver) {
            this.text = text;
            this.resolver = resolver;
        }

        public String getText() {
            return text;
        }

        public ObservableValue getCellObservableValue(Object item) {
            return new ObservableValue(resolver.apply((TreeTableItemNode) item));
        }
    }

    static final class ObservableValue {
        private final Object value;

        ObservableValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    record PersonRow(String name, String role) {
        @Override
        public String toString() {
            return name + " / " + role;
        }
    }

    static class MenuItemNode {
        private final String text;
        private final String id;
        private final List<Object> items = new java.util.ArrayList<>();
        boolean fired;

        MenuItemNode(String text, String id, Object... items) {
            this.text = text;
            this.id = id;
            this.items.addAll(List.of(items));
        }

        public String getText() {
            return text;
        }

        public String getId() {
            return id;
        }

        public List<Object> getItems() {
            return items;
        }

        public void fire() {
            fired = true;
        }
    }

    static final class ContextMenuWindow {
        private final List<?> items;

        ContextMenuWindow(List<?> items) {
            this.items = items;
        }

        public List<?> getItems() {
            return items;
        }
    }

    static class ButtonBarNode extends ParentNode {
        private final List<Object> buttons = new java.util.ArrayList<>();

        ButtonBarNode(Object... buttons) {
            super("buttonBar", "");
            this.buttons.addAll(List.of(buttons));
            for (Object button : buttons) {
                addChild(button);
                if (button instanceof ParentNode parent) {
                    parent.setParent(this);
                }
            }
        }

        public List<Object> getButtons() {
            return buttons;
        }
    }

    static class DialogPaneNode extends ParentNode {
        private final String headerText;
        private final String contentText;
        private final Object dialog;

        DialogPaneNode(String headerText, String contentText, Object dialog, Object... children) {
            super("dialogPane", "");
            this.headerText = headerText;
            this.contentText = contentText;
            this.dialog = dialog;
            for (Object child : children) {
                addChild(child);
                if (child instanceof ParentNode parent) {
                    parent.setParent(this);
                }
            }
        }

        public String getHeaderText() {
            return headerText;
        }

        public String getContentText() {
            return contentText;
        }

        public Object getDialog() {
            return dialog;
        }
    }

    static final class DialogWindow {
        private final String title;

        DialogWindow(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    static class Stage {
        private final String title;
        private double width = 640.0;
        private double height = 480.0;
        private double x = 10.0;
        private double y = 20.0;
        private boolean maximized;
        private boolean iconified;
        boolean fronted;

        Stage(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void toFront() {
            fronted = true;
        }

        public void setMaximized(boolean maximized) {
            this.maximized = maximized;
        }

        public void setIconified(boolean iconified) {
            this.iconified = iconified;
        }

        public boolean isMaximized() {
            return maximized;
        }

        public boolean isIconified() {
            return iconified;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    static final class PopupWindow extends Stage {
        PopupWindow(String title) {
            super(title);
        }
    }

    static final class DialogScene {
        private final Object root;
        private final Object focusOwner;

        DialogScene(Object root, Object focusOwner) {
            this.root = root;
            this.focusOwner = focusOwner;
        }

        public Object getRoot() {
            return root;
        }

        public Object getFocusOwner() {
            return focusOwner;
        }
    }

    static final class RecordingWindow {
        private String title;

        RecordingWindow(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    static final class KeyPressedEvent {
        private final Object code;
        private final Object source;

        KeyPressedEvent(Object code, Object source) {
            this.code = code;
            this.source = source;
        }

        public Object getCode() {
            return code;
        }

        public Object getSource() {
            return source;
        }
    }

    static final class PickingScene {
        private final Object pickResult;

        PickingScene(Object pickResult) {
            this.pickResult = pickResult;
        }

        public Object pick(double x, double y) {
            return pickResult;
        }
    }

    static final class PickResult {
        private final Object intersectedNode;

        PickResult(Object intersectedNode) {
            this.intersectedNode = intersectedNode;
        }

        public Object getIntersectedNode() {
            return intersectedNode;
        }
    }

    static final class MouseEvent {
        private final Object button;
        private final double sceneX;
        private final double sceneY;
        private final Object target;
        private final Object pickResult;

        MouseEvent(Object button, double sceneX, double sceneY, Object target, Object pickResult) {
            this.button = button;
            this.sceneX = sceneX;
            this.sceneY = sceneY;
            this.target = target;
            this.pickResult = pickResult;
        }

        public Object getButton() {
            return button;
        }

        public double getSceneX() {
            return sceneX;
        }

        public double getSceneY() {
            return sceneY;
        }

        public Object getTarget() {
            return target;
        }

        public Object getPickResult() {
            return pickResult;
        }
    }

    static final class ColorPicker extends ParentNode {
        ColorPicker() {
            super("colorPicker", "");
        }
    }

    static final class ComboBox extends ParentNode {
        ComboBox() {
            super("comboBox", "");
        }
    }

    static final class DatePicker extends ParentNode {
        DatePicker() {
            super("datePicker", "");
        }
    }

    static final class VBoxPane extends ParentNode {
        VBoxPane() {
            super("vbox", "");
        }
    }

    static final class ScrollPane extends ParentNode {
        private double hvalue;
        private double vvalue;

        ScrollPane() {
            super("scrollPane", "");
        }

        public double getHvalue() {
            return hvalue;
        }

        public double getVvalue() {
            return vvalue;
        }

        public void setHvalue(double hvalue) {
            this.hvalue = hvalue;
        }

        public void setVvalue(double vvalue) {
            this.vvalue = vvalue;
        }
    }

    static final class AlertDialog {
        private final String alertType;

        AlertDialog(String alertType) {
            this.alertType = alertType;
        }

        public Object getAlertType() {
            return alertType;
        }
    }

    static class Button extends ParentNode {
        Button(String text) {
            super("button", text);
        }
    }

    static final class PasswordField extends ParentNode {
        PasswordField(String id, String text) {
            super(id, text);
        }
    }

    static final class ButtonGraphic extends ParentNode {
        ButtonGraphic() {
            super("graphic", "");
        }
    }
}
