package dev.omniui.demo.image;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class ImageDemoApp extends Application {

    private static final String VALID_URL =
        "https://picsum.photos/seed/omniuiv1/200/150";
    private static final String BROKEN_URL =
        "https://this-url-does-not-exist.example.invalid/broken.png";

    @Override
    public void start(Stage stage) {

        // ── Image Display section ─────────────────────────────────────────────
        Image validImg  = new Image(VALID_URL,  200, 150, true, true, true);
        Image brokenImg = new Image(BROKEN_URL, 200, 150, true, true, true);

        ImageView imageView1 = new ImageView(validImg);
        imageView1.setId("imageView1");
        imageView1.setFitWidth(200);
        imageView1.setFitHeight(150);
        imageView1.setPreserveRatio(true);
        imageView1.setPickOnBounds(true);
        imageView1.setStyle("-fx-border-color: #aaa; -fx-border-width: 1;");

        ImageView imageView2 = new ImageView(brokenImg);
        imageView2.setId("imageView2");
        imageView2.setFitWidth(200);
        imageView2.setFitHeight(150);
        imageView2.setPreserveRatio(true);
        imageView2.setPickOnBounds(true);
        imageView2.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1;");

        Label urlLabel = new Label("URL: (loading...)");
        urlLabel.setId("urlLabel");

        Label statusLabel = new Label("loading");
        statusLabel.setId("statusLabel");

        Button switchBtn = new Button("Switch Images");
        switchBtn.setId("switchBtn");
        switchBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 18;");
        switchBtn.setOnAction(e -> {
            Image tmp = imageView1.getImage();
            imageView1.setImage(imageView2.getImage());
            imageView2.setImage(tmp);
            Image cur = imageView1.getImage();
            urlLabel.setText("URL: " + (cur != null && cur.getUrl() != null ? cur.getUrl() : ""));
        });

        // Track when both images finish (loaded or error) → set statusLabel="ready"
        AtomicInteger doneCount = new AtomicInteger(0);
        Runnable onImageDone = () -> {
            if (doneCount.incrementAndGet() >= 2) {
                Platform.runLater(() -> {
                    urlLabel.setText("URL: " + VALID_URL);
                    statusLabel.setText("ready");
                });
            }
        };

        validImg.progressProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 1.0) onImageDone.run();
        });
        validImg.errorProperty().addListener((obs, o, n) -> {
            if (Boolean.TRUE.equals(n)) onImageDone.run();
        });
        brokenImg.progressProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 1.0) onImageDone.run();
        });
        brokenImg.errorProperty().addListener((obs, o, n) -> {
            if (Boolean.TRUE.equals(n)) onImageDone.run();
        });

        HBox imageRow = new HBox(16, imageView1, imageView2);
        imageRow.setAlignment(Pos.CENTER_LEFT);

        VBox imageSection = section("Image Display", "imageSection",
            imageRow, switchBtn, urlLabel, statusLabel);

        // ── Drag & Drop section ───────────────────────────────────────────────
        ImageView dragImageView = new ImageView(validImg);
        dragImageView.setId("dragImageView");
        dragImageView.setFitWidth(80);
        dragImageView.setFitHeight(80);
        dragImageView.setPreserveRatio(true);
        dragImageView.setPickOnBounds(true);
        dragImageView.setStyle("-fx-border-color: #4a90d9; -fx-border-width: 2; -fx-background-color: #e8f4fd;");

        Label dropZoneInner = new Label("Drop Here");
        dropZoneInner.setId("dropZoneLabel");
        dropZoneInner.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a90d9;");

        VBox dropZone = new VBox(dropZoneInner);
        dropZone.setId("dropZone");
        dropZone.setPrefSize(200, 100);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setStyle(
            "-fx-background-color: #e8f4fd; " +
            "-fx-border-color: #4a90d9; " +
            "-fx-border-style: dashed; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 4;"
        );

        Label dropResult = new Label("");
        dropResult.setId("dropResult");
        dropResult.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Detect drop: MOUSE_RELEASED fires on source node; PickResult gives the
        // node under the cursor — walk up to check if dropZone was the target.
        dragImageView.setOnMouseReleased(e -> {
            Node target = e.getPickResult().getIntersectedNode();
            while (target != null) {
                if (target == dropZone) {
                    Platform.runLater(() -> dropResult.setText("dropped!"));
                    break;
                }
                target = target.getParent();
            }
            e.consume();
        });

        HBox dragRow = new HBox(32, dragImageView, dropZone);
        dragRow.setAlignment(Pos.CENTER_LEFT);

        VBox dragSection = section("Drag & Drop", "dragSection", dragRow, dropResult);

        // ── Root layout ───────────────────────────────────────────────────────
        VBox root = new VBox(18, imageSection, dragSection);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("OmniUI Image Demo");
        stage.setScene(scene);
        stage.show();
    }

    private VBox section(String title, String id, Node... children) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        VBox box = new VBox(10, heading);
        box.getChildren().addAll(children);
        box.setId(id);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ddd; " +
                     "-fx-border-width: 1; -fx-border-radius: 4;");
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
