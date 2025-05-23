package com.marcos.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class OptionsMenu {
    private final Stage stage;
    private final int windowsWidth;
    private final int windowsHeight;
    private final Menu menu;

    public OptionsMenu(Stage stage, int windowsWidth, int windowsHeight, Menu menu) {
        this.stage = stage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
        this.menu = menu;
    }

    public Scene createScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-image: url('/assets/board/menu.png'); -fx-background-size: cover;");

        Text title = new Text("Options");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        // 3D Mode Checkbox
        CheckBox enable3DMode = new CheckBox("Enable 3D Mode");
        enable3DMode.setStyle(createCheckboxStyle());
        enable3DMode.setSelected(GameOptions.getInstance().is3DModeEnabled());
        enable3DMode.setOnAction(e -> menu.set3DMode(enable3DMode.isSelected()));

        // Mute Checkbox
        CheckBox muteAudio = new CheckBox("Mute Audio");
        muteAudio.setStyle(createCheckboxStyle());
        muteAudio.setOnAction(e -> Audio.getInstance(null).toggleMute());

        // Volume Slider
        Text volumeLabel = new Text("Volume");
        volumeLabel.setStyle("-fx-fill: white; -fx-font-family: 'Verdana'; -fx-font-size: 22px; -fx-font-weight: bold;");

        Slider volumeSlider = new Slider(0, 1, 1);
        volumeSlider.setStyle(createSliderStyle());
        volumeSlider.setPrefWidth(200);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            Audio.getInstance(null).setVolume(newVal.floatValue())
        );

        HBox volumeControl = new HBox(20);
        volumeControl.setAlignment(Pos.CENTER);
        volumeControl.getChildren().addAll(volumeLabel, volumeSlider);
        volumeControl.setStyle("-fx-background-color: transparent;");

        StackPane testEnvironmentButton = createButton("3D Test Environment", Color.PURPLE, Color.DARKVIOLET);
        testEnvironmentButton.setOnMouseClicked(e -> {
            Test3D test3D = new Test3D(windowsWidth, windowsHeight);
            test3D.initialize();
            stage.hide();
            test3D.createGameScene(windowsWidth, windowsHeight, false);
        });

        StackPane backButton = menu.createButton("Back", Color.GRAY, Color.DARKGRAY);
        backButton.setOnMouseClicked(e -> menu.showMenu());

        VBox optionsBox = new VBox(20);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.getChildren().addAll(
            enable3DMode,
            muteAudio,
            volumeControl,
            testEnvironmentButton,
            backButton
        );

        AnchorPane.setTopAnchor(title, 50.0);
        AnchorPane.setLeftAnchor(title, 50.0);
        AnchorPane.setTopAnchor(optionsBox, 150.0);
        AnchorPane.setLeftAnchor(optionsBox, 50.0);
        AnchorPane.setRightAnchor(optionsBox, 50.0);

        root.getChildren().addAll(title, optionsBox);

        return new Scene(root, windowsWidth, windowsHeight);
    }

    private String createCheckboxStyle() {
        return "-fx-text-fill: black; " +
               "-fx-font-family: 'Verdana'; " +
               "-fx-font-size: 22px; " +
               "-fx-font-weight: bold; " +
               "-fx-background-color: rgba(255, 255, 255, 0.8); " +
               "-fx-padding: 5 10 5 10; " +
               "-fx-background-radius: 5;";
    }

    private String createSliderStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-control-inner-background: white;" +
               "-fx-track-background: transparent;" +
               "-fx-track-height: 8px;" +
               "-fx-thumb-height: 24px;" +
               "-fx-thumb-width: 24px;";
    }

    private StackPane createButton(String text, Color defaultColor, Color hoverColor) {
        Rectangle rectangle = new Rectangle(300, 80);
        rectangle.setFill(defaultColor);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);

        Text buttonText = new Text(text);
        buttonText.setFill(Color.WHITE);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        StackPane button = new StackPane(rectangle, buttonText);
        button.setOnMouseEntered(e -> rectangle.setFill(hoverColor));
        button.setOnMouseExited(e -> rectangle.setFill(defaultColor));

        return button;
    }
}