package com.marcos.chess;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.util.List;

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

    public void drawBoardWithHighlights(GraphicsContext gc, int[][] board, List<int[]> highlights) {
        Color white = Color.WHITE;
        Color black = Color.BLACK;
        Color highlightColor = Color.BLUE;
        Color captureHighlightColor = Color.LIGHTCORAL; // Light red

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, windowsWidth, windowsHeight);

        double x = (windowsWidth - size * squareSize) / 2.0;
        double y = (windowsHeight - size * squareSize) / 2.0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {

                boolean isWhite = (i + j) % 2 == 0;
                gc.setFill(isWhite ? white : black);

                double x1 = x + j * squareSize;
                double y1 = y + i * squareSize;

                gc.fillRect(x1, y1, squareSize, squareSize);

                if (highlights != null && isHighlighted(highlights, i, j)) {
                    if (board[i][j] != 0) {
                        // Enemy piece
                        gc.setFill(captureHighlightColor);
                        gc.fillRect(x1, y1, squareSize, squareSize);
                    } else {
                        // Normal highlight
                        gc.setFill(highlightColor);
                        double centerX = x1 + squareSize / 2.0;
                        double centerY = y1 + squareSize / 2.0;
                        double radius = squareSize * 0.2;

                        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                    }
                }

                Image piece = getImage(board[i][j]);
                if (piece != null) {
                    gc.drawImage(piece, x1, y1, squareSize, squareSize);
                }
            }
        }
    }

    private boolean isHighlighted(List<int[]> highlights, int i, int j) {
        for (int[] highlight : highlights) {
            if (highlight[0] == i && highlight[1] == j) {
                return true;
            }
        }

        return false;
    }

    private Image getImage(int piece) {
        return switch (piece) {
            //White pieces
            case 1 -> pawnWhite;
            case 2 -> rookWhite;
            case 3 -> knightWhite;
            case 4 -> bishopWhite;
            case 5 -> queenWhite;
            case 6 -> kingWhite;

            //Black Pieces
            case -1 -> pawnBlack;
            case -2 -> rookBlack;
            case -3 -> knightBlack;
            case -4 -> bishopBlack;
            case -5 -> queenBlack;
            case -6 -> kingBlack;
            default -> null;
        };
    }

    public int getSquareSize() {
        return this.squareSize;
    }

    public int getWindowsWidth() {
        return this.windowsWidth;
    }

    public int getWindowsHeight() {
        return this.windowsHeight;
    }

    public int getSize() {
        return this.size;
    }

    public Image obtainImage(int piece) {
        return getImage(piece);
    }

}
