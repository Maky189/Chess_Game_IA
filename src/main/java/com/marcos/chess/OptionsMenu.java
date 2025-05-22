package com.marcos.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
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

        // 3D Mode checkbox
        CheckBox enable3DMode = new CheckBox("Enable 3D Mode");
        enable3DMode.setStyle("-fx-text-fill: white; " +
                            "-fx-font-family: 'Verdana'; " +
                            "-fx-font-size: 22px; " +
                            "-fx-font-weight: bold;");
        enable3DMode.setSelected(GameOptions.getInstance().is3DModeEnabled());
        
        // Color selection toggle
        HBox colorSelection = new HBox(20);
        colorSelection.setAlignment(Pos.CENTER);

        Text colorLabel = new Text("Play as:");
        colorLabel.setFill(Color.WHITE);
        colorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        ColorToggleSwitch colorToggle = new ColorToggleSwitch(GameOptions.getInstance().isPlayingAsWhite());
                
        Text whiteText = new Text("White");
        Text blackText = new Text("Black");
        whiteText.setFill(Color.WHITE);
        blackText.setFill(Color.WHITE);
        whiteText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        blackText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Make the texts clickable as well
        whiteText.setOnMouseClicked(e -> {
            colorToggle.setWhite(true);
            GameOptions.getInstance().setPlayAsWhite(true);
        });

        blackText.setOnMouseClicked(e -> {
            colorToggle.setWhite(false);
            GameOptions.getInstance().setPlayAsWhite(false);
        });

        // Add a mouse hover effect
        whiteText.setOnMouseEntered(e -> whiteText.setFill(Color.LIGHTGRAY));
        whiteText.setOnMouseExited(e -> whiteText.setFill(Color.WHITE));
        blackText.setOnMouseEntered(e -> blackText.setFill(Color.LIGHTGRAY));
        blackText.setOnMouseExited(e -> blackText.setFill(Color.WHITE));

        colorSelection.getChildren().addAll(colorLabel, whiteText, colorToggle, blackText);

        // Add listeners
        enable3DMode.setOnAction(e -> menu.set3DMode(enable3DMode.isSelected()));
        colorToggle.setOnMouseClicked(e -> {
            colorToggle.toggle();
            GameOptions.getInstance().setPlayAsWhite(colorToggle.isWhite());
        });

        // Test Environment button
        StackPane testEnvironmentButton = createButton("3D Test Environment", Color.PURPLE, Color.DARKVIOLET);
        testEnvironmentButton.setOnMouseClicked(e -> {
            Test3D test3D = new Test3D(windowsWidth, windowsHeight);
            test3D.initialize();
            stage.hide(); // Hide the current window
            test3D.createGameScene(windowsWidth, windowsHeight, false);
        });

        StackPane backButton = menu.createButton("Back", Color.GRAY, Color.DARKGRAY);
        backButton.setOnMouseClicked(e -> menu.showMenu());

        VBox optionsBox = new VBox(30);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.getChildren().addAll(enable3DMode, colorSelection, testEnvironmentButton, backButton);

        AnchorPane.setTopAnchor(title, 50.0);
        AnchorPane.setLeftAnchor(title, 50.0);
        AnchorPane.setTopAnchor(optionsBox, 150.0);
        AnchorPane.setLeftAnchor(optionsBox, 50.0);
        AnchorPane.setRightAnchor(optionsBox, 50.0);

        root.getChildren().addAll(title, optionsBox);

        return new Scene(root, windowsWidth, windowsHeight);
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