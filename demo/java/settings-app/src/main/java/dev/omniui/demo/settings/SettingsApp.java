package dev.omniui.demo.settings;

import dev.omniui.agent.OmniUiJavaAgent;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SettingsApp extends Application {

    // ── Account tab fields ─────────────────────────────────────────────────
    private TextField     usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField     emailField;
    private ComboBox<String> roleCombo;
    private CheckBox      twoFaCheck;

    // ── Appearance tab fields ──────────────────────────────────────────────
    private ComboBox<String> themeCombo;
    private Slider        fontSizeSlider;
    private Label         fontSizeLabel;
    private RadioButton   langEnglish;
    private RadioButton   langChinese;
    private RadioButton   langJapanese;
    private ToggleButton  compactModeToggle;
    private CheckBox      showToolbarCheck;
    private CheckBox      showStatusBarCheck;

    // ── Notifications tab fields ───────────────────────────────────────────
    private CheckBox      emailNotifCheck;
    private CheckBox      desktopNotifCheck;
    private Spinner<Integer> reminderSpinner;
    private ComboBox<String> soundCombo;
    private ToggleButton  dndToggle;
    private Label         dndStatusLabel;

    // ── Bottom bar ─────────────────────────────────────────────────────────
    private Label         statusLabel;

    @Override
    public void start(Stage stage) {
        OmniUiJavaAgent.startServer("port=48112");

        BorderPane root = new BorderPane();
        root.setCenter(buildTabPane());
        root.setBottom(buildBottomBar());

        stage.setTitle("Settings");
        stage.setScene(new Scene(root, 560, 480));
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

        tabPane.getTabs().addAll(accountTab, appearanceTab, notifTab);
        return tabPane;
    }

    // ── Account tab ────────────────────────────────────────────────────────

    private ScrollPane buildAccountTab() {
        usernameField = new TextField("john_doe");
        usernameField.setId("usernameField");

        passwordField = new PasswordField();
        passwordField.setId("passwordField");
        passwordField.setPromptText("Enter new password");

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

        GridPane grid = formGrid();
        int row = 0;
        addRow(grid, row++, "Username:",         usernameField);
        addRow(grid, row++, "Password:",         passwordField);
        addRow(grid, row++, "Confirm Password:", confirmPasswordField);
        addRow(grid, row++, "Email:",            emailField);
        addRow(grid, row++, "Role:",             roleCombo);

        VBox content = new VBox(16, grid, twoFaCheck);
        content.setPadding(new Insets(20));
        return new ScrollPane(content);
    }

    // ── Appearance tab ─────────────────────────────────────────────────────

    private ScrollPane buildAppearanceTab() {
        themeCombo = new ComboBox<>(FXCollections.observableArrayList("Light", "Dark", "System"));
        themeCombo.setId("themeCombo");
        themeCombo.getSelectionModel().select("System");
        themeCombo.setMaxWidth(Double.MAX_VALUE);

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

        showToolbarCheck   = new CheckBox("Show Toolbar");
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
        addRow(grid, row++, "Theme:",      themeCombo);
        addRow(grid, row++, "Font Size:",  fontRow);
        addRow(grid, row++, "Language:",   langBox);
        addRow(grid, row++, "Mode:",       compactModeToggle);

        VBox content = new VBox(16, grid, showToolbarCheck, showStatusBarCheck);
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

        dndToggle = new ToggleButton("Do Not Disturb");
        dndToggle.setId("dndToggle");

        dndStatusLabel = new Label("DND: Off");
        dndStatusLabel.setId("dndStatusLabel");
        dndToggle.selectedProperty().addListener((obs, o, on) ->
                dndStatusLabel.setText(on ? "DND: On" : "DND: Off"));

        HBox dndRow = new HBox(12, dndToggle, dndStatusLabel);
        dndRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = formGrid();
        int row = 0;
        addRow(grid, row++, "Reminder every:", new HBox(8, reminderSpinner, new Label("min")));
        addRow(grid, row++, "Sound:",          soundCombo);
        addRow(grid, row++, "Disturbance:",    dndRow);

        VBox content = new VBox(16, emailNotifCheck, desktopNotifCheck, grid);
        content.setPadding(new Insets(20));
        return new ScrollPane(content);
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
        usernameField.setText("john_doe");
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.setText("john@example.com");
        roleCombo.getSelectionModel().select("Editor");
        twoFaCheck.setSelected(false);

        themeCombo.getSelectionModel().select("System");
        fontSizeSlider.setValue(12);
        langEnglish.setSelected(true);
        compactModeToggle.setSelected(false);
        showToolbarCheck.setSelected(true);
        showStatusBarCheck.setSelected(true);

        emailNotifCheck.setSelected(true);
        desktopNotifCheck.setSelected(true);
        reminderSpinner.getValueFactory().setValue(15);
        soundCombo.getSelectionModel().select("Chime");
        dndToggle.setSelected(false);

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

    public static void main(String[] args) {
        launch(args);
    }
}
