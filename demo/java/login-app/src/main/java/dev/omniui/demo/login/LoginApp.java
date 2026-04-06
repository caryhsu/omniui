package dev.omniui.demo.login;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Standalone Login demo app.
 *
 * Layout:
 *   ┌───────────────────────────┐
 *   │  OmniUI — Login Demo      │
 *   │  [username]               │
 *   │  [password]               │
 *   │  [Login]                  │
 *   │  status: Idle             │
 *   └───────────────────────────┘
 *
 * Node IDs:
 *   username    — TextField
 *   password    — PasswordField
 *   loginButton — Button
 *   status      — Label (shows "Success" or "Failed")
 *
 * Valid credentials: admin / 1234
 */
public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("OmniUI — Login Demo");
        title.setId("appTitle");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

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

        VBox root = new VBox(12, title, username, password, loginButton, status);
        root.setPadding(new Insets(24));
        root.setPrefWidth(320);

        Scene scene = new Scene(root, 360, 220);
        stage.setTitle("OmniUI Login Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
