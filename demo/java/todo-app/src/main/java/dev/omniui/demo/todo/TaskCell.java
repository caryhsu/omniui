package dev.omniui.demo.todo;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TaskCell extends ListCell<Task> {

    private final Runnable onToggle;

    public TaskCell(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        CheckBox checkBox = new CheckBox();
        checkBox.setId("check_" + getIndex());
        checkBox.setSelected(task.isCompleted());
        checkBox.setOnAction(e -> {
            task.setCompleted(checkBox.isSelected());
            if (onToggle != null) onToggle.run();
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(8, checkBox, titleLabel, spacer, priorityLabel, dateLabel);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 4 2;");

        setGraphic(cell);
        setText(null);
    }
}
