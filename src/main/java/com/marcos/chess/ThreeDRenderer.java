package com.marcos.chess;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.embed.swing.SwingNode;
import java.awt.Canvas;
import javax.swing.JPanel;


public class ThreeDRenderer implements GameRenderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private ChessGame chessApp;
    private Game currentGame;

    public ThreeDRenderer(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    @Override
    public void initialize() {
        if (chessApp != null) {
            // Any initialization needed for the 3D renderer
            System.out.println("Initializing 3D renderer...");
        }
    }

    @Override
    public Scene createGameScene(Game game, int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        this.currentGame = game;
        chessApp = new ChessGame(game); // Pass the Game instance
        AppSettings settings = new AppSettings(true);

        // Set full-screen mode
        settings.setFullscreen(true);

        // Dynamically set the refresh rate
        java.awt.DisplayMode displayMode = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        settings.setFrequency(displayMode.getRefreshRate());

        // Set resolution to the screen's resolution
        settings.setResolution(displayMode.getWidth(), displayMode.getHeight());

        // Other settings
        settings.setSamples(4); // Anti-aliasing
        settings.setFrameRate(displayMode.getRefreshRate()); // Match refresh rate
        settings.setGammaCorrection(true); // Enable gamma correction

        chessApp.setSettings(settings);
        chessApp.setShowSettings(false); // Disable the default JME settings menu
        chessApp.start(); // Start the application in full-screen mode

        // Return null since JME handles its own window
        return null;
    }

    private class ChessGame extends SimpleApplication {
        private final Game game;

        public ChessGame(Game game) {
            this.game = game;
        }

        @Override
        public void simpleInitApp() {
            System.out.println("JME3 simpleInitApp called!");

            // Set camera position and orientation
            cam.setLocation(new Vector3f(0, 5, 10));
            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

            // Change the background color to darker orange
            viewPort.setBackgroundColor(new ColorRGBA(0.8f, 0.4f, 0.2f, 1.0f));

            // Main directional light - adjusted for better definition
            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -1.5f, -0.5f).normalizeLocal());
            sun.setColor(ColorRGBA.White.mult(0.7f));
            rootNode.addLight(sun);

            // Ambient light - warmer color
            com.jme3.light.AmbientLight ambient = new com.jme3.light.AmbientLight();
            ambient.setColor(new ColorRGBA(0.3f, 0.25f, 0.2f, 1.0f));
            rootNode.addLight(ambient);

            // Secondary fill light - softer
            DirectionalLight fillLight = new DirectionalLight();
            fillLight.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal());
            fillLight.setColor(ColorRGBA.White.mult(0.3f));
            rootNode.addLight(fillLight);

            // Attach the chessboard
            rootNode.attachChild(createChessBoard(game));
        }

        private Node createChessBoard(Game game) {
            Node boardNode = new Node("chessBoard");

            // Create materials for white and black squares
            Material whiteMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            whiteMaterial.setColor("Diffuse", ColorRGBA.White);
            whiteMaterial.setBoolean("UseMaterialColors", true);

            Material blackMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            blackMaterial.setColor("Diffuse", ColorRGBA.Gray.mult(0.2f));
            blackMaterial.setBoolean("UseMaterialColors", true);

            // Create squares
            float squareSize = 1.0f;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Box square = new Box(squareSize / 2, 0.1f, squareSize / 2);
                    Geometry squareGeo = new Geometry("square_" + row + "_" + col, square);

                    // Set material based on position
                    squareGeo.setMaterial((row + col) % 2 == 0 ? whiteMaterial : blackMaterial);

                    // Position the square
                    float x = (col - 3.5f) * squareSize;
                    float z = (row - 3.5f) * squareSize;
                    squareGeo.setLocalTranslation(x, 0, z);

                    boardNode.attachChild(squareGeo);
                }
            }

            // Place pieces based on the Game's board
            int[][] board = game.getBoard();
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    int piece = board[row][col];
                    if (piece != 0) {
                        Node pieceNode = loadPieceModel(piece);
                        if (pieceNode != null) {
                            // Position the piece on the corresponding square
                            float x = (col - 3.5f) * squareSize;
                            float z = (row - 3.5f) * squareSize;
                            pieceNode.setLocalTranslation(x, 0.2f, z); // Slightly above the board
                            boardNode.attachChild(pieceNode);
                        }
                    }
                }
            }

            return boardNode;
        }

        private Node loadPieceModel(int piece) {
            String modelPath = "assets/3Dpieces/";
            String texturePath = "textures/";
            Node pieceNode = null;

            switch (piece) {
                case 1: pieceNode = (Node) assetManager.loadModel(modelPath + "pawn_white.j3o"); break;
                case -1: pieceNode = (Node) assetManager.loadModel(modelPath + "pawn_black.j3o"); break;
                case 2: pieceNode = (Node) assetManager.loadModel(modelPath + "rook_white.j3o"); break;
                case -2: pieceNode = (Node) assetManager.loadModel(modelPath + "rook_black.j3o"); break;
                case 3: pieceNode = (Node) assetManager.loadModel(modelPath + "knight_white.j3o"); break;
                case -3: pieceNode = (Node) assetManager.loadModel(modelPath + "knight_black.j3o"); break;
                case 4: pieceNode = (Node) assetManager.loadModel(modelPath + "bishop_white.j3o"); break;
                case -4: pieceNode = (Node) assetManager.loadModel(modelPath + "bishop_black.j3o"); break;
                case 5: pieceNode = (Node) assetManager.loadModel(modelPath + "queen_white.j3o"); break;
                case -5: pieceNode = (Node) assetManager.loadModel(modelPath + "queen_black.j3o"); break;
                case 6: pieceNode = (Node) assetManager.loadModel(modelPath + "king_white.j3o"); break;
                case -6: pieceNode = (Node) assetManager.loadModel(modelPath + "king_black.j3o"); break;
                default: return null;
            }

            if (pieceNode != null) {
                Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                
                // Apply textures based on piece color
                if (piece > 0) {
                    // White pieces
                    material.setTexture("DiffuseMap", assetManager.loadTexture(texturePath + "piece_white.png"));
                    material.setColor("Ambient", new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
                } else {
                    // Black pieces
                    material.setTexture("DiffuseMap", assetManager.loadTexture(texturePath + "piece_black.png"));
                    material.setColor("Ambient", new ColorRGBA(0.02f, 0.02f, 0.02f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
                }
                
                // Make pieces completely solid
                material.setBoolean("UseMaterialColors", true);
                material.setTransparent(false);
                material.setFloat("Shininess", 64f);
                
                // Apply material to all geometries in the piece
                pieceNode.depthFirstTraversal(spatial -> {
                    if (spatial instanceof Geometry) {
                        ((Geometry) spatial).setMaterial(material);
                    }
                });
            }

            return pieceNode;
        }
    }

    @Override
    public void cleanup() {
        if (chessApp != null) {
            chessApp.stop();
        }
    }
}