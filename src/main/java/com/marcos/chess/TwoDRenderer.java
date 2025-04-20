package com.marcos.chess;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

public class TwoDRenderer implements GameRenderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private Canvas canvas;
    private DragHandler handler;

    public TwoDRenderer(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    @Override
    public Scene createGameScene(Game game, int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        int squareSize = 80;
        int size = 8;
        
        Board board = new Board(squareSize, size, windowsWidth, windowsHeight);
        canvas = new Canvas(windowsWidth, windowsHeight);
        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());
        
        handler = new DragHandler(board, game, canvas, isMultiplayer);
        canvas.setOnMousePressed(handler::MousePressed);
        canvas.setOnMouseReleased(handler::MouseReleased);
        canvas.setOnMouseDragged(handler::MouseDragged);
        
        StackPane gameLayout = new StackPane(canvas);
        return new Scene(gameLayout, windowsWidth, windowsHeight);
    }

    @Override
    public void initialize() {
        // No initialization needed for 2D renderer
    }

    @Override
    public void cleanup() {
        // No cleanup needed for 2D renderer
    }
}