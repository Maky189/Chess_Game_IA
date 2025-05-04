package com.marcos.chess;

import javafx.geometry.Insets;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import jme3tools.savegame.SaveGame;
import javafx.application.Platform;

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

        StackPane startGameButton = createButton("SinglePlayer", Color.BLUE, Color.DARKBLUE);
        startGameButton.setOnMouseClicked(e -> startGame(false));
        
        StackPane multiplayerGameButton = createButton("Multiplayer", Color.GREEN, Color.DARKGREEN);
        multiplayerGameButton.setOnMouseClicked(e -> startGame(true));

        StackPane optionsButton = createButton("Options", Color.ORANGE, Color.DARKORANGE);
        optionsButton.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Options");
            alert.setHeaderText(null);
            alert.setContentText("Options are not available yet.");
            alert.showAndWait();
        });

        StackPane quitButton = createButton("Quit", Color.DARKRED, Color.DARKRED, 100, 50);
        quitButton.setOnMouseClicked(e -> {
            Platform.exit();
            System.exit(0);
        });
        
        VBox mainButtons = new VBox(20);
        mainButtons.getChildren().addAll(startGameButton, multiplayerGameButton, optionsButton);
        mainButtons.setAlignment(Pos.CENTER);

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(mainButtons, quitButton);
        

        AnchorPane.setTopAnchor(mainButtons, (windowsHeight - mainButtons.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(mainButtons, (double) (windowsWidth - 300) / 2);
        

        AnchorPane.setTopAnchor(quitButton, 20.0);
        AnchorPane.setLeftAnchor(quitButton, 20.0);
        
        root.setStyle("-fx-background-image: url('/assets/board/cover.png'); -fx-background-size: cover;");

        Scene scene = new Scene(root, windowsWidth, windowsHeight);

        
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
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
        if (!isMultiplayer) {
            SaveGameMenu saveGameMenu = new SaveGameMenu(primaryStage, windowsWidth, windowsHeight);
            Scene saveGameScene = saveGameMenu.createScene();
            primaryStage.setScene(saveGameScene);
        }
        else {
            GameFactory.resetGameInstance(8);
            Game game = GameFactory.getGameInstance(8);
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