package com.marcos.chess;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.ArrayList;

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
        Color checkColor = Color.DARKRED;

        //background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, windowsWidth, windowsHeight);

        double x = (windowsWidth - size * squareSize) / 2.0;
        double y = (windowsHeight - size * squareSize) / 2.0;

        // Find the positions of the kings
        int[] whiteKingPos = findKingPosition(board, 6);
        int[] blackKingPos = findKingPosition(board, -6);
        boolean whiteKingInCheck = whiteKingPos != null && isCheck(board, whiteKingPos[0], whiteKingPos[1], -1);
        boolean blackKingInCheck = blackKingPos != null && isCheck(board, blackKingPos[0], blackKingPos[1], 1);

        // Render chessboard
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                // Alternate colors
                var isWhite = (i + j) % 2 == 0;
                gc.setFill(isWhite ? white : black);

                // Calculate position of square
                double x1 = x + j * squareSize;
                double y1 = y + i * squareSize;

                gc.fillRect(x1, y1, squareSize, squareSize);

                if ((whiteKingInCheck && whiteKingPos != null && i == whiteKingPos[0] && j == whiteKingPos[1]) ||
                    (blackKingInCheck && blackKingPos != null && i == blackKingPos[0] && j == blackKingPos[1])) {
                    gc.setFill(checkColor);
                    gc.fillRect(x1, y1, squareSize, squareSize);
                }

                // Render the piece
                Image piece = getImage(board[i][j]);
                if (piece != null) {
                    gc.drawImage(piece, x1, y1, squareSize, squareSize);
                }
            }
        }

        drawcoords(gc, x, y);
    }

    public void drawcoords(GraphicsContext gc, double x, double y) {
        // Add horizontal letters
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, squareSize * 0.3));
        for (int j = 0; j < size; j++) {
            String letter = String.valueOf((char) ('A' + j));
            double letterX = x + j * squareSize + squareSize / 2.0 - gc.getFont().getSize() / 2.0;

            // Add bellow board
            double letterYBottom = y + size * squareSize + squareSize * 0.4;
            gc.fillText(letter, letterX, letterYBottom);
        }

        // Add vertical numbers
        for (int i = 0; i < size; i++) {
            String number = String.valueOf(size - i);
            double numberX = x - squareSize * 0.4;
            double numberY = y + i * squareSize + squareSize / 2.0 + gc.getFont().getSize() / 2.0;
            gc.fillText(number, numberX, numberY);

        }
    }

    public void drawBoardWithHighlights(GraphicsContext gc, int[][] board, List<int[]> highlights) {
        Color white = Color.WHITE;
        Color black = Color.BLACK;
        Color highlightColor = Color.BLUE;
        Color captureHighlightColor = Color.LIGHTCORAL;
        Color checkColor = Color.DARKRED;

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, windowsWidth, windowsHeight);

        double x = (windowsWidth - size * squareSize) / 2.0;
        double y = (windowsHeight - size * squareSize) / 2.0;

        int[] whiteKingPos = findKingPosition(board, 6);
        int[] blackKingPos = findKingPosition(board, -6);

        boolean whiteKingInCheck = whiteKingPos != null && isCheck(board, whiteKingPos[0], whiteKingPos[1], -1);
        boolean blackKingInCheck = blackKingPos != null && isCheck(board, blackKingPos[0], blackKingPos[1], 1);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                boolean isWhite = (i + j) % 2 == 0;
                if (!isWhite) gc.setFill(black);
                else gc.setFill(white);

                double x1 = x + j * squareSize;
                double y1 = y + i * squareSize;

                gc.fillRect(x1, y1, squareSize, squareSize);
                if ((whiteKingInCheck && whiteKingPos != null && i == whiteKingPos[0] && j == whiteKingPos[1]) ||
                    (blackKingInCheck && blackKingPos != null && i == blackKingPos[0] && j == blackKingPos[1])) {
                    gc.setFill(checkColor);
                    gc.fillRect(x1, y1, squareSize, squareSize);
                }

                if (highlights != null && isHighlighted(highlights, i, j)) {
                    if (board[i][j] != 0) {
                        gc.setFill(captureHighlightColor);
                        gc.fillRect(x1, y1, squareSize, squareSize);
                    } else {
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

        drawcoords(gc, x, y);
    }

    private int[] findKingPosition(int[][] board, int kingValue) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == kingValue) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public boolean isCheck(int[][] board, int kingX, int kingY, int opponentSign) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (Integer.signum(board[i][j]) == opponentSign) {
                    List<int[]> possibleMoves = calculatePossibleMoves(board, i, j);
                    for (int[] move : possibleMoves) {
                        if (move[0] == kingX && move[1] == kingY) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<int[]> calculatePossibleMoves(int[][] board, int x, int y) {
        Game tempGame = new Game(8);
        tempGame.setBoard(board);
        return tempGame.calculatePossibleMoves(x, y);
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

    public double getSquareCenterX(int row, int col) {
        double boardStartX = (windowsWidth - (size * squareSize)) / 2.0;
        return boardStartX + (col * squareSize) + (squareSize / 2.0);
    }

    public double getSquareCenterY(int row, int col) {
        double boardStartY = (windowsHeight - (size * squareSize)) / 2.0;
        return boardStartY + (row * squareSize) + (squareSize / 2.0);
    }
}