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
        primaryStage.setTitle("Chess Game Menu");

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(e -> startGame());

        Button multiplayerGameButton = new Button("Multiplayer Game");
        // Action for multiplayerGameButton can be implemented later

        VBox layout = new VBox(10);
        layout.getChildren().addAll(startGameButton, multiplayerGameButton);
        Scene scene = new Scene(layout, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame() {
        // Logic to start the game goes here
        // For now, we can just print to the console
        System.out.println("Starting a single player game...");
        // You can initialize the game here and switch to the game scene
    }
}