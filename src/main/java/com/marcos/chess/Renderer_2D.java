package com.marcos.chess;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;

public class Renderer_2D implements Renderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private Canvas canvas;
    private DragHandler handler;
    private Pane pieceLayer;

    public Renderer_2D(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    @Override
    public Scene createGameScene(int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        int squareSize = 80;
        int size = 8;

        Game game =  GameFactory.getGameInstance(size);

        Board board = new Board(squareSize, size, windowsWidth, windowsHeight);
        canvas = new Canvas(windowsWidth, windowsHeight);

        pieceLayer = new Pane();
        pieceLayer.setMouseTransparent(true);
        pieceLayer.setPrefSize(windowsWidth, windowsHeight);

        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());

        handler = new DragHandler(board, game, canvas, pieceLayer, isMultiplayer);

        canvas.setOnMousePressed(handler::MousePressed);
        canvas.setOnMouseReleased(handler::MouseReleased);
        canvas.setOnMouseDragged(handler::MouseDragged);

        StackPane gameLayout = new StackPane();
        gameLayout.getChildren().addAll(canvas, pieceLayer);

        Scene scene = new Scene(gameLayout, windowsWidth, windowsHeight);

        //Listener for "F3" to switch to 3D mode
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.F3) {
                cleanup();
                Render_3D render_3D = new Render_3D(windowsWidth, windowsHeight);
                render_3D.initialize();
                render_3D.createGameScene(windowsWidth, windowsHeight, isMultiplayer);
            }
        });

        return scene;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void cleanup() {
        if (pieceLayer != null) {
            pieceLayer.getChildren().clear();
        }
    }
}