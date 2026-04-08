package dev.omniui.demo.dynamicfxml.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ListController {

    @FXML private TextField newItemField;
    @FXML private Button addBtn;
    @FXML private ListView<String> itemList;
    @FXML private Button removeBtn;
    @FXML private Button clearBtn;

    private final ObservableList<String> items = FXCollections.observableArrayList(
            "Buy groceries", "Write tests", "Review pull request", "Update docs");

    @FXML
    public void initialize() {
        itemList.setItems(items);
    }

    @FXML
    private void handleAdd() {
        String text = newItemField.getText().trim();
        if (text.isEmpty()) return;
        items.add(text);
        newItemField.clear();
    }

    @FXML
    private void handleRemove() {
        int idx = itemList.getSelectionModel().getSelectedIndex();
        if (idx >= 0) items.remove(idx);
    }

    @FXML
    private void handleClear() {
        items.clear();
    }
}
