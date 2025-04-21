package com.marcos.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Menu {

    private final Stage primaryStage;
    private final int windowsWidth;
    private final int windowsHeight;
    private boolean is3DMode = false;
    private Renderer currentRenderer;

    public Menu(Stage primaryStage, int windowsWidth, int windowsHeight) {
        this.primaryStage = primaryStage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
        this.currentRenderer = new Renderer_2D(windowsWidth, windowsHeight);
    }

    public void showMenu() {
        primaryStage.setTitle("Chess Game Menu");

        StackPane startGameButton = createButton("Start Game", Color.BLUE, Color.DARKBLUE);
        startGameButton.setOnMouseClicked(e -> startGame(false));
        
        StackPane multiplayerGameButton = createButton("Multiplayer Game", Color.GREEN, Color.DARKGREEN);
        multiplayerGameButton.setOnMouseClicked(e -> startGame(true));

        StackPane Button_3D = createButton("3D", Color.GRAY, Color.GREEN, 100, 50);
        Button_3D.setOnMouseClicked(e -> {
            try {
                is3DMode = !is3DMode;
                if(is3DMode) {
                    currentRenderer.cleanup();
                    currentRenderer = new Render_3D(windowsWidth, windowsHeight);
                }
                else {
                    currentRenderer.cleanup();
                    currentRenderer = new Renderer_2D(windowsWidth, windowsHeight);
                }
            }
            catch (Exception ex) {
                currentRenderer = new Renderer_2D(windowsWidth, windowsHeight);
            }
        });
        
        VBox mainButtons = new VBox(20);
        mainButtons.getChildren().addAll(startGameButton, multiplayerGameButton);
        mainButtons.setAlignment(Pos.CENTER);

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(mainButtons, Button_3D);
        

        AnchorPane.setTopAnchor(mainButtons, (windowsHeight - mainButtons.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(mainButtons, (double) (windowsWidth - 300) / 2); // 300 is the width of main buttons
        

        AnchorPane.setTopAnchor(Button_3D, 20.0);
        AnchorPane.setLeftAnchor(Button_3D, 20.0);
        
        root.setStyle("-fx-background-image: url('/assets/board/cover.png'); -fx-background-size: cover;");

        Scene scene = new Scene(root, windowsWidth, windowsHeight);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private StackPane createButton(String text, Color defaultColor, Color hoverColor) {

        Rectangle rectangle = new Rectangle(300, 100); 
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


    private StackPane createButton(String text, Color defaultColor, Color hoverColor, double width, double height) {
        Rectangle rectangle = new Rectangle(width, height); 
        rectangle.setFill(defaultColor);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);
        
        Text buttonText = new Text(text);
        buttonText.setFill(Color.WHITE);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
       
        StackPane button = new StackPane(rectangle, buttonText);
        button.setOnMouseEntered(e -> rectangle.setFill(hoverColor)); 
        button.setOnMouseExited(e -> rectangle.setFill(defaultColor)); 

        return button;
    }



    private void startGame(boolean isMultiplayer) {
        Game game = new Game(8);
        currentRenderer.initialize();
        Scene gameScene = currentRenderer.createGameScene(game, windowsWidth, windowsHeight, isMultiplayer);
        primaryStage.setScene(gameScene);
    }
}