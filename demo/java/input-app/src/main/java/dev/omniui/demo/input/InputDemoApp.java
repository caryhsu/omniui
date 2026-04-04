package dev.omniui.demo.input;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class InputDemoApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // ---- DatePicker demo ----------------------------------------------
        DatePicker demoPicker = new DatePicker();
        demoPicker.setId("demoPicker");
        demoPicker.setPromptText("Pick a date");

        Label datePickerStatus = new Label("Picked date: none");
        datePickerStatus.setId("datePickerStatus");
        demoPicker.valueProperty().addListener((obs, oldVal, newVal) ->
            datePickerStatus.setText("Picked date: " + (newVal == null ? "none" : newVal.toString()))
        );

        VBox datePickerSection = section("DatePicker Demo", "datePickerSection",
            demoPicker, datePickerStatus);

        // ---- RadioButton / ToggleButton demo --------------------------------
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton radio1 = new RadioButton("Option A");
        radio1.setId("radioA");
        radio1.setToggleGroup(radioGroup);
        radio1.setSelected(true);
        RadioButton radio2 = new RadioButton("Option B");
        radio2.setId("radioB");
        radio2.setToggleGroup(radioGroup);
        ToggleButton toggleBtn = new ToggleButton("Toggle Me");
        toggleBtn.setId("toggleButton");
        Label radioStatus = new Label("Radio: A selected");
        radioStatus.setId("radioStatus");
        radioGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == radio1) radioStatus.setText("Radio: A selected");
            else if (n == radio2) radioStatus.setText("Radio: B selected");
        });
        toggleBtn.selectedProperty().addListener((obs, o, n) ->
            radioStatus.setText(radioStatus.getText() + " | Toggle: " + (n ? "on" : "off")));

        VBox radioToggleSection = section("RadioButton / ToggleButton Demo", "radioToggleSection",
            radio1, radio2, toggleBtn, radioStatus);

        // ---- Slider / Spinner demo ------------------------------------------
        Slider demoSlider = new Slider(0, 100, 25);
        demoSlider.setId("demoSlider");
        demoSlider.setShowTickLabels(true);
        demoSlider.setShowTickMarks(true);
        Label sliderValue = new Label("Slider: 25.0");
        sliderValue.setId("sliderValue");
        demoSlider.valueProperty().addListener((obs, o, n) ->
            sliderValue.setText(String.format("Slider: %.1f", n.doubleValue())));

        Spinner<Integer> demoSpinner = new Spinner<>(0, 100, 10, 1);
        demoSpinner.setId("demoSpinner");
        demoSpinner.setEditable(true);
        Label spinnerValue = new Label("Spinner: 10");
        spinnerValue.setId("spinnerValue");
        demoSpinner.valueProperty().addListener((obs, o, n) ->
            spinnerValue.setText("Spinner: " + n));

        VBox sliderSpinnerSection = section("Slider / Spinner Demo", "sliderSpinnerSection",
            demoSlider, sliderValue, demoSpinner, spinnerValue);

        // ---- TextArea demo --------------------------------------------------
        TextArea demoTextArea = new TextArea("Hello\nWorld");
        demoTextArea.setId("demoTextArea");
        demoTextArea.setPrefRowCount(4);
        VBox textAreaSection = section("TextArea Demo", "textAreaSection", demoTextArea);

        // ---- PasswordField demo ---------------------------------------------
        PasswordField demoPasswordField = new PasswordField();
        demoPasswordField.setId("demoPasswordField");
        demoPasswordField.setPromptText("Enter password");
        VBox passwordFieldSection = section("PasswordField Demo", "passwordFieldSection", demoPasswordField);

        // ---- Hyperlink demo -------------------------------------------------
        Hyperlink demoHyperlink = new Hyperlink("Click me");
        demoHyperlink.setId("demoHyperlink");
        VBox hyperlinkSection = section("Hyperlink Demo", "hyperlinkSection", demoHyperlink);

        // ---- CheckBox demo --------------------------------------------------
        CheckBox checkA = new CheckBox("Option A");
        checkA.setId("checkA");
        checkA.setSelected(true);
        CheckBox checkB = new CheckBox("Option B");
        checkB.setId("checkB");
        CheckBox checkC = new CheckBox("Option C");
        checkC.setId("checkC");
        VBox checkBoxSection = section("CheckBox Demo", "checkBoxSection", checkA, checkB, checkC);

        // ---- ChoiceBox demo -------------------------------------------------
        ChoiceBox<String> demoChoiceBox = new ChoiceBox<>();
        demoChoiceBox.setId("demoChoiceBox");
        demoChoiceBox.getItems().addAll("Apple", "Banana", "Cherry");
        demoChoiceBox.setValue("Apple");
        VBox choiceBoxSection = section("ChoiceBox Demo", "choiceBoxSection", demoChoiceBox);

        // ---- ColorPicker demo ---------------------------------------------
        ColorPicker demoPicker2 = new ColorPicker(Color.web("#1e90ff"));
        demoPicker2.setId("demoPicker2");

        Label colorResult = new Label("Selected: #1e90ff");
        colorResult.setId("colorResult");
        demoPicker2.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String hex = String.format("#%02x%02x%02x",
                    (int)(newVal.getRed() * 255),
                    (int)(newVal.getGreen() * 255),
                    (int)(newVal.getBlue() * 255));
                colorResult.setText("Selected: " + hex);
            }
        });

        javafx.scene.control.Button resetColorButton = new javafx.scene.control.Button("Reset Color");
        resetColorButton.setId("resetColorButton");
        resetColorButton.setOnAction(e -> demoPicker2.setValue(Color.web("#1e90ff")));

        VBox colorPickerSection = section("ColorPicker Demo", "colorPickerSection",
            demoPicker2, colorResult, resetColorButton);

        // ---- CSS Style Demo ------------------------------------------------
        Label cssTarget = new Label("Styled Label");
        cssTarget.setId("cssTarget");
        cssTarget.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        cssTarget.getStyleClass().add("error-label");
        Label cssSectionStatus = new Label("CSS demo target above");
        cssSectionStatus.setId("cssSectionStatus");
        VBox cssSection = section("CSS Style Demo", "cssSection", cssTarget, cssSectionStatus);

        VBox root = new VBox(
            18,
            datePickerSection,
            radioToggleSection,
            sliderSpinnerSection,
            textAreaSection,
            passwordFieldSection,
            hyperlinkSection,
            checkBoxSection,
            choiceBoxSection,
            colorPickerSection,
            cssSection
        );
        root.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setId("demoScrollPane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(scrollPane, 720, 760);
        primaryStage.setTitle("OmniUI Input Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox section(String title, String id, javafx.scene.Node... children) {
        Label heading = new Label(title);
        heading.setId(id + "Title");
        VBox box = new VBox(10, heading);
        box.setId(id);
        box.getChildren().addAll(children);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
