package com.marcos.chess;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.List;

public class DragHandler {

    private final Board board;
    private final Game game;
    private final Canvas canvas;
    private int selectedPiece = 0;
    private int fromX = -1;
    private int fromY = -1;
    private double toX = 0;
    private double toY = 0;
    private List<int[]> possibleMoves = null;
    private int currentPlayer = 1;

    public DragHandler(Board board, Game game, Canvas canvas) {
        this.board = board;
        this.game = game;
        this.canvas = canvas;
    }

    // Start dragging
    public void MousePressed(MouseEvent e) {
        int[][] board = game.getBoard();
        int[] pos = getCoord(e.getX(), e.getY());

        if (pos != null) {
            fromX = pos[0];
            fromY = pos[1];
            selectedPiece = board[fromX][fromY];

            // Allow selection only if the piece belongs to the current player
            if (selectedPiece != 0 && Integer.signum(selectedPiece) == currentPlayer) {
                possibleMoves = game.calculatePossibleMoves(fromX, fromY);
                redrawWithHighlight();
                board[fromX][fromY] = 0; // Temporarily remove the piece
            } else {
                // Clear selection if turn or piece is invalid
                selectedPiece = 0;
                clear();
            }
        } else {
            selectedPiece = 0;
            clear();
        }
    }

    public void MouseDragged(MouseEvent mouseEvent) {
        if (selectedPiece != 0) {
            toX = mouseEvent.getX();
            toY = mouseEvent.getY();
            drawDrag();
        }
    }

    //Mouse released
    public void MouseReleased(MouseEvent mouseEvent) {
        if (selectedPiece != 0) {
            int[][] board = game.getBoard();
            int[] pos = getCoord(mouseEvent.getX(), mouseEvent.getY());

            if (pos != null) {
                int x = pos[0];
                int y = pos[1];

                // Check if the target position is valid
                boolean isValidMove = possibleMoves != null && possibleMoves.stream()
                        .anyMatch(move -> move[0] == x && move[1] == y);

                if (isValidMove && Integer.signum(selectedPiece) == currentPlayer) {
                    // Place the piece at the new position
                    board[x][y] = selectedPiece;
                    selectedPiece = 0;

                    // Change player turn
                    changePlayer();
                } else {
                    // Invalid move: return piece to the original position
                    board[fromX][fromY] = selectedPiece;
                    selectedPiece = 0;
                }
            } else {
                // Invalid drop (outside the board): return piece to the original position
                board[fromX][fromY] = selectedPiece;
                selectedPiece = 0;
            }

            // Clear move highlights and redraw the board
            possibleMoves = null;
            redraw();
        }
    }

    private void changePlayer() {
        currentPlayer = -currentPlayer;
    }

    // Redraw the board with the piece beign dragged
    private void drawDrag() {
        // Clear the canvas before redrawing
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (possibleMoves != null) {
            board.drawBoardWithHighlights(canvas.getGraphicsContext2D(), game.getBoard(), possibleMoves);
        }
        else {
            board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());
        }

        if(selectedPiece != 0) {
            double x = toX - board.getSquareSize() / 2.0;
            double y = toY - board.getSquareSize() / 2.0;

            canvas.getGraphicsContext2D().drawImage(board.obtainImage(selectedPiece), x, y, board.getSquareSize(), board.getSquareSize());
        }
    }

    private void redraw() {
        //Clear board
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //Update Board
        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());
    }

    private void redrawWithHighlight() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        board.drawBoardWithHighlights(canvas.getGraphicsContext2D(), game.getBoard(), possibleMoves);
    }

    private void clear() {
        possibleMoves = null;
        redraw();
    }

    private int[] getCoord(double x, double y) {
        double boardWidth = (board.getWindowsWidth() - board.getSquareSize() * board.getSize()) / 2.0;
        double boardHeight = (board.getWindowsHeight() - board.getSquareSize() * board.getSize()) / 2.0;

        int j = (int) ((x - boardWidth) / board.getSquareSize());
        int i = (int) ((y - boardHeight) / board.getSquareSize());

        if (i >= 0 && i < board.getSize() && j >= 0 && j < board.getSize()) {
            return new int[] {i, j};
        }

        return null;
    }
}
