package dev.omniui.demo.explorer;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class ExplorerApp extends Application {

    // ── Mock file-system model ─────────────────────────────────────────────

    record FsNode(String name, String type, String size, List<FsNode> children) {
        boolean isFolder() { return "Folder".equals(type); }

        @Override
        public String toString() { return name; }
    }

    private static FsNode folder(String name, FsNode... children) {
        return new FsNode(name, "Folder", "—", List.of(children));
    }

    private static FsNode file(String name, String size) {
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toUpperCase() : "File";
        return new FsNode(name, ext + " File", size, List.of());
    }

    private static final FsNode ROOT = folder("My Computer",
        folder("Documents",
            file("report.pdf",   "1.2 MB"),
            file("notes.txt",    "14 KB"),
            folder("Projects",
                file("project-a.zip", "4.7 MB"),
                file("project-b.zip", "3.1 MB")
            )
        ),
        folder("Pictures",
            file("vacation.jpg", "3.4 MB"),
            file("profile.png",  "256 KB"),
            folder("Screenshots",
                file("screen1.png", "890 KB"),
                file("screen2.png", "720 KB")
            )
        ),
        folder("Downloads",
            file("installer.exe", "58 MB"),
            file("archive.zip",   "12 MB"),
            file("readme.md",     "8 KB")
        ),
        folder("Music",
            file("song1.mp3", "4.1 MB"),
            file("song2.mp3", "3.8 MB"),
            folder("Playlists",
                file("favorites.m3u", "2 KB")
            )
        )
    );

    // ── UI state ───────────────────────────────────────────────────────────

    private TableView<FsNode> fileTable;
    private TreeView<FsNode> folderTree;
    private Label statusBar;
    private SplitPane splitPane;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(buildMenuBar());
        root.setCenter(buildSplitPane());
        root.setBottom(buildStatusBar());

        stage.setTitle("Mini Explorer");
        stage.setScene(new Scene(root, 900, 580));
        stage.show();

        // expand root by default
        showFolder(ROOT);
        statusBar.setText("Ready — " + ROOT.children().size() + " top-level items");
    }

    // ── MenuBar ────────────────────────────────────────────────────────────

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.setId("fileMenu");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setId("exitItem");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().add(exitItem);

        Menu viewMenu = new Menu("View");
        viewMenu.setId("viewMenu");

        MenuItem expandAll = new MenuItem("Expand All");
        expandAll.setId("expandAllItem");
        expandAll.setOnAction(e -> setExpandedAll(folderTree.getRoot(), true));

        MenuItem collapseAll = new MenuItem("Collapse All");
        collapseAll.setId("collapseAllItem");
        collapseAll.setOnAction(e -> setExpandedAll(folderTree.getRoot(), false));

        viewMenu.getItems().addAll(expandAll, collapseAll);
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        return menuBar;
    }

    // ── SplitPane ──────────────────────────────────────────────────────────

    private SplitPane buildSplitPane() {
        splitPane = new SplitPane();
        splitPane.setId("mainSplitPane");
        splitPane.getItems().addAll(buildTree(), buildFileTable());
        splitPane.setDividerPositions(0.30);
        return splitPane;
    }

    // ── TreeView ───────────────────────────────────────────────────────────

    private TreeView<FsNode> buildTree() {
        TreeItem<FsNode> rootItem = toTreeItem(ROOT);
        rootItem.setExpanded(true);

        folderTree = new TreeView<>(rootItem);
        folderTree.setId("folderTree");
        folderTree.setShowRoot(true);
        folderTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(FsNode item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : (item.isFolder() ? "📁 " : "📄 ") + item.name());
            }
        });

        folderTree.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            FsNode node = sel.getValue();
            if (node.isFolder()) {
                showFolder(node);
            } else {
                showFile(node);
            }
        });

        return folderTree;
    }

    private TreeItem<FsNode> toTreeItem(FsNode node) {
        TreeItem<FsNode> item = new TreeItem<>(node);
        for (FsNode child : node.children()) {
            if (child.isFolder()) item.getChildren().add(toTreeItem(child));
        }
        return item;
    }

    // ── File TableView ─────────────────────────────────────────────────────

    private TableView<FsNode> buildFileTable() {
        fileTable = new TableView<>();
        fileTable.setId("fileTable");
        fileTable.setPlaceholder(new Label("Select a folder to view its contents."));

        TableColumn<FsNode, String> nameCol = new TableColumn<>("Name");
        nameCol.setId("colName");
        nameCol.setPrefWidth(280);
        nameCol.setCellValueFactory(r -> new SimpleStringProperty(
                (r.getValue().isFolder() ? "📁 " : "📄 ") + r.getValue().name()));

        TableColumn<FsNode, String> typeCol = new TableColumn<>("Type");
        typeCol.setId("colType");
        typeCol.setPrefWidth(120);
        typeCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().type()));

        TableColumn<FsNode, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setId("colSize");
        sizeCol.setPrefWidth(90);
        sizeCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().size()));

        fileTable.getColumns().addAll(nameCol, typeCol, sizeCol);

        // Double-click a folder row → navigate into it in the tree
        fileTable.setRowFactory(tv -> {
            TableRow<FsNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    FsNode node = row.getItem();
                    if (node.isFolder()) showFolder(node);
                }
            });
            return row;
        });

        return fileTable;
    }

    // ── Status bar ─────────────────────────────────────────────────────────

    private Label buildStatusBar() {
        statusBar = new Label("");
        statusBar.setId("statusBar");
        statusBar.setPadding(new Insets(4, 10, 4, 10));
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setStyle(
                "-fx-background-color: #f0f0f0;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 1 0 0 0;");
        return statusBar;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void showFolder(FsNode folder) {
        ObservableList<FsNode> items = FXCollections.observableArrayList(folder.children());
        fileTable.setItems(items);
        statusBar.setText("📁 " + folder.name() + "   — " + items.size() + " item(s)");
    }

    private void showFile(FsNode file) {
        statusBar.setText("📄 " + file.name() + "   " + file.size());
    }

    private void setExpandedAll(TreeItem<FsNode> item, boolean expanded) {
        if (item == null) return;
        item.setExpanded(expanded);
        for (TreeItem<FsNode> child : item.getChildren()) {
            setExpandedAll(child, expanded);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
