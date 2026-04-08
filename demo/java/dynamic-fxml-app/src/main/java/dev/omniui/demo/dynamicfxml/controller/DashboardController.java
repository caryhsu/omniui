package dev.omniui.demo.dynamicfxml.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.util.Random;

public class DashboardController {

    @FXML private Label usersCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private ProgressBar cpuBar;
    @FXML private ProgressBar memBar;
    @FXML private Button refreshBtn;
    @FXML private PieChart pieChart;

    private final Random rng = new Random();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    private void refresh() {
        int total  = 80 + rng.nextInt(40);
        int active = 20 + rng.nextInt(total / 2);
        usersCountLabel.setText(String.valueOf(total));
        activeCountLabel.setText(String.valueOf(active));

        double cpu = 0.1 + rng.nextDouble() * 0.8;
        double mem = 0.2 + rng.nextDouble() * 0.6;
        cpuBar.setProgress(cpu);
        memBar.setProgress(mem);

        pieChart.getData().clear();
        pieChart.getData().addAll(
                new PieChart.Data("Active",   active),
                new PieChart.Data("Inactive", total - active)
        );
        pieChart.setTitle("User Status");
    }
}
