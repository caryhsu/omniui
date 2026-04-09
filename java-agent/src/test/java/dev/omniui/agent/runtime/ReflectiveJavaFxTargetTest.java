package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectiveJavaFxTargetTest {

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

    static class ParentNode {
        private final String id;
        private final String text;
        private final List<Object> children = new java.util.ArrayList<>();

        ParentNode(String id, String text) {
            this.id = id;
            this.text = text;
        }

        void addChild(Object child) {
            children.add(child);
        }

        public List<Object> getChildrenUnmodifiable() {
            return List.copyOf(children);
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
}
