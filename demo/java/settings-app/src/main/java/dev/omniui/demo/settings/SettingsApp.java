package dev.omniui.demo.settings;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SettingsApp extends Application {

    // ── Account tab fields ─────────────────────────────────────────────────
    private TextField        usernameField;
    private PasswordField    passwordField;
    private PasswordField    confirmPasswordField;
    private TextField        emailField;
    private ComboBox<String> roleCombo;
    private CheckBox         twoFaCheck;
    private TextArea         bioField;

    // ── Appearance tab fields ──────────────────────────────────────────────
    private ComboBox<String>  themeCombo;
    private ColorPicker       themePicker;
    private ChoiceBox<String> densityChoiceBox;
    private Slider            fontSizeSlider;
    private Label             fontSizeLabel;
    private RadioButton       langEnglish;
    private RadioButton       langChinese;
    private RadioButton       langJapanese;
    private ToggleButton      compactModeToggle;
    private CheckBox          showToolbarCheck;
    private CheckBox          showStatusBarCheck;

    // ── Notifications tab fields ───────────────────────────────────────────
    private CheckBox          emailNotifCheck;
    private CheckBox          desktopNotifCheck;
    private Spinner<Integer>  reminderSpinner;
    private ComboBox<String>  soundCombo;
    private ListView<String>  notifTypesList;
    private ToggleButton      dndToggle;
    private Label             dndStatusLabel;
    private ProgressIndicator testNotifIndicator;

    // ── Advanced tab fields ────────────────────────────────────────────────
    private Slider       cpuSlider;
    private ProgressBar  cpuUsageBar;
    private TextField    proxyField;
    private CheckBox     enableProxyCheck;
    private ToggleButton debugModeToggle;
    private TextArea     logsPreview;

    // ── Bottom bar ─────────────────────────────────────────────────────────
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setCenter(buildTabPane());
        root.setBottom(buildBottomBar());

        stage.setTitle("Settings");
        stage.setScene(new Scene(root, 640, 560));
        stage.show();
    }

    // ── TabPane ────────────────────────────────────────────────────────────

    private TabPane buildTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setId("settingsTabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab accountTab = new Tab("Account", buildAccountTab());
        accountTab.setId("accountTab");

        Tab appearanceTab = new Tab("Appearance", buildAppearanceTab());
        appearanceTab.setId("appearanceTab");

        Tab notifTab = new Tab("Notifications", buildNotificationsTab());
        notifTab.setId("notificationsTab");

        Tab advancedTab = new Tab("Advanced", buildAdvancedTab());
        advancedTab.setId("advancedTab");

        Tab aboutTab = new Tab("About", buildAboutTab());
        aboutTab.setId("aboutTab");

        tabPane.getTabs().addAll(accountTab, appearanceTab, notifTab, advancedTab, aboutTab);
        return tabPane;
    }

    // ── Account tab ────────────────────────────────────────────────────────

    private ScrollPane buildAccountTab() {
        usernameField = new TextField("john_doe");
        usernameField.setId("usernameField");

        passwordField = new PasswordField();
        passwordField.setId("passwordField");
        passwordField.setPromptText("Enter new password");
        Tooltip.install(passwordField,
                new Tooltip("8+ chars, mix of upper, lower, digit & symbol"));

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setId("confirmPasswordField");
        confirmPasswordField.setPromptText("Confirm password");

        emailField = new TextField("john@example.com");
        emailField.setId("emailField");

        roleCombo = new ComboBox<>(FXCollections.observableArrayList("Admin", "Editor", "Viewer", "Guest"));
        roleCombo.setId("roleCombo");
        roleCombo.getSelectionModel().select("Editor");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        twoFaCheck = new CheckBox("Enable Two-Factor Authentication");
        twoFaCheck.setId("twoFaCheck");

        bioField = new TextArea("JavaFX developer & coffee enthusiast.");
        bioField.setId("bioField");
        bioField.setPrefRowCount(3);
        bioField.setWrapText(true);
        bioField.setPromptText("Tell us about yourself…");

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setId("forgotPasswordLink");
        forgotPasswordLink.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill: steelblue;");
            statusLabel.setText("Password reset email sent.");
        });

        GridPane grid = formGrid();
        int row = 0;
        addRow(grid, row++, "Username:",         usernameField);
        addRow(grid, row++, "Password:",         passwordField);
        addRow(grid, row++, "Confirm Password:", confirmPasswordField);
        addRow(grid, row++, "Email:",            emailField);
        addRow(grid, row++, "Role:",             roleCombo);
        addRow(grid, row++, "Bio:",              bioField);

        VBox content = new VBox(16, grid, twoFaCheck, forgotPasswordLink);
        content.setPadding(new Insets(20));
        return new ScrollPane(content);
    }

    // ── Appearance tab ─────────────────────────────────────────────────────

    private ScrollPane buildAppearanceTab() {
        themeCombo = new ComboBox<>(FXCollections.observableArrayList("Light", "Dark", "System"));
        themeCombo.setId("themeCombo");
        themeCombo.getSelectionModel().select("System");
        themeCombo.setMaxWidth(Double.MAX_VALUE);

        themePicker = new ColorPicker(Color.web("#2196F3"));
        themePicker.setId("themePicker");
        themePicker.setMaxWidth(Double.MAX_VALUE);

        fontSizeSlider = new Slider(8, 24, 12);
        fontSizeSlider.setId("fontSizeSlider");
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(4);
        fontSizeSlider.setBlockIncrement(1);

        fontSizeLabel = new Label("12 pt");
        fontSizeLabel.setId("fontSizeLabel");
        fontSizeSlider.valueProperty().addListener((obs, o, n) ->
                fontSizeLabel.setText(String.format("%.0f pt", n.doubleValue())));

        densityChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Compact", "Normal", "Comfortable"));
        densityChoiceBox.setId("densityChoiceBox");
        densityChoiceBox.getSelectionModel().select("Normal");
        densityChoiceBox.setMaxWidth(Double.MAX_VALUE);

        ToggleGroup langGroup = new ToggleGroup();
        langEnglish  = new RadioButton("English");  langEnglish.setId("langEnglish");
        langChinese  = new RadioButton("繁體中文");   langChinese.setId("langChinese");
        langJapanese = new RadioButton("日本語");     langJapanese.setId("langJapanese");
        langEnglish.setToggleGroup(langGroup);
        langChinese.setToggleGroup(langGroup);
        langJapanese.setToggleGroup(langGroup);
        langEnglish.setSelected(true);
        HBox langBox = new HBox(16, langEnglish, langChinese, langJapanese);

        compactModeToggle = new ToggleButton("Compact Mode");
        compactModeToggle.setId("compactModeToggle");

        showToolbarCheck = new CheckBox("Show Toolbar");
        showToolbarCheck.setId("showToolbarCheck");
        showToolbarCheck.setSelected(true);

        showStatusBarCheck = new CheckBox("Show Status Bar");
        showStatusBarCheck.setId("showStatusBarCheck");
        showStatusBarCheck.setSelected(true);

        HBox fontRow = new HBox(12, fontSizeSlider, fontSizeLabel);
        fontRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fontSizeSlider, Priority.ALWAYS);

        GridPane grid = formGrid();
        int row = 0;
        addRow(grid, row++, "Theme:",        themeCombo);
        addRow(grid, row++, "Accent Color:", themePicker);
        addRow(grid, row++, "Font Size:",    fontRow);
        addRow(grid, row++, "Density:",      densityChoiceBox);

        VBox content = new VBox(12,
                grid,
                new Separator(),
                labeled("Language:", langBox),
                labeled("Mode:",     compactModeToggle),
                showToolbarCheck,
                showStatusBarCheck);
        content.setPadding(new Insets(20));
        return new ScrollPane(content);
    }

    // ── Notifications tab ──────────────────────────────────────────────────

    private ScrollPane buildNotificationsTab() {
        emailNotifCheck = new CheckBox("Email notifications");
        emailNotifCheck.setId("emailNotifCheck");
        emailNotifCheck.setSelected(true);

        desktopNotifCheck = new CheckBox("Desktop notifications");
        desktopNotifCheck.setId("desktopNotifCheck");
        desktopNotifCheck.setSelected(true);

        reminderSpinner = new Spinner<>(1, 60, 15);
        reminderSpinner.setId("reminderSpinner");
        reminderSpinner.setEditable(true);
        reminderSpinner.setPrefWidth(90);

        soundCombo = new ComboBox<>(FXCollections.observableArrayList("None", "Chime", "Bell", "Beep"));
        soundCombo.setId("soundCombo");
        soundCombo.getSelectionModel().select("Chime");
        soundCombo.setMaxWidth(Double.MAX_VALUE);

        notifTypesList = new ListView<>(FXCollections.observableArrayList(
                "System Updates", "Security Alerts", "Messages", "Mentions", "Reminders", "Promotions"));
        notifTypesList.setId("notifTypesList");
        notifTypesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        notifTypesList.setPrefHeight(130);
        notifTypesList.getSelectionModel().selectIndices(0, 1, 2);

        dndToggle = new ToggleButton("Do Not Disturb");
        dndToggle.setId("dndToggle");

        dndStatusLabel = new Label("DND: Off");
        dndStatusLabel.setId("dndStatusLabel");
        dndToggle.selectedProperty().addListener((obs, o, on) ->
                dndStatusLabel.setText(on ? "DND: On" : "DND: Off"));

        testNotifIndicator = new ProgressIndicator(-1);
        testNotifIndicator.setId("testNotifIndicator");
        testNotifIndicator.setPrefSize(20, 20);
        testNotifIndicator.setVisible(false);

        Button testNotifBtn = new Button("Test Notification");
        testNotifBtn.setId("testNotifBtn");
        testNotifBtn.setOnAction(e -> {
            testNotifIndicator.setVisible(true);
            testNotifBtn.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                testNotifIndicator.setVisible(false);
                testNotifBtn.setDisable(false);
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("✔ Test notification sent.");
            });
            pause.play();
        });

        HBox dndRow = new HBox(12, dndToggle, dndStatusLabel);
        dndRow.setAlignment(Pos.CENTER_LEFT);

        HBox testRow = new HBox(10, testNotifBtn, testNotifIndicator);
        testRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = formGrid();
        int row = 0;
        addRow(grid, row++, "Reminder every:", new HBox(8, reminderSpinner, new Label("min")));
        addRow(grid, row++, "Sound:",          soundCombo);
        addRow(grid, row++, "Disturbance:",    dndRow);

        VBox content = new VBox(12,
                emailNotifCheck, desktopNotifCheck,
                grid,
                new Label("Notification Types:"), notifTypesList,
                testRow);
        content.setPadding(new Insets(20));
        return new ScrollPane(content);
    }

    // ── Advanced tab ───────────────────────────────────────────────────────

    private ScrollPane buildAdvancedTab() {
        // System pane
        cpuSlider = new Slider(10, 100, 50);
        cpuSlider.setId("cpuLimitSlider");
        cpuSlider.setShowTickLabels(true);
        cpuSlider.setMajorTickUnit(25);
        Label cpuLabel = new Label("50%");
        cpuSlider.valueProperty().addListener((obs, o, n) ->
                cpuLabel.setText(String.format("%.0f%%", n.doubleValue())));

        cpuUsageBar = new ProgressBar(0.35);
        cpuUsageBar.setId("cpuUsageBar");
        cpuUsageBar.setMaxWidth(Double.MAX_VALUE);

        HBox cpuRow = new HBox(8, cpuSlider, cpuLabel);
        cpuRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cpuSlider, Priority.ALWAYS);

        VBox systemBox = new VBox(8,
                new Label("CPU Limit:"), cpuRow,
                new Label("Current Usage:"), cpuUsageBar);
        systemBox.setPadding(new Insets(10));

        // Network pane
        proxyField = new TextField();
        proxyField.setId("proxyField");
        proxyField.setPromptText("http://proxy:8080");
        proxyField.setDisable(true);

        enableProxyCheck = new CheckBox("Enable Proxy");
        enableProxyCheck.setId("enableProxyCheck");
        enableProxyCheck.selectedProperty().addListener((obs, o, on) -> proxyField.setDisable(!on));

        VBox networkBox = new VBox(8, enableProxyCheck, labeled("Proxy URL:", proxyField));
        networkBox.setPadding(new Insets(10));

        // Debug pane
        debugModeToggle = new ToggleButton("Enable Debug Mode");
        debugModeToggle.setId("debugModeToggle");

        Button exportLogsBtn = new Button("Export Logs…");
        exportLogsBtn.setId("exportLogsBtn");
        exportLogsBtn.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill: steelblue;");
            statusLabel.setText("Logs exported.");
        });

        logsPreview = new TextArea("[INFO]  App started\n[DEBUG] Agent connected\n[INFO]  Scene loaded");
        logsPreview.setId("logsPreview");
        logsPreview.setPrefRowCount(4);
        logsPreview.setEditable(false);
        logsPreview.setStyle("-fx-font-family: monospace;");

        VBox debugBox = new VBox(8, debugModeToggle, exportLogsBtn, logsPreview);
        debugBox.setPadding(new Insets(10));

        TitledPane systemPane  = new TitledPane("System",  systemBox);  systemPane.setId("systemPane");
        TitledPane networkPane = new TitledPane("Network", networkBox); networkPane.setId("networkPane");
        TitledPane debugPane   = new TitledPane("Debug",   debugBox);   debugPane.setId("debugPane");

        Accordion accordion = new Accordion(systemPane, networkPane, debugPane);
        accordion.setId("advancedAccordion");
        accordion.setExpandedPane(systemPane);

        VBox content = new VBox(accordion);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    // ── About tab ──────────────────────────────────────────────────────────

    private VBox buildAboutTab() {
        ImageView logoView = new ImageView();
        logoView.setId("logoImage");
        logoView.setFitWidth(48);
        logoView.setFitHeight(48);
        logoView.setPreserveRatio(true);

        Label logoEmoji = new Label("⚙");
        logoEmoji.setStyle("-fx-font-size: 36; -fx-text-fill: white;");
        StackPane logoPlaceholder = new StackPane(logoView, logoEmoji);
        logoPlaceholder.setStyle("-fx-background-color: #1976D2; -fx-background-radius: 14;");
        logoPlaceholder.setPrefSize(72, 72);
        logoPlaceholder.setMaxSize(72, 72);

        Label appName = new Label("Settings Demo App");
        appName.setId("appNameLabel");
        appName.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label version = new Label("Version 1.0.0");
        version.setId("versionLabel");
        version.setStyle("-fx-text-fill: gray;");

        Hyperlink websiteLink = new Hyperlink("https://github.com/caryhsu/omniui");
        websiteLink.setId("websiteLink");
        websiteLink.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill: steelblue;");
            statusLabel.setText("Opening website…");
        });

        Button checkUpdateBtn = new Button("Check for Updates");
        checkUpdateBtn.setId("checkUpdateBtn");
        checkUpdateBtn.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("✔ You are on the latest version.");
        });

        VBox content = new VBox(16, logoPlaceholder, appName, version, websiteLink, checkUpdateBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        return content;
    }

    // ── Bottom bar ─────────────────────────────────────────────────────────

    private HBox buildBottomBar() {
        Button saveBtn = new Button("💾 Save");
        saveBtn.setId("saveBtn");
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> handleSave());

        Button resetBtn = new Button("↺ Reset");
        resetBtn.setId("resetBtn");
        resetBtn.setOnAction(e -> handleReset());

        statusLabel = new Label("");
        statusLabel.setId("statusLabel");

        HBox bar = new HBox(10, saveBtn, resetBtn, statusLabel);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    // ── Actions ────────────────────────────────────────────────────────────

    private void handleSave() {
        if (!passwordField.getText().isEmpty()
                && !passwordField.getText().equals(confirmPasswordField.getText())) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("⚠ Passwords do not match.");
            return;
        }
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText("✔ Settings saved.");
    }

    private void handleReset() {
        // Account
        usernameField.setText("john_doe");
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.setText("john@example.com");
        roleCombo.getSelectionModel().select("Editor");
        twoFaCheck.setSelected(false);
        bioField.setText("JavaFX developer & coffee enthusiast.");

        // Appearance
        themeCombo.getSelectionModel().select("System");
        themePicker.setValue(Color.web("#2196F3"));
        densityChoiceBox.getSelectionModel().select("Normal");
        fontSizeSlider.setValue(12);
        langEnglish.setSelected(true);
        compactModeToggle.setSelected(false);
        showToolbarCheck.setSelected(true);
        showStatusBarCheck.setSelected(true);

        // Notifications
        emailNotifCheck.setSelected(true);
        desktopNotifCheck.setSelected(true);
        reminderSpinner.getValueFactory().setValue(15);
        soundCombo.getSelectionModel().select("Chime");
        dndToggle.setSelected(false);
        notifTypesList.getSelectionModel().clearSelection();
        notifTypesList.getSelectionModel().selectIndices(0, 1, 2);

        // Advanced
        cpuSlider.setValue(50);
        cpuUsageBar.setProgress(0.35);
        enableProxyCheck.setSelected(false);
        proxyField.clear();
        debugModeToggle.setSelected(false);
        logsPreview.setText("[INFO]  App started\n[DEBUG] Agent connected\n[INFO]  Scene loaded");

        statusLabel.setStyle("-fx-text-fill: gray;");
        statusLabel.setText("↺ Settings reset to defaults.");
    }

    // ── Grid helpers ───────────────────────────────────────────────────────

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        ColumnConstraints label = new ColumnConstraints(140);
        ColumnConstraints field = new ColumnConstraints();
        field.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(label, field);
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        lbl.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(lbl, 0, row);
        GridPane.setConstraints(control, 1, row);
        grid.getChildren().addAll(lbl, control);
    }

    private HBox labeled(String text, javafx.scene.Node control) {
        Label lbl = new Label(text);
        lbl.setMinWidth(140);
        HBox row = new HBox(12, lbl, control);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
