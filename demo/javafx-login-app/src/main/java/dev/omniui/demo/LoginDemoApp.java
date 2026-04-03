package dev.omniui.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.application.Platform;
import javafx.stage.Stage;

public final class LoginDemoApp extends Application {
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
        serverList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

        // ---- DatePicker demo ----------------------------------------------
        DatePicker demoPicker = new DatePicker();
        demoPicker.setId("demoPicker");
        demoPicker.setPromptText("Pick a date");

        Label datePickerStatus = new Label("Picked date: none");
        datePickerStatus.setId("datePickerStatus");
        demoPicker.valueProperty().addListener((obs, oldVal, newVal) ->
            datePickerStatus.setText("Picked date: " + (newVal == null ? "none" : newVal.toString()))
        );

        VBox datePickerSection = section("DatePicker Demo", "datePickerSection",
            demoPicker, datePickerStatus);

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

        // ---- RadioButton / ToggleButton demo --------------------------------
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton radio1 = new RadioButton("Option A");
        radio1.setId("radioA");
        radio1.setToggleGroup(radioGroup);
        radio1.setSelected(true);
        RadioButton radio2 = new RadioButton("Option B");
        radio2.setId("radioB");
        radio2.setToggleGroup(radioGroup);
        ToggleButton toggleBtn = new ToggleButton("Toggle Me");
        toggleBtn.setId("toggleButton");
        Label radioStatus = new Label("Radio: A selected");
        radioStatus.setId("radioStatus");
        radioGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == radio1) radioStatus.setText("Radio: A selected");
            else if (n == radio2) radioStatus.setText("Radio: B selected");
        });
        toggleBtn.selectedProperty().addListener((obs, o, n) ->
            radioStatus.setText(radioStatus.getText() + " | Toggle: " + (n ? "on" : "off")));

        VBox radioToggleSection = section("RadioButton / ToggleButton Demo", "radioToggleSection",
            radio1, radio2, toggleBtn, radioStatus);

        // ---- Slider / Spinner demo ------------------------------------------
        Slider demoSlider = new Slider(0, 100, 25);
        demoSlider.setId("demoSlider");
        demoSlider.setShowTickLabels(true);
        demoSlider.setShowTickMarks(true);
        Label sliderValue = new Label("Slider: 25.0");
        sliderValue.setId("sliderValue");
        demoSlider.valueProperty().addListener((obs, o, n) ->
            sliderValue.setText(String.format("Slider: %.1f", n.doubleValue())));

        Spinner<Integer> demoSpinner = new Spinner<>(0, 100, 10, 1);
        demoSpinner.setId("demoSpinner");
        demoSpinner.setEditable(true);
        Label spinnerValue = new Label("Spinner: 10");
        spinnerValue.setId("spinnerValue");
        demoSpinner.valueProperty().addListener((obs, o, n) ->
            spinnerValue.setText("Spinner: " + n));

        VBox sliderSpinnerSection = section("Slider / Spinner Demo", "sliderSpinnerSection",
            demoSlider, sliderValue, demoSpinner, spinnerValue);

        // ---- ProgressBar demo -----------------------------------------------
        ProgressBar demoProgressBar = new ProgressBar(0.4);
        demoProgressBar.setId("demoProgressBar");
        demoProgressBar.setPrefWidth(300);
        ProgressIndicator demoProgressIndicator = new ProgressIndicator(0.7);
        demoProgressIndicator.setId("demoProgressIndicator");
        Label progressLabel = new Label("Bar: 40%  |  Indicator: 70%");
        progressLabel.setId("progressLabel");

        VBox progressSection = section("ProgressBar Demo", "progressSection",
            demoProgressBar, demoProgressIndicator, progressLabel);

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

        // ---- TextArea demo --------------------------------------------------
        TextArea demoTextArea = new TextArea("Hello\nWorld");
        demoTextArea.setId("demoTextArea");
        demoTextArea.setPrefRowCount(4);
        VBox textAreaSection = section("TextArea Demo", "textAreaSection", demoTextArea);

        // ---- PasswordField demo ---------------------------------------------
        PasswordField demoPasswordField = new PasswordField();
        demoPasswordField.setId("demoPasswordField");
        demoPasswordField.setPromptText("Enter password");
        VBox passwordFieldSection = section("PasswordField Demo", "passwordFieldSection", demoPasswordField);

        // ---- Hyperlink demo -------------------------------------------------
        Hyperlink demoHyperlink = new Hyperlink("Click me");
        demoHyperlink.setId("demoHyperlink");
        VBox hyperlinkSection = section("Hyperlink Demo", "hyperlinkSection", demoHyperlink);

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
            gridSection,
            contextMenuSection,
            menuBarSection,
            datePickerSection,
            dialogSection,
            alertSection,
            radioToggleSection,
            sliderSpinnerSection,
            progressSection,
            tabSection,
            textAreaSection,
            passwordFieldSection,
            hyperlinkSection
        );
        root.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setId("demoScrollPane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(scrollPane, 720, 760);
        primaryStage.setTitle("OmniUI Login Demo");
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
