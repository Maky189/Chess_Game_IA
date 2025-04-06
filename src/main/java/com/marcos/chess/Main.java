package com.marcos.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;


public class Main extends Application {

    private final int SQUARE_SIZE = 80;
    private final int SIZE = 8;
    private final int PIXEL_SIZE = SQUARE_SIZE * SIZE;
    private final int WINDOWS_WIDTH = 800;
    private final int WINDOWS_HEIGHT = 800;

    public static void main(String[] args) {

        launch();
    }

    @Override
    public void start(Stage stage) {

        //Create array of the board
        int[][] board = new int[SIZE][SIZE];

        //Populate each tile of the board with a pawn; 1 for pawn
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = 1;
            }
        }

        //Create canvas with the windows size
        Canvas canvas = new Canvas(WINDOWS_WIDTH, WINDOWS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //Fill the black borders
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WINDOWS_WIDTH, WINDOWS_HEIGHT);

        //Calculate where to start drawing the board
        double startX = (WINDOWS_WIDTH - PIXEL_SIZE) / 2.0;
        double startY = (WINDOWS_HEIGHT - PIXEL_SIZE) / 2.0;

        //Draw the board with the pawns
        drawBoard(gc, board, startX, startY);

        //Add the canvas
        StackPane container = new StackPane(canvas);
        container.setAlignment(Pos.CENTER);

        Scene scene = new Scene(container, WINDOWS_WIDTH, WINDOWS_HEIGHT, Color.BLACK);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.show();
    }

    private void drawBoard(GraphicsContext gc, int[][] board, double startX, double startY) {

        //Definitions of each square of the board
        Color whiteColor = Color.WHITE;
        Color blackColor = Color.BLACK;

        //Load the pawn image
        Image pawnWhite = new Image("assets/pieces/pawn_white.png");

        //render each square
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {

                //Alternate between color for each square
                boolean isWhite = (i + j) % 2 == 0;
                gc.setFill(isWhite ? whiteColor : blackColor);

                //Calculate the top-left corner of the actual square
                double x = startX + j * SQUARE_SIZE;
                double y = startY + i * SQUARE_SIZE;

                //Draw the square with the color
                gc.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

                //Check if contains a pawn
                if (board[i][j] == 1) {
                    gc.drawImage(pawnWhite, x, y, SQUARE_SIZE, SQUARE_SIZE);
                }

            }
        }

    }
}
