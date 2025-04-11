package com.marcos.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Menu extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button startGameButton = new Button("Start Game");
        Button multiplayerGameButton = new Button("Multiplayer Game");

        startGameButton.setOnAction(e -> startGame());
        
        VBox layout = new VBox(20);
        layout.getChildren().addAll(startGameButton, multiplayerGameButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 300, Color.BLACK);
        primaryStage.setTitle("Chess Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame() {
        // Logic to start the game goes here
        // For now, we can just print to the console
        System.out.println("Starting a new game...");
        // You can initialize the game and switch to the game scene here
    }

    public static void main(String[] args) {
        launch(args);
    }
}