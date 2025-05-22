package com.marcos.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;

public class Menu {

    private final Stage primaryStage;
    private final int windowsWidth;
    private final int windowsHeight;
    public static boolean is3DMode = false;
    private Renderer currentRenderer;
    private GameOptions gameOptions = GameOptions.getInstance();

    public Menu(Stage primaryStage, int windowsWidth, int windowsHeight) {
        this.primaryStage = primaryStage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
        this.is3DMode = gameOptions.is3DModeEnabled();
        this.currentRenderer = is3DMode ? new Render_3D(windowsWidth, windowsHeight) : new Renderer_2D(windowsWidth, windowsHeight);
    }

    public void showMenu() {
        primaryStage.setTitle("Chess Game Menu");

        StackPane startGameButton = createButton("SinglePlayer", Color.GRAY, Color.DARKGRAY);
        startGameButton.setOnMouseClicked(e -> startGame(false));
        
        StackPane multiplayerGameButton = createButton("Multiplayer", Color.GRAY, Color.DARKGRAY);
        multiplayerGameButton.setOnMouseClicked(e -> {
            MenuMultiplayer multiplayerMenu = new MenuMultiplayer(primaryStage, windowsWidth, windowsHeight);
            Scene multiplayerScene = multiplayerMenu.createScene();
            primaryStage.setScene(multiplayerScene);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);
        });

        StackPane optionsButton = createButton("Options", Color.GRAY, Color.DARKGREY);
        optionsButton.setOnMouseClicked(e -> {
            OptionsMenu optionsMenu = new OptionsMenu(primaryStage, windowsWidth, windowsHeight, this);
            Scene optionsScene = optionsMenu.createScene();
            primaryStage.setScene(optionsScene);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);
        });

        StackPane quitButton = createButton("Quit", Color.GRAY, Color.DARKGRAY.grayscale(), 100, 50);
        quitButton.setOnMouseClicked(e -> {
            Platform.exit();
        });
        
        VBox mainButtons = new VBox(20);
        mainButtons.getChildren().addAll(startGameButton, multiplayerGameButton, optionsButton);
        mainButtons.setAlignment(Pos.TOP_CENTER);

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(mainButtons, quitButton);
        

        AnchorPane.setTopAnchor(mainButtons, (windowsHeight - mainButtons.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(mainButtons, (double) (windowsWidth - 300) / 2);
        

        AnchorPane.setTopAnchor(quitButton, 20.0);
        AnchorPane.setLeftAnchor(quitButton, 20.0);
        
        root.setStyle("-fx-background-image: url('/assets/board/menu.png'); -fx-background-size: cover;");

        Scene scene = new Scene(root, windowsWidth, windowsHeight);

        
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public StackPane createButton(String text, Color defaultColor, Color hoverColor) {

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

    public static boolean is3DModeEnabled() {
        return GameOptions.getInstance().is3DModeEnabled();
    }

    public void set3DMode(boolean enabled) {
        this.is3DMode = enabled;
        gameOptions.set3DMode(enabled);
        currentRenderer = enabled ? new Render_3D(windowsWidth, windowsHeight) : new Renderer_2D(windowsWidth, windowsHeight);
    }

    private void startGame(boolean isMultiplayer) {
        if (!isMultiplayer) {
            SaveGameMenu saveGameMenu = new SaveGameMenu(primaryStage, windowsWidth, windowsHeight);
            Scene saveGameScene = saveGameMenu.createScene();
            primaryStage.setScene(saveGameScene);
        } else {
            MainGame.resetGameInstance(8);
            Game game = MainGame.getGameInstance(8);
            currentRenderer.initialize();

            if (is3DMode) {
                primaryStage.hide();
                currentRenderer.createGameScene(windowsWidth, windowsHeight, true);
            } else {
                Scene scene = currentRenderer.createGameScene(windowsWidth, windowsHeight, true);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setScene(scene);
                primaryStage.setMaximized(true);
                primaryStage.setFullScreen(true);
            }
        }
    }
}