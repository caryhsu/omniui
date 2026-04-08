package dev.omniui.demo.dynamicfxml.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FormController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button submitBtn;
    @FXML private Button clearBtn;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(
                "Admin", "Editor", "Viewer", "Guest"));
        roleCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSubmit() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role  = roleCombo.getValue();

        if (name.isEmpty() || email.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("⚠ Name and Email are required.");
            return;
        }
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText("✔ Submitted — " + name + " <" + email + "> [" + role + "]");
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        emailField.clear();
        roleCombo.getSelectionModel().selectFirst();
        statusLabel.setText("");
    }
}
