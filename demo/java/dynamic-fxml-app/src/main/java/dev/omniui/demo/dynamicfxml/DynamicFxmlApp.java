package dev.omniui.demo.dynamicfxml;

import dev.omniui.agent.OmniUiJavaAgent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class DynamicFxmlApp extends Application {

    private BorderPane contentArea;
    private Label statusBar;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        OmniUiJavaAgent.startServer("port=48110");

        BorderPane root = new BorderPane();

        // ── Fixed top: MenuBar + ToolBar ──────────────────────────────────
        VBox topArea = new VBox();
        topArea.getChildren().addAll(buildMenuBar(), buildToolBar());
        root.setTop(topArea);

        // ── Dynamic center ────────────────────────────────────────────────
        contentArea = new BorderPane();
        Label welcome = new Label("Select a view from the toolbar or View menu.");
        welcome.setStyle("-fx-font-size: 14; -fx-text-fill: gray;");
        BorderPane.setMargin(welcome, new Insets(40));
        contentArea.setCenter(welcome);
        root.setCenter(contentArea);

        // ── Status bar ────────────────────────────────────────────────────
        statusBar = new Label("Ready — no view loaded");
        statusBar.setPadding(new Insets(4, 10, 4, 10));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        root.setBottom(statusBar);

        stage.setTitle("Dynamic FXML Viewer");
        stage.setScene(new Scene(root, 800, 560));
        stage.show();
    }

    // ── MenuBar ────────────────────────────────────────────────────────────

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        fileMenu.setId("fileMenu");

        MenuItem openItem = new MenuItem("Open FXML…");
        openItem.setId("openFxmlItem");
        openItem.setOnAction(e -> handleOpenFxml());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setId("exitItem");
        exitItem.setOnAction(e -> primaryStage.close());

        fileMenu.getItems().addAll(openItem, new SeparatorMenuItem(), exitItem);

        // View menu
        Menu viewMenu = new Menu("View");
        viewMenu.setId("viewMenu");

        MenuItem formItem = new MenuItem("Form View");
        formItem.setId("formMenuItem");
        formItem.setOnAction(e -> loadBuiltin("views/form.fxml", "Form View"));

        MenuItem dashItem = new MenuItem("Dashboard");
        dashItem.setId("dashboardMenuItem");
        dashItem.setOnAction(e -> loadBuiltin("views/dashboard.fxml", "Dashboard"));

        MenuItem listItem = new MenuItem("Task List");
        listItem.setId("listMenuItem");
        listItem.setOnAction(e -> loadBuiltin("views/list.fxml", "Task List"));

        viewMenu.getItems().addAll(formItem, dashItem, listItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu);
        return menuBar;
    }

    // ── ToolBar ────────────────────────────────────────────────────────────

    private ToolBar buildToolBar() {
        Button formBtn = new Button("📋 Form");
        formBtn.setId("formBtn");
        formBtn.setOnAction(e -> loadBuiltin("views/form.fxml", "Form View"));

        Button dashBtn = new Button("📊 Dashboard");
        dashBtn.setId("dashboardBtn");
        dashBtn.setOnAction(e -> loadBuiltin("views/dashboard.fxml", "Dashboard"));

        Button listBtn = new Button("📝 Task List");
        listBtn.setId("listBtn");
        listBtn.setOnAction(e -> loadBuiltin("views/list.fxml", "Task List"));

        Button openBtn = new Button("📂 Open FXML…");
        openBtn.setId("openBtn");
        openBtn.setOnAction(e -> handleOpenFxml());

        ToolBar toolBar = new ToolBar(formBtn, dashBtn, listBtn, new Separator(), openBtn);
        return toolBar;
    }

    // ── FXML loading ───────────────────────────────────────────────────────

    private void loadBuiltin(String resourcePath, String viewName) {
        URL url = DynamicFxmlApp.class.getResource(resourcePath);
        if (url == null) {
            statusBar.setText("❌ Built-in resource not found: " + resourcePath);
            return;
        }
        loadFxml(url, viewName);
    }

    private void handleOpenFxml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open FXML File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("FXML Files", "*.fxml"));
        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) return;
        try {
            loadFxml(file.toURI().toURL(), file.getName());
        } catch (Exception ex) {
            statusBar.setText("❌ Failed to open: " + ex.getMessage());
        }
    }

    private void loadFxml(URL url, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            contentArea.setCenter(view);
            statusBar.setText("✔ Loaded: " + viewName + "   (" + url + ")");
        } catch (Exception ex) {
            statusBar.setText("❌ Load error: " + ex.getMessage());
            Label errLabel = new Label("Failed to load FXML:\n" + ex.getMessage());
            errLabel.setStyle("-fx-text-fill: red; -fx-padding: 20;");
            contentArea.setCenter(errLabel);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
