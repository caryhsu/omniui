package dev.omniui.demo.core;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class CoreDemoApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        TextField username = new TextField();
        username.setId("username");
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setId("password");
        password.setPromptText("Password");

        Label status = new Label("Idle");
        status.setId("status");

        Button loginButton = new Button("Login");
        loginButton.setId("loginButton");
        loginButton.setTooltip(new Tooltip("Enter credentials and click to log in"));
        loginButton.setOnAction(event -> {
            String value = "admin".equals(username.getText()) && "1234".equals(password.getText())
                ? "Success"
                : "Failed";
            status.setText(value);
        });

        Label loginSectionTitle = new Label("Login Flow");
        loginSectionTitle.setId("loginSectionTitle");

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("Admin", "Operator", "Viewer"));
        roleCombo.setId("roleCombo");
        roleCombo.setPromptText("Select role");

        Label roleStatus = new Label("Selected role: none");
        roleStatus.setId("roleStatus");
        roleCombo.valueProperty().addListener((observable, oldValue, newValue) ->
            roleStatus.setText("Selected role: " + (newValue == null ? "none" : newValue))
        );

        ListView<String> serverList = new ListView<>(FXCollections.observableArrayList(
            "alpha-node",
            "beta-node",
            "gamma-node",
            "delta-node"
        ));
        serverList.setId("serverList");
        serverList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverList.setPrefHeight(120);

        Label listStatus = new Label("Selected server: none");
        listStatus.setId("listStatus");
        serverList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
            listStatus.setText("Selected server: " + (newValue == null ? "none" : newValue))
        );

        TreeItem<String> infrastructureRoot = new TreeItem<>("Infrastructure");
        infrastructureRoot.setExpanded(true);
        TreeItem<String> edgeCluster = new TreeItem<>("Edge Cluster");
        edgeCluster.getChildren().addAll(new TreeItem<>("edge-a"), new TreeItem<>("edge-b"));
        TreeItem<String> coreCluster = new TreeItem<>("Core Cluster");
        coreCluster.getChildren().addAll(new TreeItem<>("core-a"), new TreeItem<>("core-b"));
        infrastructureRoot.getChildren().addAll(edgeCluster, coreCluster);

        TreeView<String> assetTree = new TreeView<>(infrastructureRoot);
        assetTree.setId("assetTree");
        assetTree.setShowRoot(true);
        assetTree.setPrefHeight(180);

        Label treeStatus = new Label("Selected tree item: none");
        treeStatus.setId("treeStatus");
        assetTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
            treeStatus.setText("Selected tree item: " + (newValue == null ? "none" : newValue.getValue()))
        );

        TableView<UserRecord> userTable = new TableView<>();
        userTable.setId("userTable");
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        userTable.setPrefHeight(180);

        TableColumn<UserRecord, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setId("userNameColumn");
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));

        TableColumn<UserRecord, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setId("userRoleColumn");
        roleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().role()));

        TableColumn<UserRecord, String> stateColumn = new TableColumn<>("State");
        stateColumn.setId("userStateColumn");
        stateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().state()));

        userTable.getColumns().addAll(nameColumn, roleColumn, stateColumn);
        userTable.setItems(FXCollections.observableArrayList(
            new UserRecord("Ava", "Admin", "Active"),
            new UserRecord("Noah", "Operator", "Idle"),
            new UserRecord("Mia", "Viewer", "Pending")
        ));

        Label tableStatus = new Label("Selected row: none");
        tableStatus.setId("tableStatus");
        userTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
            tableStatus.setText("Selected row: " + (newValue == null ? "none" : newValue.name() + "/" + newValue.role()))
        );

        GridPane settingsGrid = new GridPane();
        settingsGrid.setId("settingsGrid");
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(12);

        Label gridTitle = new Label("Grid Settings");
        gridTitle.setId("gridTitle");
        GridPane.setColumnSpan(gridTitle, 2);
        settingsGrid.add(gridTitle, 0, 0);

        CheckBox auditEnabled = new CheckBox("Audit Enabled");
        auditEnabled.setId("auditEnabled");
        auditEnabled.setContentDisplay(ContentDisplay.RIGHT);
        settingsGrid.add(auditEnabled, 0, 1);

        CheckBox notificationsEnabled = new CheckBox("Notifications Enabled");
        notificationsEnabled.setId("notificationsEnabled");
        notificationsEnabled.setContentDisplay(ContentDisplay.RIGHT);
        settingsGrid.add(notificationsEnabled, 1, 1);

        Label regionLabel = new Label("Region");
        regionLabel.setId("regionLabel");
        regionLabel.setStyle("-fx-text-fill: green;");
        settingsGrid.add(regionLabel, 0, 2);

        TextField regionField = new TextField("ap-northeast-1");
        regionField.setId("regionField");
        settingsGrid.add(regionField, 1, 2);

        VBox selectionSection = section(
            "Selection Controls",
            "selectionSection",
            roleCombo,
            roleStatus,
            serverList,
            listStatus
        );

        VBox hierarchySection = section(
            "Hierarchy Controls",
            "hierarchySection",
            assetTree,
            treeStatus
        );

        VBox tableSection = section(
            "Table Controls",
            "tableSection",
            userTable,
            tableStatus
        );

        VBox gridSection = section(
            "Grid Layout",
            "gridSection",
            settingsGrid
        );

        VBox root = new VBox(
            18,
            loginSectionTitle,
            username,
            password,
            loginButton,
            status,
            selectionSection,
            hierarchySection,
            tableSection,
            gridSection
        );
        root.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setId("demoScrollPane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(scrollPane, 720, 760);
        primaryStage.setTitle("OmniUI Core Demo");
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

    public record UserRecord(String name, String role, String state) {
    }
}
