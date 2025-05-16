package com.marcos.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
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

        CheckBox enable3DMode = new CheckBox("Enable 3D Mode");
        enable3DMode.setStyle("-fx-text-fill: black; " +
                            "-fx-font-family: 'Verdana'; " +
                            "-fx-font-size: 22px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: rgba(255, 255, 255, 0.8); " +
                            "-fx-padding: 5 10 5 10; " +
                            "-fx-background-radius: 5;");
        enable3DMode.setSelected(GameOptions.getInstance().is3DModeEnabled());
        enable3DMode.setOnAction(e -> menu.set3DMode(enable3DMode.isSelected()));

        StackPane testEnvironmentButton = createButton("3D Test Environment", Color.PURPLE, Color.DARKVIOLET);
        testEnvironmentButton.setOnMouseClicked(e -> {
            Test3D test3D = new Test3D(windowsWidth, windowsHeight);
            test3D.initialize();
            stage.hide(); // Hide the current window
            test3D.createGameScene(windowsWidth, windowsHeight, false);
        });

        StackPane backButton = menu.createButton("Back", Color.GRAY, Color.DARKGRAY);
        backButton.setOnMouseClicked(e -> menu.showMenu());

        VBox optionsBox = new VBox(20);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.getChildren().addAll(enable3DMode, testEnvironmentButton, backButton);

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