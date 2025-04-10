package com.marcos.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private final int SQUARE_SIZE = 80;
    private final int SIZE = 8;
    private final int WINDOWS_WIDTH = 800;
    private final int WINDOWS_HEIGHT = 800;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {


        Game game = new Game(SIZE);


        Canvas canvas = new Canvas(WINDOWS_WIDTH, WINDOWS_HEIGHT);
        StackPane container = new StackPane(canvas);
        container.setAlignment(Pos.CENTER);

        Scene scene = new Scene(container, WINDOWS_WIDTH, WINDOWS_HEIGHT, Color.BLACK);

        //Create a board
        Board board = new Board(SQUARE_SIZE, SIZE, WINDOWS_WIDTH, WINDOWS_HEIGHT);

        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());

        //Handle game mouse events
        DragHandler handler = new DragHandler(board, game, canvas);
        canvas.setOnMousePressed(handler::MousePressed);
        canvas.setOnMouseReleased(handler::MouseReleased);
        canvas.setOnMouseDragged(handler::MouseDragged);


        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.show();

    }
}
