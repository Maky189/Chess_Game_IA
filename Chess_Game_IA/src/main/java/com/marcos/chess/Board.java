package com.marcos.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Menu extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Button startGameButton = new Button("Start Game");
        Button multiplayerGameButton = new Button("Multiplayer Game");

        startGameButton.setOnAction(e -> {
            // Logic to start the game goes here
            Game game = new Game(8);
            Board board = new Board(80, 8, 800, 800);
            // Initialize the game window with the board
            // This part would typically involve transitioning to the game scene
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(startGameButton, multiplayerGameButton);
        
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setTitle("Chess Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}