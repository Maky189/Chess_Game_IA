package com.marcos.chess;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Board {

    private final int squareSize;
    private final int size;
    private final int windowsWidth;
    private final int windowsHeight;

    private final Image pawnWhite = new Image("assets/pieces/pawn_white.png");
    private final Image pawnBlack = new Image("assets/pieces/pawn_black.png");
    private final Image rookWhite = new Image("assets/pieces/rook_white.png");
    private final Image rookBlack = new Image("assets/pieces/rook_black.png");
    private final Image knightWhite = new Image("assets/pieces/knight_white.png");
    private final Image knightBlack = new Image("assets/pieces/knight_black.png");
    private final Image bishopWhite = new Image("assets/pieces/bishop_white.png");
    private final Image bishopBlack = new Image("assets/pieces/bishop_black.png");
    private final Image queenWhite = new Image("assets/pieces/queen_white.png");
    private final Image queenBlack = new Image("assets/pieces/queen_black.png");
    private final Image kingWhite = new Image("assets/pieces/king_white.png");
    private final Image kingBlack = new Image("assets/pieces/king_black.png");

    public Board(int squareSize, int size, int windowsWidth, int windowsHeight) {
        this.squareSize = squareSize;
        this.size = size;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public void drawBoard(GraphicsContext gc, int[][] board) {

        Color white = Color.WHITE;
        Color black = Color.BLACK;
        Image pawnWhite = new Image("assets/pieces/pawn_white.png");

        //Fill the background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, windowsWidth, windowsHeight);

        //Calculate the starting positions for the board
        double x = (windowsWidth - size * squareSize) / 2.0;
        double y = (windowsHeight - size * squareSize) / 2.0;

        //Render the chessboard
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {

                //Alter colors to make a grid
                boolean isWhite = (i + j) % 2 == 0;
                gc.setFill(isWhite ? white : black);

                //Calculate the position of a square
                double x1 = x + j * squareSize;
                double y1 = y + i * squareSize;

                //Draw square
                gc.fillRect(x1, y1, squareSize, squareSize);

                //Render it
                Image piece = getImage(board[i][j]);
                if(piece != null) {
                    gc.drawImage(piece, x1, y1, squareSize, squareSize);
                }
            }
        }
    }

    private Image getImage(int piece) {
        switch (piece) {
            //White pieces
            case 1: return pawnWhite;
            case 2: return rookWhite;
            case 3: return knightWhite;
            case 4: return bishopWhite;
            case 5: return queenWhite;
            case 6: return kingWhite;

            //Black Pieces
            case -1: return pawnBlack;
            case -2: return rookBlack;
            case -3: return knightBlack;
            case -4: return bishopBlack;
            case -5: return queenBlack;
            case -6: return kingBlack;

            default: return null;
        }
    }


}
