package com.marcos.chess;

import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.util.List;

import com.marcos.chess.networking.GameSession;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private final IA ia;
    private final boolean isMultiplayer;
    private boolean isAnimating = false;
    private ImageView movingPiece = null;
    private double animationProgress = 0;
    private AnimationTimer animator = null;
    private static final double DELAY = 0.5;
    private GameSession gameSession;

    public DragHandler(Board board, Game game, Canvas canvas, Pane pieceLayer, boolean isMultiplayer) {
        this.board = board;
        this.game = game;
        this.canvas = canvas;
        this.pieceLayer = pieceLayer;
        this.ia = new IA();
        this.isMultiplayer = isMultiplayer;
    }

    public void setGameSession(GameSession session) {
        this.gameSession = session;
        if (gameSession != null) {
            gameSession.listenForOpponentMove(this);
        }
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

            if (selectedPiece != 0 && Integer.signum(selectedPiece) == game.getCurrentPlayer()) {
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

                // Check if this is a castling move
                boolean isCastling = Math.abs(piece) == 6 && Math.abs(aiMove.fromY - aiMove.toY) == 2;

                game.getBoard()[aiMove.fromX][aiMove.fromY] = 0;

                // If castling, handle rook movement
                if (isCastling) {
                    if (aiMove.toY > aiMove.fromY) {
                        game.performKingsideCastle(aiMove.toX);
                    } else {
                        game.performQueensideCastle(aiMove.toX);
                    }
                }

                redraw();

                // Create animation
                ImageView aiPiece = new ImageView(board.obtainImage(piece));
                aiPiece.setFitWidth(board.getSquareSize());
                aiPiece.setFitHeight(board.getSquareSize());

                double offsetSquares = 4.6;
                double boardStartX = (board.getWindowsWidth() - (board.getSize() * board.getSquareSize())) / 2.0 + (offsetSquares * board.getSquareSize());
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

                    // Update the last move
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
            validMove = possibleMoves.stream().anyMatch(move -> move[0] == pos[0] && move[1] == pos[1]);

            if (validMove) {
                // special moves first
                int startY = fromY;
                boolean isCastling = game.isCastlingMove(fromX, fromY, pos[0], pos[1]);
                boolean isEnPassant = game.isEnPassantCapture(fromX, fromY, pos[0], pos[1]);

                // Then make the basic move
                game.getBoard()[pos[0]][pos[1]] = selectedPiece;
                game.getBoard()[fromX][fromY] = 0;

                // castling
                if (Math.abs(selectedPiece) == 6 && Math.abs(pos[1] - startY) == 2) {
                    if (pos[1] > startY) {
                        game.performKingsideCastle(pos[0]);
                    }
                    else {
                        game.performQueensideCastle(pos[0]);
                    }
                }
                // en passant
                if (isEnPassant) {
                    int capturedPawnRow = fromX;
                    int capturedPawnCol = pos[1];
                    game.getBoard()[capturedPawnRow][capturedPawnCol] = 0;
                }

                // pawn promotion
                if (Math.abs(selectedPiece) == 1) {
                    if ((selectedPiece == 1 && pos[0] == 0) || (selectedPiece == -1 && pos[0] == 7)) {
                        promotionModal(pos[0], pos[1], Integer.signum(selectedPiece));
                        return;
                    }
                }

                // Update game state
                game.updateLastMove(fromX, fromY, pos[0], pos[1]);
                redraw();
                changePlayer();

                if (!isMultiplayer) {
                    handleAIMove();
                } else if (gameSession != null) {
                    gameSession.makeMove(fromX, fromY, pos[0], pos[1]);
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

    private void changePlayer() {
        game.switchPlayer();
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

    private void promotionModal(int toX, int toY, int color) {
        Stage menu = new Stage();
        menu.initModality(Modality.APPLICATION_MODAL);
        menu.initOwner((Stage) canvas.getScene().getWindow());
        menu.setTitle("Promote Pawn");

        HBox piecesBox = new HBox(20);
        piecesBox.setAlignment(Pos.CENTER);
        piecesBox.setPadding(new Insets(20));
        piecesBox.setStyle("-fx-background-color: white;");

        // Options
        int[] promotionPieces = {2, 3, 4, 5};
        String[] pieceNames = {"Rook", "Knight", "Bishop", "Queen"};

        for (int i = 0; i < promotionPieces.length; i++) {
            final int piece = promotionPieces[i] * color;
            VBox pieceBox = new VBox(10);
            pieceBox.setAlignment(Pos.CENTER);

            ImageView pieceImage = new ImageView(board.obtainImage(piece));
            pieceImage.setFitWidth(board.getSquareSize());
            pieceImage.setFitHeight(board.getSquareSize());

            Text pieceName = new Text(pieceNames[i]);
            pieceName.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            pieceBox.getChildren().addAll(pieceImage, pieceName);
            pieceBox.setOnMouseClicked(e -> {
                game.getBoard()[toX][toY] = piece;
                redraw();
                menu.close();
                changePlayer();
                if (!isMultiplayer) {
                    handleAIMove();
                }
            });

            // effect
            pieceBox.setOnMouseEntered(e -> pieceBox.setStyle("-fx-background-color: lightgray; -fx-cursor: hand;"));
            pieceBox.setOnMouseExited(e -> pieceBox.setStyle("-fx-background-color: transparent;"));

            piecesBox.getChildren().add(pieceBox);
        }

        Scene dialogScene = new Scene(piecesBox);
        menu.setScene(dialogScene);
        menu.show();
    }


    public void redrawBoard() {
        redraw();
    }
}