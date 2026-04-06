package dev.omniui.demo.todo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TodoDemoApp extends Application {

    private final ObservableList<Task> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Task> displayList = FXCollections.observableArrayList();

    private ListView<Task> taskList;
    private ToggleButton   showCompleted;
    private TextField      searchField;

    @Override
    public void start(Stage stage) {

        // ── ToolBar ───────────────────────────────────────────────────────────
        Button addButton = new Button("＋ Add");
        addButton.setId("addButton");

        Button clearButton = new Button("⊘ Clear All");
        clearButton.setId("clearButton");

        showCompleted = new ToggleButton("☑ Show Completed");
        showCompleted.setId("showCompleted");

        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("🔍 Search...");
        searchField.setPrefWidth(180);

        ToolBar toolBar = new ToolBar(
            addButton, clearButton,
            new Separator(),
            showCompleted, searchField
        );

        // ── Center: ListView ──────────────────────────────────────────────────
        taskList = new ListView<>(displayList);
        taskList.setId("taskList");
        taskList.setCellFactory(lv -> new TaskCell(
            this::applyFilter,
            this::handleEditTask,
            this::handleDeleteTask
        ));

        // ── Root layout ───────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(taskList);

        // ── Wiring ────────────────────────────────────────────────────────────
        addButton.setOnAction(e -> handleAdd());
        clearButton.setOnAction(e -> handleClearAll());
        showCompleted.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        applyFilter();

        Scene scene = new Scene(root, 720, 480);
        stage.setTitle("OmniUI Todo Demo");
        stage.setScene(scene);
        stage.show();
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleAdd() {
        // Platform.runLater defers dialog so the click HTTP response is returned first,
        // preventing the onFxThread latch from blocking indefinitely.
        Platform.runLater(() ->
            showTaskDialog(null).ifPresent(task -> {
                masterList.add(task);
                applyFilter();
            })
        );
    }

    private void handleEditTask(Task task) {
        Platform.runLater(() ->
            showTaskDialog(task).ifPresent(updated -> {
                task.setTitle(updated.getTitle());
                task.setPriority(updated.getPriority());
                task.setDueDate(updated.getDueDate());
                applyFilter();
            })
        );
    }

    private void handleDeleteTask(Task task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Task");
            alert.setHeaderText(null);
            alert.setContentText("Delete \"" + task.getTitle() + "\"?");
            alert.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    masterList.remove(task);
                    applyFilter();
                }
            });
        });
    }

    private void handleClearAll() {
        masterList.clear();
        showCompleted.setSelected(false);
        searchField.clear();
        applyFilter();
    }

    // ── Dialog ────────────────────────────────────────────────────────────────

    private Optional<Task> showTaskDialog(Task existing) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Task" : "Edit Task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(existing != null ? existing.getTitle() : "");
        titleField.setId("dialogTitleField");
        titleField.setPromptText("Task title...");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.setId("dialogPriorityCombo");
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue(existing != null ? existing.getPriority() : "Medium");

        DatePicker datePicker = new DatePicker();
        datePicker.setId("dialogDatePicker");
        if (existing != null && !existing.getDueDate().isEmpty()) {
            try { datePicker.setValue(LocalDate.parse(existing.getDueDate())); }
            catch (Exception ignored) {}
        }

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.disableProperty().bind(titleField.textProperty().isEmpty());

        VBox content = new VBox(6,
            new Label("Title:"),    titleField,
            new Label("Priority:"), priorityBox,
            new Label("Due Date:"), datePicker
        );
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(320);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String title = titleField.getText().trim();
            if (title.isEmpty()) return null;
            String priority = priorityBox.getValue() != null ? priorityBox.getValue() : "Medium";
            String date = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
            return new Task(title, priority, date);
        });

        return dialog.showAndWait();
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    void applyFilter() {
        String  query    = searchField != null ? searchField.getText().toLowerCase() : "";
        boolean showDone = showCompleted != null && showCompleted.isSelected();
        List<Task> filtered = masterList.stream()
            .filter(t -> (showDone || !t.isCompleted())
                      && t.getTitle().toLowerCase().contains(query))
            .collect(Collectors.toList());
        displayList.setAll(filtered);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

