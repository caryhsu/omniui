package dev.omniui.demo.progress;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProgressDemoApp extends Application {

    private static final int STEPS      = 100;
    private static final double STEP_MS = 50.0;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage stage) {
        Button runBtn = new Button("Run Job");
        runBtn.setId("runBtn");
        runBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 24;");

        Button resetBtn = new Button("Reset");
        resetBtn.setId("resetBtn");
        resetBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 18;");

        Button jobDetailBtn = new Button("Job Detail");
        jobDetailBtn.setId("jobDetailBtn");
        jobDetailBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 18;");
        jobDetailBtn.setDisable(true);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setId("progressBar");
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(22);

        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setId("progressIndicator");
        indicator.setPrefSize(80, 80);

        Label checkLabel = new Label("\u2713");
        checkLabel.setId("doneIndicator");
        checkLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");
        checkLabel.setVisible(false);

        StackPane doneCircle = new StackPane(checkLabel);
        doneCircle.setId("doneCircle");
        doneCircle.setPrefSize(80, 80);
        doneCircle.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 40;");
        doneCircle.setVisible(false);

        StackPane indicatorStack = new StackPane(indicator, doneCircle);
        indicatorStack.setPrefSize(80, 80);

        Label statusLabel = new Label("idle");
        statusLabel.setId("statusLabel");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label percentLabel = new Label("0 %");
        percentLabel.setId("percentLabel");
        percentLabel.setStyle("-fx-font-size: 13px;");

        TextArea jobLogArea = new TextArea();
        jobLogArea.setId("jobLogArea");
        jobLogArea.setEditable(false);
        jobLogArea.setPrefRowCount(6);
        jobLogArea.setPrefWidth(340);
        jobLogArea.setVisible(false);
        jobLogArea.setManaged(false);
        jobLogArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        List<String> logLines = new ArrayList<>();

        HBox btnRow = new HBox(12, runBtn, resetBtn, jobDetailBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, btnRow, progressBar, percentLabel, indicatorStack, statusLabel, jobLogArea);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));

        jobDetailBtn.setOnAction(e -> {
            jobLogArea.setText(String.join("\n", logLines));
            jobLogArea.setVisible(true);
            jobLogArea.setManaged(true);
        });

        resetBtn.setOnAction(e -> {
            progressBar.setProgress(0);
            indicator.setProgress(0);
            indicator.setVisible(true);
            doneCircle.setVisible(false);
            checkLabel.setVisible(false);
            percentLabel.setText("0 %");
            statusLabel.setText("idle");
            runBtn.setDisable(false);
            jobDetailBtn.setDisable(true);
            jobLogArea.setVisible(false);
            jobLogArea.setManaged(false);
            jobLogArea.clear();
            logLines.clear();
        });

        runBtn.setOnAction(e -> {
            progressBar.setProgress(0);
            indicator.setProgress(0);
            indicator.setVisible(true);
            doneCircle.setVisible(false);
            checkLabel.setVisible(false);
            percentLabel.setText("0 %");
            statusLabel.setText("running");
            runBtn.setDisable(true);
            jobDetailBtn.setDisable(true);
            jobLogArea.setVisible(false);
            jobLogArea.setManaged(false);
            jobLogArea.clear();
            logLines.clear();

            logLines.add("[" + LocalTime.now().format(TIME_FMT) + "] Job started");

            final int[] step = {0};
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(STEP_MS), ev -> {
                    step[0]++;
                    double progress = (double) step[0] / STEPS;
                    progressBar.setProgress(progress);
                    indicator.setProgress(progress);
                    percentLabel.setText(step[0] + " %");

                    if (step[0] == 25 || step[0] == 50 || step[0] == 75) {
                        logLines.add("[" + LocalTime.now().format(TIME_FMT) + "] "
                            + step[0] + "% complete -- processing batch " + (step[0] / 25));
                    }

                    if (step[0] >= STEPS) {
                        logLines.add("[" + LocalTime.now().format(TIME_FMT) + "] Job finished -- 100 items processed");
                        logLines.add("[" + LocalTime.now().format(TIME_FMT) + "] Status: SUCCESS");
                        indicator.setVisible(false);
                        doneCircle.setVisible(true);
                        checkLabel.setVisible(true);
                        statusLabel.setText("done");
                        runBtn.setDisable(false);
                        jobDetailBtn.setDisable(false);
                    }
                })
            );
            timeline.setCycleCount(STEPS);
            timeline.play();
        });

        Scene scene = new Scene(root, 420, 400);
        stage.setTitle("OmniUI Progress Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
