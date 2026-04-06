package dev.omniui.demo.todo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TodoDemoApp extends Application {

    private final ObservableList<Task> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Task> displayList = FXCollections.observableArrayList();

    private ListView<Task>   taskList;
    private Button           editButton;
    private Button           deleteButton;
    private ToggleButton     showCompleted;
    private TextField        searchField;
    private TextField        taskTitleField;
    private ComboBox<String> priorityCombo;
    private DatePicker       dueDatePicker;
    private Button           addButton;

    @Override
    public void start(Stage stage) {

        // ── ToolBar ───────────────────────────────────────────────────────────
        editButton = new Button("✎ Edit");
        editButton.setId("editButton");

        deleteButton = new Button("🗑 Delete");
        deleteButton.setId("deleteButton");

        Button clearButton = new Button("⊘ Clear All");
        clearButton.setId("clearButton");

        showCompleted = new ToggleButton("☑ Show Completed");
        showCompleted.setId("showCompleted");

        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("🔍 Search...");
        searchField.setPrefWidth(180);

        ToolBar toolBar = new ToolBar(
            editButton, deleteButton, clearButton,
            new Separator(),
            showCompleted, searchField
        );

        // ── Center: ListView ──────────────────────────────────────────────────
        taskList = new ListView<>(displayList);
        taskList.setId("taskList");
        taskList.setCellFactory(lv -> new TaskCell(this::applyFilter));

        // ── Bottom: Input Panel ───────────────────────────────────────────────
        taskTitleField = new TextField();
        taskTitleField.setId("taskTitleField");
        taskTitleField.setPromptText("Task title...");
        taskTitleField.setPrefWidth(230);

        priorityCombo = new ComboBox<>();
        priorityCombo.setId("priorityCombo");
        priorityCombo.getItems().addAll("Low", "Medium", "High");
        priorityCombo.setValue("Medium");

        dueDatePicker = new DatePicker();
        dueDatePicker.setId("dueDatePicker");
        dueDatePicker.setPromptText("Due date");

        addButton = new Button("＋ Add");
        addButton.setId("addButton");
        addButton.setDefaultButton(true);
        // Disable Add when title is empty
        addButton.disableProperty().bind(
            taskTitleField.textProperty().isEmpty()
        );

        HBox inputPanel = new HBox(8, taskTitleField, priorityCombo, dueDatePicker, addButton);
        inputPanel.setPadding(new Insets(10, 12, 10, 12));
        inputPanel.setAlignment(Pos.CENTER_LEFT);
        inputPanel.setStyle(
            "-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // ── Root layout ───────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(taskList);
        root.setBottom(inputPanel);

        // ── Wiring ────────────────────────────────────────────────────────────
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        clearButton.setOnAction(e -> handleClearAll());
        showCompleted.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        taskList.getSelectionModel().selectedItemProperty()
            .addListener((obs, o, n) -> prefillInputPanel(n));

        applyFilter();

        Scene scene = new Scene(root, 720, 480);
        stage.setTitle("OmniUI Todo Demo");
        stage.setScene(scene);
        stage.show();

    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleAdd() {
        String title = taskTitleField.getText().trim();
        if (title.isEmpty()) return;
        String priority = priorityCombo.getValue() != null ? priorityCombo.getValue() : "Medium";
        String dueDate  = dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : "";
        masterList.add(new Task(title, priority, dueDate));
        clearInputPanel();
        applyFilter();
    }

    private void handleEdit() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String title = taskTitleField.getText().trim();
        if (title.isEmpty()) return;
        selected.setTitle(title);
        if (priorityCombo.getValue() != null) selected.setPriority(priorityCombo.getValue());
        if (dueDatePicker.getValue() != null)  selected.setDueDate(dueDatePicker.getValue().toString());
        applyFilter();
    }

    private void handleDelete() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        masterList.remove(selected);
        clearInputPanel();
        applyFilter();
    }

    private void handleClearAll() {
        masterList.clear();
        showCompleted.setSelected(false);
        searchField.clear();
        clearInputPanel();
        applyFilter();
    }

    // Package-private so TaskCell can call it via the Runnable lambda
    void applyFilter() {
        String  query    = searchField != null ? searchField.getText().toLowerCase() : "";
        boolean showDone = showCompleted != null && showCompleted.isSelected();
        List<Task> filtered = masterList.stream()
            .filter(t -> (showDone || !t.isCompleted())
                      && t.getTitle().toLowerCase().contains(query))
            .collect(Collectors.toList());
        displayList.setAll(filtered);
    }

    private void prefillInputPanel(Task task) {
        if (task == null) return;
        taskTitleField.setText(task.getTitle());
        priorityCombo.setValue(task.getPriority());
        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            try { dueDatePicker.setValue(LocalDate.parse(task.getDueDate())); }
            catch (Exception ignored) { dueDatePicker.setValue(null); }
        } else {
            dueDatePicker.setValue(null);
        }
    }

    private void clearInputPanel() {
        taskTitleField.clear();
        priorityCombo.setValue("Medium");
        dueDatePicker.setValue(null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
