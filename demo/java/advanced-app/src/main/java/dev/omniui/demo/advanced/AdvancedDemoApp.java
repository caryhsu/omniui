package dev.omniui.demo.advanced;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class AdvancedDemoApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // ---- ContextMenu demo ---------------------------------------------
        Label contextMenuTarget = new Label("Right-click me");
        contextMenuTarget.setId("contextMenuTarget");

        MenuItem cmCopyItem = new MenuItem("Copy");
        cmCopyItem.setId("cmCopy");
        MenuItem cmPasteItem = new MenuItem("Paste");
        cmPasteItem.setId("cmPaste");
        Menu cmSubmenu = new Menu("More");
        cmSubmenu.setId("cmMore");
        MenuItem cmDetailsItem = new MenuItem("Details");
        cmDetailsItem.setId("cmDetails");
        cmSubmenu.getItems().add(cmDetailsItem);

        ContextMenu nodeContextMenu = new ContextMenu(cmCopyItem, cmPasteItem, new SeparatorMenuItem(), cmSubmenu);
        contextMenuTarget.setContextMenu(nodeContextMenu);

        Label contextMenuStatus = new Label("Context menu: none");
        contextMenuStatus.setId("contextMenuStatus");
        cmCopyItem.setOnAction(e -> contextMenuStatus.setText("Context menu: Copy"));
        cmPasteItem.setOnAction(e -> contextMenuStatus.setText("Context menu: Paste"));
        cmDetailsItem.setOnAction(e -> contextMenuStatus.setText("Context menu: Details"));

        VBox contextMenuSection = section("ContextMenu Demo", "contextMenuSection",
            contextMenuTarget, contextMenuStatus);

        // ---- MenuBar demo -------------------------------------------------
        MenuBar demoMenuBar = new MenuBar();
        demoMenuBar.setId("demoMenuBar");

        Menu fileMenu = new Menu("File");
        fileMenu.setId("menuFile");
        MenuItem newItem = new MenuItem("New");
        newItem.setId("menuFileNew");
        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setId("menuFileSaveAs");
        fileMenu.getItems().addAll(newItem, saveAsItem);

        Menu editMenu = new Menu("Edit");
        editMenu.setId("menuEdit");
        Menu editAdvanced = new Menu("Advanced");
        editAdvanced.setId("menuEditAdvanced");
        MenuItem reformatItem = new MenuItem("Reformat");
        reformatItem.setId("menuEditReformat");
        editAdvanced.getItems().add(reformatItem);
        editMenu.getItems().add(editAdvanced);

        demoMenuBar.getMenus().addAll(fileMenu, editMenu);

        Label menuBarStatus = new Label("Menu: none");
        menuBarStatus.setId("menuBarStatus");
        newItem.setOnAction(e -> menuBarStatus.setText("Menu: File > New"));
        saveAsItem.setOnAction(e -> menuBarStatus.setText("Menu: File > Save As"));
        reformatItem.setOnAction(e -> menuBarStatus.setText("Menu: Edit > Advanced > Reformat"));

        VBox menuBarSection = section("MenuBar Demo", "menuBarSection",
            demoMenuBar, menuBarStatus);

        // ---- Dialog demo --------------------------------------------------
        Button showDialogButton = new Button("Show Dialog");
        showDialogButton.setId("showDialogButton");

        Label dialogStatus = new Label("Dialog: none");
        dialogStatus.setId("dialogStatus");
        showDialogButton.setOnAction(e -> Platform.runLater(() -> {
            javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Confirm Action");
            dialog.setHeaderText("Please confirm");
            dialog.setContentText("Are you sure you want to proceed?");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(bt -> bt);
            dialog.showAndWait().ifPresent(bt -> dialogStatus.setText("Dialog: " + bt.getText()));
        }));

        VBox dialogSection = section("Dialog Demo", "dialogSection",
            showDialogButton, dialogStatus);

        // ---- Alert demo ---------------------------------------------------
        Button infoAlertButton = new Button("Information");
        infoAlertButton.setId("infoAlertButton");
        Button confirmAlertButton = new Button("Confirmation");
        confirmAlertButton.setId("confirmAlertButton");
        Button warnAlertButton = new Button("Warning");
        warnAlertButton.setId("warnAlertButton");
        Button errorAlertButton = new Button("Error");
        errorAlertButton.setId("errorAlertButton");

        Label alertStatus = new Label("Alert: none");
        alertStatus.setId("alertStatus");

        infoAlertButton.setOnAction(e -> Platform.runLater(() -> {
            Alert a = new Alert(AlertType.INFORMATION, "This is an info message.", ButtonType.OK);
            a.setTitle("Information");
            a.setHeaderText("Info");
            a.showAndWait();
            alertStatus.setText("Alert: INFORMATION");
        }));
        confirmAlertButton.setOnAction(e -> Platform.runLater(() -> {
            Alert a = new Alert(AlertType.CONFIRMATION, "Proceed?", ButtonType.YES, ButtonType.NO);
            a.setTitle("Confirmation");
            a.setHeaderText("Confirm");
            a.showAndWait().ifPresent(bt -> alertStatus.setText("Alert: CONFIRMATION > " + bt.getText()));
        }));
        warnAlertButton.setOnAction(e -> Platform.runLater(() -> {
            Alert a = new Alert(AlertType.WARNING, "This is a warning.", ButtonType.OK);
            a.setTitle("Warning");
            a.setHeaderText("Warning");
            a.showAndWait();
            alertStatus.setText("Alert: WARNING");
        }));
        errorAlertButton.setOnAction(e -> Platform.runLater(() -> {
            Alert a = new Alert(AlertType.ERROR, "An error occurred.", ButtonType.OK);
            a.setTitle("Error");
            a.setHeaderText("Error");
            a.showAndWait();
            alertStatus.setText("Alert: ERROR");
        }));

        VBox alertSection = section("Alert Demo", "alertSection",
            infoAlertButton, confirmAlertButton, warnAlertButton, errorAlertButton, alertStatus);

        // ---- TabPane demo ---------------------------------------------------
        TabPane demoTabPane = new TabPane();
        demoTabPane.setId("demoTabPane");
        Tab tabOne = new Tab("First");
        tabOne.setContent(new Label("Content of First tab"));
        tabOne.setClosable(false);
        Tab tabTwo = new Tab("Second");
        tabTwo.setContent(new Label("Content of Second tab"));
        tabTwo.setClosable(false);
        Tab tabThree = new Tab("Third");
        tabThree.setContent(new Label("Content of Third tab"));
        tabThree.setClosable(false);
        demoTabPane.getTabs().addAll(tabOne, tabTwo, tabThree);

        VBox tabSection = section("TabPane Demo", "tabSection", demoTabPane);

        // ---- Accordion + TreeTableView demo ---------------------------------
        TitledPane pane1 = new TitledPane("Section 1", new Label("Content of Section 1"));
        pane1.setId("pane1");
        TitledPane pane2 = new TitledPane("Section 2", new Label("Content of Section 2"));
        pane2.setId("pane2");
        TitledPane pane3 = new TitledPane("Section 3", new Label("Content of Section 3"));
        pane3.setId("pane3");
        Accordion demoAccordion = new Accordion(pane1, pane2, pane3);
        demoAccordion.setId("demoAccordion");
        VBox accordionSection = section("Accordion Demo", "accordionSection", demoAccordion);

        TreeTableColumn<DeptRecord, String> deptNameCol = new TreeTableColumn<>("Name");
        deptNameCol.setId("deptNameCol");
        deptNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getValue().name()));

        TreeTableColumn<DeptRecord, String> deptTypeCol = new TreeTableColumn<>("Department");
        deptTypeCol.setId("deptTypeCol");
        deptTypeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getValue().department()));

        TreeTableView<DeptRecord> demoTreeTable = new TreeTableView<>();
        demoTreeTable.setId("demoTreeTable");
        demoTreeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        demoTreeTable.setPrefHeight(200);
        demoTreeTable.getColumns().addAll(deptNameCol, deptTypeCol);

        TreeItem<DeptRecord> treeRoot = new TreeItem<>(new DeptRecord("Company", "Root"));
        treeRoot.setExpanded(true);

        TreeItem<DeptRecord> engDept = new TreeItem<>(new DeptRecord("Engineering", "Department"));
        engDept.setExpanded(false);
        engDept.getChildren().addAll(
            new TreeItem<>(new DeptRecord("Alice", "Engineer")),
            new TreeItem<>(new DeptRecord("Bob", "Engineer"))
        );

        TreeItem<DeptRecord> salesDept = new TreeItem<>(new DeptRecord("Sales", "Department"));
        salesDept.setExpanded(false);
        salesDept.getChildren().addAll(
            new TreeItem<>(new DeptRecord("Carol", "Sales Rep")),
            new TreeItem<>(new DeptRecord("Dave", "Sales Rep"))
        );

        treeRoot.getChildren().addAll(engDept, salesDept);
        demoTreeTable.setRoot(treeRoot);
        demoTreeTable.setShowRoot(false);
        VBox treeTableSection = section("TreeTableView Demo", "treeTableSection", demoTreeTable);

        // ---- SplitPane demo -----------------------------------------------
        Label leftPanel = new Label("Left Panel");
        leftPanel.setId("splitLeft");
        leftPanel.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 10;");
        VBox leftBox = new VBox(leftPanel);
        leftBox.setStyle("-fx-background-color: #e8f4fd;");

        Label rightPanel = new Label("Right Panel");
        rightPanel.setId("splitRight");
        rightPanel.setStyle("-fx-background-color: #fdecea; -fx-padding: 10;");
        VBox rightBox = new VBox(rightPanel);
        rightBox.setStyle("-fx-background-color: #fdecea;");

        SplitPane demoSplitPane = new SplitPane(leftBox, rightBox);
        demoSplitPane.setId("demoSplitPane");
        demoSplitPane.setPrefHeight(100);
        demoSplitPane.setDividerPositions(0.5);

        Label dividerResult = new Label("Divider: 0.50");
        dividerResult.setId("dividerResult");
        demoSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) ->
            dividerResult.setText(String.format("Divider: %.2f", newVal.doubleValue()))
        );

        VBox splitPaneSection = section("SplitPane Demo", "splitPaneSection",
            demoSplitPane, dividerResult);

        // ---- Node State Test section --------------------------------------
        Button nodeStateTarget = new Button("Target Button");
        nodeStateTarget.setId("nodeStateTarget");

        Label nodeStateStatus = new Label("visible=true  enabled=true");
        nodeStateStatus.setId("nodeStateStatus");

        CheckBox visibilityToggle = new CheckBox("Visible");
        visibilityToggle.setId("visibilityToggle");
        visibilityToggle.setSelected(true);
        visibilityToggle.setOnAction(e -> {
            nodeStateTarget.setVisible(visibilityToggle.isSelected());
            nodeStateStatus.setText(
                "visible=" + nodeStateTarget.isVisible() +
                "  enabled=" + !nodeStateTarget.isDisable());
        });

        CheckBox enabledToggle = new CheckBox("Enabled");
        enabledToggle.setId("enabledToggle");
        enabledToggle.setSelected(true);
        enabledToggle.setOnAction(e -> {
            nodeStateTarget.setDisable(!enabledToggle.isSelected());
            nodeStateStatus.setText(
                "visible=" + nodeStateTarget.isVisible() +
                "  enabled=" + !nodeStateTarget.isDisable());
        });

        VBox nodeStateSection = section("Node State Test", "nodeStateSection",
            nodeStateTarget, nodeStateStatus, visibilityToggle, enabledToggle);

        VBox scrollRows = new VBox(4);
        scrollRows.setId("scrollRowsBox");
        for (int i = 0; i < 30; i++) {
            Label row = new Label("Scroll Row " + i);
            row.setId("scrollRow" + i);
            scrollRows.getChildren().add(row);
        }
        ScrollPane innerScrollPane = new ScrollPane(scrollRows);
        innerScrollPane.setId("innerScrollPane");
        innerScrollPane.setPrefHeight(120);
        innerScrollPane.setFitToWidth(true);
        VBox scrollTestSection = section("Scroll Test", "scrollTestSection", innerScrollPane);

        // ---- Tooltip demo -------------------------------------------------
        Button tooltipBtn = new Button("Hover Me");
        tooltipBtn.setId("tooltipBtn");
        tooltipBtn.setTooltip(new Tooltip("Hover to see this tooltip"));
        VBox tooltipSection = section("Tooltip Demo", "tooltipSection", tooltipBtn);

        // ---- ToolBar demo -------------------------------------------------
        javafx.scene.control.ToolBar mainToolBar = new javafx.scene.control.ToolBar();
        mainToolBar.setId("mainToolBar");

        Button tbNew = new Button("New");
        tbNew.setId("tbNew");
        Button tbOpen = new Button("Open");
        tbOpen.setId("tbOpen");
        Button tbSave = new Button("Save");
        tbSave.setId("tbSave");
        tbSave.setDisable(true);
        javafx.scene.control.Separator tbSep = new javafx.scene.control.Separator();
        tbSep.setId("tbSeparator");
        Button tbDelete = new Button("Delete");
        tbDelete.setId("tbDelete");

        mainToolBar.getItems().addAll(tbNew, tbOpen, tbSave, tbSep, tbDelete);

        Label tbStatus = new Label("ToolBar ready");
        tbStatus.setId("tbStatus");
        tbNew.setOnAction(e -> tbStatus.setText("New clicked"));
        tbOpen.setOnAction(e -> tbStatus.setText("Open clicked"));
        tbDelete.setOnAction(e -> tbStatus.setText("Delete clicked"));

        VBox toolBarSection = section("ToolBar Demo", "toolBarSection", mainToolBar, tbStatus);

        // ScrollBar Demo
        javafx.scene.control.ScrollBar demoScrollBar = new javafx.scene.control.ScrollBar();
        demoScrollBar.setId("demoScrollBar");
        demoScrollBar.setMin(0);
        demoScrollBar.setMax(100);
        demoScrollBar.setValue(30);
        demoScrollBar.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        demoScrollBar.setMinWidth(200);
        Label scrollBarValueLabel = new Label("ScrollBar value: 30.0");
        scrollBarValueLabel.setId("scrollBarValueLabel");
        demoScrollBar.valueProperty().addListener((obs, oldV, newV) ->
            scrollBarValueLabel.setText("ScrollBar value: " + String.format("%.1f", newV.doubleValue())));
        VBox scrollBarSection = section("ScrollBar Demo", "scrollBarSection", demoScrollBar, scrollBarValueLabel);

        // Pagination Demo
        javafx.scene.control.Pagination demoPagination = new javafx.scene.control.Pagination(5, 0);
        demoPagination.setId("demoPagination");
        Label pageLabel = new Label("Page: 0 / 5");
        pageLabel.setId("pageLabel");
        demoPagination.currentPageIndexProperty().addListener((obs, oldV, newV) ->
            pageLabel.setText("Page: " + newV.intValue() + " / " + demoPagination.getPageCount()));
        VBox paginationSection = section("Pagination Demo", "paginationSection", demoPagination, pageLabel);

        // Window Demo
        Button openSecondWindowBtn = new Button("Open Second Window");
        openSecondWindowBtn.setId("openSecondWindowBtn");
        openSecondWindowBtn.setOnAction(e -> {
            javafx.stage.Stage second = new javafx.stage.Stage();
            second.setTitle("OmniUI Second Window");
            second.setScene(new Scene(new javafx.scene.layout.StackPane(new Label("Second Window")), 400, 200));
            second.show();
        });
        VBox windowSection = section("Window Demo", "windowSection", openSecondWindowBtn);

        // Within (Scoped Selector) Demo — two panels each with a duplicate-id button
        VBox withinLeft = new VBox(8);
        withinLeft.setId("leftPanel");
        withinLeft.setStyle("-fx-border-color: #999; -fx-padding: 8;");
        Button leftOkBtn = new Button("OK");
        leftOkBtn.setId("panelOkBtn");
        Label leftStatus = new Label("left idle");
        leftStatus.setId("leftPanelStatus");
        leftOkBtn.setOnAction(e -> leftStatus.setText("left clicked"));
        withinLeft.getChildren().addAll(new Label("Left Panel"), leftOkBtn, leftStatus);

        VBox withinRight = new VBox(8);
        withinRight.setId("rightPanel");
        withinRight.setStyle("-fx-border-color: #999; -fx-padding: 8;");
        Button rightOkBtn = new Button("OK");
        rightOkBtn.setId("panelOkBtn");
        Label rightStatus = new Label("right idle");
        rightStatus.setId("rightPanelStatus");
        rightOkBtn.setOnAction(e -> rightStatus.setText("right clicked"));
        withinRight.getChildren().addAll(new Label("Right Panel"), rightOkBtn, rightStatus);

        javafx.scene.layout.HBox panelRow = new javafx.scene.layout.HBox(16, withinLeft, withinRight);
        VBox withinSection = section("Within Demo", "withinSection", panelRow);

        VBox root = new VBox(
            18,
            toolBarSection,
            scrollBarSection,
            paginationSection,
            windowSection,
            withinSection,
            contextMenuSection,
            menuBarSection,
            dialogSection,
            alertSection,
            tabSection,
            accordionSection,
            treeTableSection,
            splitPaneSection,
            nodeStateSection,
            scrollTestSection,
            tooltipSection
        );
        root.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setId("demoScrollPane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(scrollPane, 720, 760);
        primaryStage.setTitle("OmniUI Advanced Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox section(String title, String id, javafx.scene.Node... children) {
        Label heading = new Label(title);
        heading.setId(id + "Title");
        VBox box = new VBox(10, heading);
        box.setId(id);
        box.getChildren().addAll(children);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public record DeptRecord(String name, String department) {
        @Override public String toString() { return name; }
    }
}
