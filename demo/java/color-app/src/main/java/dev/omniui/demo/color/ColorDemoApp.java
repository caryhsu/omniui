package dev.omniui.demo.color;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ColorDemoApp extends Application {

    @Override
    public void start(Stage stage) {

        Label heading = new Label("Color Picker");
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ColorPicker demoPicker = new ColorPicker(Color.WHITE);
        demoPicker.setId("demoPicker");
        demoPicker.setPrefWidth(220);

        Label colorResult = new Label("");
        colorResult.setId("colorResult");
        colorResult.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Button resetColorButton = new Button("Reset");
        resetColorButton.setId("resetColorButton");
        resetColorButton.setStyle("-fx-font-size: 13px; -fx-padding: 6 18;");

        demoPicker.setOnAction(e -> {
            Color c = demoPicker.getValue();
            if (c != null) {
                String hex = String.format("#%02x%02x%02x",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));
                colorResult.setText("Selected: " + hex);
            }
        });

        resetColorButton.setOnAction(e -> {
            demoPicker.setValue(Color.WHITE);
            colorResult.setText("");
        });

        VBox root = new VBox(16, heading, demoPicker, colorResult, resetColorButton);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 340, 220);
        stage.setTitle("OmniUI Color Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
