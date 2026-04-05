package dev.omniui.demo.drag;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone Drag & Drop demo app.
 *
 * Layout:
 *   ┌─────────────────────────────────────────────┐
 *   │  OmniUI — Drag & Drop Demo                  │
 *   │  [dragStatus label]                         │
 *   │                                             │
 *   │  ┌──────────────┐  ┌──────────────────────┐ │
 *   │  │  leftPanel   │  │     rightPanel       │ │
 *   │  │  Apple       │  │  (drop items here)   │ │
 *   │  │  Banana      │  │                      │ │
 *   │  │  Cherry      │  │                      │ │
 *   │  │  Date        │  │                      │ │
 *   │  │  Elderberry  │  │                      │ │
 *   │  └──────────────┘  └──────────────────────┘ │
 *   │  [Reset]                                    │
 *   └─────────────────────────────────────────────┘
 *
 * Each item in leftPanel has fx:id = "item_apple", "item_banana", etc.
 * The dragStatus label (id="dragStatus") shows what was last dragged.
 * The resetBtn (id="resetBtn") restores the initial state.
 *
 * Drag logic uses scene-level addEventFilter (capture phase) so both real mouse
 * drags and synthetic fireDragSequence events (fired on scene root) are handled.
 */
public class DragDropApp extends Application {

    private static final List<String[]> INITIAL_ITEMS = List.of(
        new String[]{"item_apple",      "Apple"},
        new String[]{"item_banana",     "Banana"},
        new String[]{"item_cherry",     "Cherry"},
        new String[]{"item_date",       "Date"},
        new String[]{"item_elderberry", "Elderberry"}
    );

    private VBox  leftPanel;
    private VBox  rightPanel;
    private Label dragStatus;

    // pending drag state (set on MOUSE_PRESSED, consumed on MOUSE_RELEASED)
    private String pendingId   = null;
    private String pendingText = null;

    @Override
    public void start(Stage stage) {
        // ── header ───────────────────────────────────────────────────────────
        Label title = new Label("OmniUI — Drag & Drop Demo");
        title.setId("appTitle");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        dragStatus = new Label("drag_status: idle");
        dragStatus.setId("dragStatus");
        dragStatus.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");

        // ── left panel ───────────────────────────────────────────────────────
        leftPanel = new VBox(6);
        leftPanel.setId("leftPanel");
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-border-color: #4a90d9; -fx-border-width: 2; -fx-background-color: #f0f6ff;");
        leftPanel.setPrefWidth(160);
        leftPanel.setMinHeight(200);

        Label leftTitle = new Label("Available");
        leftTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a90d9;");
        leftPanel.getChildren().add(leftTitle);
        for (String[] item : INITIAL_ITEMS) {
            leftPanel.getChildren().add(makeItem(item[0], item[1]));
        }

        // ── right panel ──────────────────────────────────────────────────────
        rightPanel = new VBox(6);
        rightPanel.setId("rightPanel");
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-border-color: #2a9d2a; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #f0fff0;");
        rightPanel.setPrefWidth(200);
        rightPanel.setMinHeight(200);

        Label rightTitle = new Label("Selected");
        rightTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a9d2a;");

        Label dropHint = new Label("Drop items here");
        dropHint.setId("dropHint");
        dropHint.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");

        rightPanel.getChildren().addAll(rightTitle, dropHint);

        // ── panels row ───────────────────────────────────────────────────────
        HBox panels = new HBox(30, leftPanel, rightPanel);
        panels.setAlignment(Pos.TOP_CENTER);

        // ── reset button ─────────────────────────────────────────────────────
        Button resetBtn = new Button("Reset");
        resetBtn.setId("resetBtn");
        resetBtn.setOnAction(e -> resetItems());

        // ── root layout ──────────────────────────────────────────────────────
        VBox root = new VBox(12, title, dragStatus, panels, resetBtn);
        root.setId("demoRoot");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 480, 380);

        // ── drag logic (capture phase — works for both real mouse and synthetic events)
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            pendingId   = null;
            pendingText = null;
            // Walk up from event target to find a draggable item label
            if (e.getTarget() instanceof javafx.scene.Node node) {
                javafx.scene.Node n = node;
                for (int depth = 0; n != null && depth < 5; depth++, n = n.getParent()) {
                    String id = n.getId();
                    if (id != null && id.startsWith("item_")) {
                        pendingId   = id;
                        pendingText = n instanceof Label l ? l.getText() : id;
                        dragStatus.setText("drag_status: dragging " + pendingText);
                        break;
                    }
                }
            }
            // Coordinate fallback: if target walk didn't work (synthetic event on root),
            // find which left-panel item contains the press coordinates.
            if (pendingId == null) {
                for (javafx.scene.Node child : leftPanel.getChildren()) {
                    String id = child.getId();
                    if (id == null || !id.startsWith("item_")) continue;
                    Bounds b = child.localToScene(child.getBoundsInLocal());
                    if (b.contains(e.getSceneX(), e.getSceneY())) {
                        pendingId   = id;
                        pendingText = child instanceof Label l ? l.getText() : id;
                        dragStatus.setText("drag_status: dragging " + pendingText);
                        break;
                    }
                }
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (pendingId == null) return;
            Bounds rightBounds = rightPanel.localToScene(rightPanel.getBoundsInLocal());
            if (rightBounds.contains(e.getSceneX(), e.getSceneY())) {
                moveItemToRight(pendingId, pendingText);
                dragStatus.setText("drag_status: dropped " + pendingText);
            } else {
                dragStatus.setText("drag_status: cancelled");
            }
            pendingId   = null;
            pendingText = null;
        });

        stage.setTitle("OmniUI Drag & Drop Demo");
        stage.setScene(scene);
        stage.show();
    }

    private Label makeItem(String id, String text) {
        Label lbl = new Label(text);
        lbl.setId(id);
        lbl.setStyle("-fx-background-color: #4a90d9; -fx-text-fill: white; "
                   + "-fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private void moveItemToRight(String id, String text) {
        // Remove from left panel
        leftPanel.getChildren().removeIf(n -> id.equals(n.getId()));
        // Remove drop hint if this is the first item dropped
        rightPanel.getChildren().removeIf(n -> "dropHint".equals(n.getId()));
        // Add to right panel (green style)
        Label lbl = new Label(text);
        lbl.setId(id + "_dropped");
        lbl.setStyle("-fx-background-color: #2a9d2a; -fx-text-fill: white; "
                   + "-fx-padding: 6 14; -fx-font-weight: bold;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        rightPanel.getChildren().add(lbl);
    }

    private void resetItems() {
        // Restore left panel items
        leftPanel.getChildren().clear();
        Label leftTitle = new Label("Available");
        leftTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a90d9;");
        leftPanel.getChildren().add(leftTitle);
        for (String[] item : INITIAL_ITEMS) {
            leftPanel.getChildren().add(makeItem(item[0], item[1]));
        }
        // Clear right panel
        rightPanel.getChildren().clear();
        Label rightTitle = new Label("Selected");
        rightTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a9d2a;");
        Label dropHint = new Label("Drop items here");
        dropHint.setId("dropHint");
        dropHint.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");
        rightPanel.getChildren().addAll(rightTitle, dropHint);
        dragStatus.setText("drag_status: idle");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
