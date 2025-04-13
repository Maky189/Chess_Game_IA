package com.marcos.chess;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
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

    public Menu(Stage primaryStage, int windowsWidth, int windowsHeight) {
        this.primaryStage = primaryStage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public void showMenu() {
        primaryStage.setTitle("Chess Game Menu");

        StackPane startGameButton = createButton("Start Game", Color.BLUE, Color.DARKBLUE);
        startGameButton.setOnMouseClicked(e -> startGame(false));
        
        StackPane multiplayerGameButton = createButton("Multiplayer Game", Color.GREEN, Color.DARKGREEN);
        multiplayerGameButton.setOnMouseClicked(e -> startGame(true));
       
        VBox layout = new VBox(20);
        layout.getChildren().addAll(startGameButton, multiplayerGameButton);
        layout.setStyle("-fx-alignment: center; -fx-background-image: url('/assets/board/cover.png'); -fx-background-size: cover;");

        Scene scene = new Scene(layout, windowsWidth, windowsHeight);

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

    private void startGame(boolean isMultiplayer) {
        int squareSize = 80;
        int size = 8;

        Game game = new Game(size);
        Board board = new Board(squareSize, size, windowsWidth, windowsHeight);
        
        Canvas canvas = new Canvas(windowsWidth, windowsHeight);
        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());
        
        DragHandler handler = new DragHandler(board, game, canvas, isMultiplayer);
        canvas.setOnMousePressed(handler::MousePressed);
        canvas.setOnMouseReleased(handler::MouseReleased);
        canvas.setOnMouseDragged(handler::MouseDragged);
        
        StackPane gameLayout = new StackPane(canvas);
        Scene gameScene = new Scene(gameLayout, windowsWidth, windowsHeight);

        primaryStage.setScene(gameScene);
    }
}