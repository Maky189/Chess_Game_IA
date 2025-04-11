package com.marcos.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
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
            // Logic to start the game
            Game game = new Game(8); // Initialize a new game
            Board board = new Board(80, 8, 800, 800); // Create a new board
            // You can add code here to transition to the game scene
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(startGameButton, multiplayerGameButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setTitle("Chess Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}