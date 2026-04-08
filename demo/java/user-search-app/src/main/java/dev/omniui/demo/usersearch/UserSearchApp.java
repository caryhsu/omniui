package dev.omniui.demo.usersearch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User Search demo app — paginated list with simulated DB query delay.
 *
 * Layout:
 *   ┌─────────────────────────────────────────┐
 *   │  OmniUI — User Search Demo              │
 *   │  Name: [_______]  Email: [__________]   │
 *   │                            [Search]     │
 *   │  status: Ready                          │
 *   │  ┌──┬─────────────┬───────────────┬───┐ │
 *   │  │ID│ Name        │ Email         │Role│ │
 *   │  ├──┼─────────────┼───────────────┼───┤ │
 *   │  │  │ ...         │ ...           │   │ │
 *   │  └──┴─────────────┴───────────────┴───┘ │
 *   │  [◀ Prev]  Page 1 / 5  [Next ▶]         │
 *   └─────────────────────────────────────────┘
 *
 * fx:id list:
 *   nameFilter    — TextField  (search by name, optional)
 *   emailFilter   — TextField  (search by email, optional)
 *   searchButton  — Button
 *   statusLabel   — Label
 *   userTable     — TableView
 *   prevButton    — Button
 *   nextButton    — Button
 *   pageLabel     — Label      ("Page X / Y")
 */
public class UserSearchApp extends Application {

    private static final int PAGE_SIZE = 5;

    // ---------- mock data ---------------------------------------------------
    private static final List<String[]> ALL_USERS = new ArrayList<>();
    static {
        String[][] seed = {
            {"1",  "Alice Chen",     "alice@example.com",   "Admin"},
            {"2",  "Bob Wang",       "bob@example.com",     "User"},
            {"3",  "Carol Liu",      "carol@example.com",   "User"},
            {"4",  "David Lee",      "david@example.com",   "Manager"},
            {"5",  "Emma Zhang",     "emma@example.com",    "User"},
            {"6",  "Frank Wu",       "frank@example.com",   "User"},
            {"7",  "Grace Yang",     "grace@example.com",   "Manager"},
            {"8",  "Henry Lin",      "henry@example.com",   "User"},
            {"9",  "Iris Huang",     "iris@example.com",    "Admin"},
            {"10", "Jack Zhou",      "jack@example.com",    "User"},
            {"11", "Karen Xu",       "karen@example.com",   "User"},
            {"12", "Leo Zheng",      "leo@example.com",     "Manager"},
            {"13", "Mia Sun",        "mia@example.com",     "User"},
            {"14", "Nathan Gao",     "nathan@example.com",  "User"},
            {"15", "Olivia He",      "olivia@example.com",  "Admin"},
            {"16", "Peter Ma",       "peter@example.com",   "User"},
            {"17", "Quinn Hu",       "quinn@example.com",   "Manager"},
            {"18", "Rachel Luo",     "rachel@example.com",  "User"},
            {"19", "Sam Deng",       "sam@example.com",     "User"},
            {"20", "Tina Feng",      "tina@example.com",    "User"},
            {"21", "Umar Tang",      "umar@example.com",    "Manager"},
            {"22", "Vera Cai",       "vera@example.com",    "User"},
            {"23", "Will Pan",       "will@example.com",    "User"},
            {"24", "Xia Song",       "xia@example.com",     "Admin"},
            {"25", "Yuki Jiang",     "yuki@example.com",    "User"},
        };
        for (String[] row : seed) ALL_USERS.add(row);
    }

