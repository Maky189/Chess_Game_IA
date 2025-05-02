package com.marcos.chess;

import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;

public class DragHandler {
    private final Board board;
    private final Game game;
    private final Canvas canvas;
    private final Pane pieceLayer;
    private int selectedPiece = 0;
    private int fromX = -1;
    private int fromY = -1;
    private double toX = 0;
    private double toY = 0;
    private List<int[]> possibleMoves = null;
    private int currentPlayer = 1;
    private final IA ia;
    private final boolean isMultiplayer;
    private boolean isAnimating = false;
    private ImageView movingPiece = null;
    private double animationProgress = 0;
    private AnimationTimer animator = null;
    private static final double DELAY = 0.5;

    public DragHandler(Board board, Game game, Canvas canvas, Pane pieceLayer, boolean isMultiplayer) {
        this.board = board;
        this.game = game;
        this.canvas = canvas;
        this.pieceLayer = pieceLayer;
        this.ia = new IA();
        this.isMultiplayer = isMultiplayer;
    }


    private void cleanupAnimation() {
        if (animator != null) {
            animator.stop();
        }
        if (movingPiece != null && pieceLayer.getChildren().contains(movingPiece)) {
            pieceLayer.getChildren().remove(movingPiece);
        }
        movingPiece = null;
        isAnimating = false;
    }

    public void MousePressed(MouseEvent e) {
        cleanupAnimation();

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

    private void handleAIMove() {
        PauseTransition pause = new PauseTransition(Duration.seconds(DELAY));
        pause.setOnFinished(event -> {
            IA.Move aiMove = ia.makeMove(game, -1);
            if (aiMove != null) {
                isAnimating = true;
                int piece = game.getBoard()[aiMove.fromX][aiMove.fromY];

                // Determine if this is an en passant capture BEFORE making any moves
                boolean isEnPassant = game.isEnPassantCapture(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY);
                int capturedPawnX = game.getLastMoveToX();
                int capturedPawnY = game.getLastMoveToY();

                // Clear the source square
                game.getBoard()[aiMove.fromX][aiMove.fromY] = 0;

                // If it's an en passant capture, remove the captured pawn immediately
                if (isEnPassant) {
                    game.getBoard()[capturedPawnX][capturedPawnY] = 0;
                }

                redraw();

                // Create animation
                ImageView aiPiece = new ImageView(board.obtainImage(piece));
                aiPiece.setFitWidth(board.getSquareSize());
                aiPiece.setFitHeight(board.getSquareSize());

                double offsetSquares = 4.6;
                double boardStartX = (board.getWindowsWidth() - (board.getSize() * board.getSquareSize())) / 2.0
                        + (offsetSquares * board.getSquareSize());
                double boardStartY = (board.getWindowsHeight() - (board.getSize() * board.getSquareSize())) / 2.0;

                double startX = boardStartX + (aiMove.fromY * board.getSquareSize());
                double startY = boardStartY + (aiMove.fromX * board.getSquareSize());
                double endX = boardStartX + (aiMove.toY * board.getSquareSize());
                double endY = boardStartY + (aiMove.toX * board.getSquareSize());

                aiPiece.setLayoutX(startX);
                aiPiece.setLayoutY(startY);

                pieceLayer.getChildren().add(aiPiece);

                TranslateTransition transition = new TranslateTransition(Duration.millis(400), aiPiece);
                transition.setFromX(0);
                transition.setFromY(0);
                transition.setToX(endX - startX);
                transition.setToY(endY - startY);

                transition.setOnFinished(e -> {
                    pieceLayer.getChildren().remove(aiPiece);
                    // Place the piece in its final position
                    game.getBoard()[aiMove.toX][aiMove.toY] = piece;

                    // Update the last move info
                    game.updateLastMove(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY);

                    redraw();
                    isAnimating = false;
                    changePlayer();
                });

                transition.play();
            }
        });
        pause.play();
    }

    public void MouseReleased(MouseEvent mouseEvent) {
        if (isAnimating || selectedPiece == 0) return;

        int[] pos = getCoord(mouseEvent.getX(), mouseEvent.getY());
        boolean validMove = false;

        if (pos != null && possibleMoves != null) {
            validMove = possibleMoves.stream()
                    .anyMatch(move -> move[0] == pos[0] && move[1] == pos[1]);

            if (validMove) {
                // Handle special moves first
                int startY = fromY;
                boolean isCastling = game.isCastlingMove(fromX, fromY, pos[0], pos[1]);
                boolean isEnPassant = game.isEnPassantCapture(fromX, fromY, pos[0], pos[1]);

                // Make the basic move
                game.getBoard()[pos[0]][pos[1]] = selectedPiece;
                game.getBoard()[fromX][fromY] = 0;

                // Handle castling
                if (Math.abs(selectedPiece) == 6 && Math.abs(pos[1] - startY) == 2) {
                    if (pos[1] > startY) {
                        game.performKingsideCastle(pos[0]);
                    }
                    else {
                        game.performQueensideCastle(pos[0]);
                    }
                }
                // Handle en passant
                if (isEnPassant) {
                    int capturedPawnRow = fromX; // Same row as moving pawn
                    int capturedPawnCol = pos[1]; // Same column as destination
                    game.getBoard()[capturedPawnRow][capturedPawnCol] = 0;
                }

                // Update game state
                game.updateLastMove(fromX, fromY, pos[0], pos[1]);
                redraw();
                changePlayer();

                if (!isMultiplayer) {
                    handleAIMove();
                }
            }
        }

        if (!validMove) {
            game.getBoard()[fromX][fromY] = selectedPiece;
        }

        selectedPiece = 0;
        fromX = -1;
        fromY = -1;
        possibleMoves = null;
        redraw();
    }

    public void MouseDragged(MouseEvent mouseEvent) {
        if (isAnimating) return;
        if (selectedPiece != 0) {
            toX = mouseEvent.getX();
            toY = mouseEvent.getY();
            drawDrag();
        }
    }

    private void drawDrag() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (possibleMoves != null) {
            board.drawBoardWithHighlights(canvas.getGraphicsContext2D(), game.getBoard(), possibleMoves);
        } else {
            board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());
        }

        if (selectedPiece != 0) {
            double x = toX - board.getSquareSize() / 2.0;
            double y = toY - board.getSquareSize() / 2.0;
            canvas.getGraphicsContext2D().drawImage(
                    board.obtainImage(selectedPiece),
                    x, y,
                    board.getSquareSize(),
                    board.getSquareSize()
            );
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

    private void changePlayer() {
        currentPlayer = -currentPlayer;
    }

    private int[] getCoord(double x, double y) {
        double boardStartX = (board.getWindowsWidth() - board.getSize() * board.getSquareSize()) / 2.0;
        double boardStartY = (board.getWindowsHeight() - board.getSize() * board.getSquareSize()) / 2.0;

        int col = (int) ((x - boardStartX) / board.getSquareSize());
        int row = (int) ((y - boardStartY) / board.getSquareSize());

        if (row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize()) {
            return new int[] {row, col};
        }
        return null;
    }

    public void cleanup() {
        cleanupAnimation();
    }
}