package dev.omniui.demo.todo;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class TaskCell extends ListCell<Task> {

    private final Runnable        onRefresh;
    private final Consumer<Task>  onEdit;
    private final Consumer<Task>  onDelete;

    public TaskCell(Runnable onRefresh, Consumer<Task> onEdit, Consumer<Task> onDelete) {
        this.onRefresh = onRefresh;
        this.onEdit    = onEdit;
        this.onDelete  = onDelete;
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        int idx = getIndex();

        CheckBox checkBox = new CheckBox();
        checkBox.setId("check_" + idx);
        checkBox.setSelected(task.isCompleted());
        checkBox.setOnAction(e -> {
            task.setCompleted(checkBox.isSelected());
            if (onRefresh != null) onRefresh.run();
        });

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle(task.isCompleted()
            ? "-fx-strikethrough: true; -fx-text-fill: #aaa; -fx-font-size: 13px;"
            : "-fx-font-size: 13px; -fx-text-fill: #222;");

        String color = switch (task.getPriority()) {
            case "High"   -> "#e74c3c";
            case "Medium" -> "#f39c12";
            default       -> "#27ae60";
        };
        Label priorityLabel = new Label(task.getPriority());
        priorityLabel.setStyle(
            "-fx-background-color: " + color + "; -fx-text-fill: white; "
            + "-fx-padding: 1 7; -fx-background-radius: 8; -fx-font-size: 11px;");

        Label dateLabel = new Label(task.getDueDate().isEmpty() ? "" : "📅 " + task.getDueDate());
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Button editBtn = new Button("✎");
        editBtn.setId("edit_" + idx);
        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 6;");
        editBtn.setOnAction(e -> { if (onEdit != null) onEdit.accept(task); });

        Button deleteBtn = new Button("🗑");
        deleteBtn.setId("delete_" + idx);
        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 6;");
        deleteBtn.setOnAction(e -> { if (onDelete != null) onDelete.accept(task); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(8, checkBox, titleLabel, spacer, priorityLabel, dateLabel, editBtn, deleteBtn);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 4 2;");

        setGraphic(cell);
        setText(null);
    }
}