    // ---------- state -------------------------------------------------------
    private List<String[]> currentResults = new ArrayList<>();
    private int currentPage = 0;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "query-thread");
                t.setDaemon(true);
                return t;
            });

    // ---------- UI nodes ----------------------------------------------------
    private TextField nameFilter;
    private TextField emailFilter;
    private Button searchButton;
    private Label statusLabel;
    private TableView<String[]> userTable;
    private Button prevButton;
    private Button nextButton;
    private Label pageLabel;

    @Override
    public void start(Stage stage) {
        // ---- title ---------------------------------------------------------
        Label title = new Label("OmniUI — User Search Demo");
        title.setId("appTitle");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // ---- search form ---------------------------------------------------
        nameFilter = new TextField();
        nameFilter.setId("nameFilter");
        nameFilter.setPromptText("Name (optional)");
        nameFilter.setPrefWidth(160);

        emailFilter = new TextField();
        emailFilter.setId("emailFilter");
        emailFilter.setPromptText("Email (optional)");
        emailFilter.setPrefWidth(200);

        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> runSearch());
        // Enter key in either field also triggers search
        nameFilter.setOnAction(e -> runSearch());
        emailFilter.setOnAction(e -> runSearch());

        HBox searchForm = new HBox(10,
                new Label("Name:"), nameFilter,
                new Label("Email:"), emailFilter,
                searchButton);
        searchForm.setAlignment(Pos.CENTER_LEFT);

        // ---- status --------------------------------------------------------
        statusLabel = new Label("Ready — enter a filter or leave blank to search all.");
        statusLabel.setId("statusLabel");

        // ---- table ---------------------------------------------------------
        userTable = new TableView<>();
        userTable.setId("userTable");
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPrefHeight(200);
        userTable.setPlaceholder(new Label("No results — press Search to query."));

        TableColumn<String[], Integer> colId = new TableColumn<>("ID");
        colId.setId("colId");
        colId.setMaxWidth(50);
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(
                Integer.parseInt(c.getValue()[0])).asObject());

        TableColumn<String[], String> colName = new TableColumn<>("Name");
        colName.setId("colName");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));

        TableColumn<String[], String> colEmail = new TableColumn<>("Email");
        colEmail.setId("colEmail");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[2]));

        TableColumn<String[], String> colRole = new TableColumn<>("Role");
        colRole.setId("colRole");
        colRole.setMaxWidth(90);
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[3]));

        userTable.getColumns().addAll(colId, colName, colEmail, colRole);

        // ---- pagination ----------------------------------------------------
        prevButton = new Button("◀ Prev");
        prevButton.setId("prevButton");
        prevButton.setDisable(true);
        prevButton.setOnAction(e -> {
            currentPage--;
            showPage();
        });

        nextButton = new Button("Next ▶");
        nextButton.setId("nextButton");
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> {
            currentPage++;
            showPage();
        });

        pageLabel = new Label("Page — / —");
        pageLabel.setId("pageLabel");
        pageLabel.setMinWidth(100);
        pageLabel.setAlignment(Pos.CENTER);

        HBox pagination = new HBox(10, prevButton, pageLabel, nextButton);
        pagination.setAlignment(Pos.CENTER);

        // ---- root ----------------------------------------------------------
        VBox root = new VBox(12, title, searchForm, statusLabel, userTable, pagination);
        root.setPadding(new Insets(20));
        root.setPrefWidth(620);

        Scene scene = new Scene(root, 640, 420);
        stage.setTitle("OmniUI User Search Demo");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> scheduler.shutdownNow());
        stage.show();
    }

    // -------------------------------------------------------------------------

    private void runSearch() {
        String nameTerm  = nameFilter.getText().trim().toLowerCase(Locale.ROOT);
        String emailTerm = emailFilter.getText().trim().toLowerCase(Locale.ROOT);

        searchButton.setDisable(true);
        statusLabel.setText("Searching…");
        userTable.setItems(FXCollections.emptyObservableList());
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        pageLabel.setText("Page — / —");

        // Simulate 1-second DB query on a background thread
        scheduler.schedule(() -> {
            List<String[]> results = query(nameTerm, emailTerm);
            Platform.runLater(() -> {
                currentResults = results;
                currentPage = 0;
                showPage();
                searchButton.setDisable(false);
                int total = results.size();
                statusLabel.setText(total == 0
                        ? "No users found."
                        : "Found " + total + " user" + (total == 1 ? "" : "s") + ".");
            });
        }, 1, TimeUnit.SECONDS);
    }

    private List<String[]> query(String nameTerm, String emailTerm) {
        List<String[]> out = new ArrayList<>();
        for (String[] row : ALL_USERS) {
            boolean nameOk  = nameTerm.isEmpty()  || row[1].toLowerCase(Locale.ROOT).contains(nameTerm);
            boolean emailOk = emailTerm.isEmpty() || row[2].toLowerCase(Locale.ROOT).contains(emailTerm);
            if (nameOk && emailOk) out.add(row);
        }
        return out;
    }

    private void showPage() {
        int total = currentResults.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        ObservableList<String[]> page =
                FXCollections.observableArrayList(currentResults.subList(from, to));
        userTable.setItems(page);

        pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
