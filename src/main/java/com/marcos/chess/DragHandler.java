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
    private final IA ia;

    public DragHandler(Board board, Game game, Canvas canvas) {
        this.board = board;
        this.game = game;
        this.canvas = canvas;
        this.ia = new IA();
    }

 
    public void MousePressed(MouseEvent e) {
        int[][] board = game.getBoard();
        int[] pos = getCoord(e.getX(), e.getY());

        if (pos != null) {
            fromX = pos[0];
            fromY = pos[1];
            selectedPiece = board[fromX][fromY];

            
            if (selectedPiece != 0 && Integer.signum(selectedPiece) == currentPlayer) {
                possibleMoves = game.calculatePossibleMoves(fromX, fromY);
                redrawWithHighlight();
                board[fromX][fromY] = 0; 
            } else {
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

    public void MouseReleased(MouseEvent mouseEvent) {
        if (selectedPiece != 0) {
            int[][] board = game.getBoard();
            int[] pos = getCoord(mouseEvent.getX(), mouseEvent.getY());

            if (pos != null) {
                int x = pos[0];
                int y = pos[1];

                boolean isValidMove = possibleMoves != null && possibleMoves.stream()
                        .anyMatch(move -> move[0] == x && move[1] == y);

                if (isValidMove && Integer.signum(selectedPiece) == currentPlayer) {
                    board[x][y] = selectedPiece;
                    selectedPiece = 0;

                    if(currentPlayer == 1) {
                        changePlayer();
                        ia.makeMove(game);
                        changePlayer();
                        redraw();
                    }
                } else {
                    board[fromX][fromY] = selectedPiece;
                    selectedPiece = 0;
                }
            } else {
                board[fromX][fromY] = selectedPiece;
                selectedPiece = 0;
            }

            possibleMoves = null;
            redraw();
        }
    }

    private void changePlayer() {
        currentPlayer = -currentPlayer;
    }

    private void drawDrag() {
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
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

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
