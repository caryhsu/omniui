package dev.omniui.demo.image;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        // Three drag sources — each shows a different image; sources don't change.
        // Drop target ImageView updates to show whichever source was dragged onto it.
        String[] sourceUrls = {
            "https://picsum.photos/seed/omniuisrc1/100/100",
            "https://picsum.photos/seed/omniuisrc2/100/100",
            "https://picsum.photos/seed/omniuisrc3/100/100",
        };
        String[] sourceIds = { "dragSource1", "dragSource2", "dragSource3" };

        Label dropTarget = new Label("Drop target");
        dropTarget.setId("dropTarget");
        dropTarget.setPrefSize(120, 120);
        dropTarget.setAlignment(Pos.CENTER);
        dropTarget.setStyle(
            "-fx-background-color: #f0f0f0; " +
            "-fx-border-color: #4a90d9; " +
            "-fx-border-style: dashed; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 4; " +
            "-fx-font-size: 12px; -fx-text-fill: #888;"
        );

        ImageView dropTargetImage = new ImageView();
        dropTargetImage.setId("dropTargetImage");
        dropTargetImage.setFitWidth(100);
        dropTargetImage.setFitHeight(100);
        dropTargetImage.setPreserveRatio(true);
        dropTargetImage.setPickOnBounds(true);

        // StackPane: dropTarget label behind, dropTargetImage in front
        javafx.scene.layout.StackPane dropPane = new javafx.scene.layout.StackPane(dropTarget, dropTargetImage);
        dropPane.setId("dropPane");
        dropPane.setPrefSize(120, 120);

        Label dropResult = new Label("");
        dropResult.setId("dropResult");
        dropResult.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        HBox sourcesRow = new HBox(16);
        sourcesRow.setAlignment(Pos.CENTER_LEFT);

        // Track which source is being dragged (index 1/2/3, or 0 = none)
        AtomicInteger dragIdx = new AtomicInteger(0);
        AtomicReference<Image> dragImage = new AtomicReference<>(null);

        for (int i = 0; i < 3; i++) {
            final String srcUrl = sourceUrls[i];
            final String srcId  = sourceIds[i];

            Image srcImg = new Image(srcUrl, 100, 100, true, true, true);
            ImageView srcView = new ImageView(srcImg);
            srcView.setId(srcId);
            srcView.setFitWidth(100);
            srcView.setFitHeight(100);
            srcView.setPreserveRatio(true);
            srcView.setPickOnBounds(true);
            srcView.setStyle(
                "-fx-border-color: #aaa; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);"
            );

            // Cursor: open hand on hover
            srcView.setOnMouseEntered(e -> srcView.setCursor(Cursor.OPEN_HAND));
            srcView.setOnMouseExited(e -> srcView.setCursor(Cursor.DEFAULT));

            sourcesRow.getChildren().add(srcView);
        }

        Label arrowLabel = new Label("→");
        arrowLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #888;");

        HBox dragRow = new HBox(24, sourcesRow, arrowLabel, dropPane);
        dragRow.setAlignment(Pos.CENTER_LEFT);

        VBox dragSection = section("Drag & Drop", "dragSection", dragRow, dropResult);

        // ── Root layout ───────────────────────────────────────────────────────
        VBox root = new VBox(18, imageSection, dragSection);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 650, 580);

        // ── Drag logic using scene-level capture-phase filters ────────────────
        // Works for both real mouse events and OmniUI synthetic events.
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            dragIdx.set(0);
            dragImage.set(null);
            // Try target-walk first (real mouse events set a proper target)
            if (e.getTarget() instanceof Node node) {
                Node n = node;
                for (int depth = 0; n != null && depth < 5; depth++, n = n.getParent()) {
                    String id = n.getId();
                    if (id != null && id.startsWith("dragSource")) {
                        int idx = Integer.parseInt(id.replace("dragSource", ""));
                        dragIdx.set(idx);
                        dragImage.set(((ImageView) sourcesRow.getChildren().get(idx - 1)).getImage());
                        scene.setCursor(Cursor.CLOSED_HAND);
                        break;
                    }
                }
            }
            // Coordinate fallback for synthetic events (target may be root/scene)
            if (dragIdx.get() == 0) {
                for (Node child : sourcesRow.getChildren()) {
                    String id = child.getId();
                    if (id == null || !id.startsWith("dragSource")) continue;
                    Bounds b = child.localToScene(child.getBoundsInLocal());
                    if (b.contains(e.getSceneX(), e.getSceneY())) {
                        int idx = Integer.parseInt(id.replace("dragSource", ""));
                        dragIdx.set(idx);
                        dragImage.set(((ImageView) child).getImage());
                        scene.setCursor(Cursor.CLOSED_HAND);
                        break;
                    }
                }
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            scene.setCursor(Cursor.DEFAULT);
            if (dragIdx.get() == 0) return;
            Bounds dropBounds = dropPane.localToScene(dropPane.getBoundsInLocal());
            if (dropBounds.contains(e.getSceneX(), e.getSceneY())) {
                int idx = dragIdx.get();
                Image img = dragImage.get();
                dropTargetImage.setImage(img);
                dropTarget.setText("");
                dropResult.setText("source" + idx + " dropped!");
            }
            dragIdx.set(0);
            dragImage.set(null);
        });

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
