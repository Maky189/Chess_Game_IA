package com.marcos.chess;

import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.input.MouseInput;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.scene.Spatial;
import javafx.scene.Scene;

import java.util.List;

public class Render_3D implements Renderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private ChessGame chessApp;
    private Game currentGame;

    public Render_3D(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    @Override
    public void initialize() {
        if (chessApp != null) {
            System.out.println("Init");
        }
    }

    @Override
    public Scene createGameScene(Game game, int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        this.currentGame = game;
        chessApp = new ChessGame(game, isMultiplayer);
        AppSettings settings = new AppSettings(true);

        settings.setFullscreen(true);

        java.awt.DisplayMode displayMode = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        settings.setFrequency(displayMode.getRefreshRate());

        settings.setResolution(displayMode.getWidth(), displayMode.getHeight());

        settings.setSamples(4);
        settings.setFrameRate(displayMode.getRefreshRate());
        settings.setGammaCorrection(true);

        chessApp.setSettings(settings);
        chessApp.setShowSettings(false);
        chessApp.start();

        return null;
    }

    private class ChessGame extends SimpleApplication {
        private static Game game;
        private Node boardNode;
        private List<int[]> currentHighlights = null;
        private Material highlightMaterial;
        private Node selectedPieceNode = null;
        private int[] selectedPosition = null;
        private static boolean isMultiplayer;
        private int currentPlayer = 1;
        private static IA ia;
        private Material whiteMaterial;
        private Material blackMaterial;

        public ChessGame(Game game, boolean isMultiplayer) {
            super(); // Call SimpleApplication constructor
            ChessGame.game = game; // Initialize the final field in the constructor
            ChessGame.ia = new IA();
            ChessGame.isMultiplayer = isMultiplayer;
        }

        @Override
        public void simpleInitApp() {
            initializeMaterials();
            setupCamera();
            setupLighting();
            setupInputHandling();
            boardNode = createChessBoard(game);
            rootNode.attachChild(boardNode);
        }

        private void initializeMaterials() {
            whiteMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            whiteMaterial.setColor("Diffuse", ColorRGBA.White);
            whiteMaterial.setBoolean("UseMaterialColors", true);

            blackMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            blackMaterial.setColor("Diffuse", ColorRGBA.Gray.mult(0.2f));
            blackMaterial.setBoolean("UseMaterialColors", true);

            highlightMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            highlightMaterial.setColor("Diffuse", new ColorRGBA(0, 0, 1f, 0.5f));
            highlightMaterial.setColor("Ambient", new ColorRGBA(0, 0, 0.5f, 0.5f));
            highlightMaterial.setBoolean("UseMaterialColors", true);
            highlightMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        }

        private void setupCamera() {
            cam.setLocation(new Vector3f(0, 10, 7));
            cam.lookAt(Vector3f.ZERO, Vector3f.ZERO);
        }

        private void setupCameraMultiplayer() {
            cam.setLocation(new Vector3f(0, 10, 7));
            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        }

        private void setupLighting() {
            viewPort.setBackgroundColor(new ColorRGBA(0.8f, 0.4f, 0.2f, 1.0f));

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -1.5f, -0.5f).normalizeLocal());
            sun.setColor(ColorRGBA.White.mult(0.7f));
            rootNode.addLight(sun);

            // Ambient light
            com.jme3.light.AmbientLight ambient = new com.jme3.light.AmbientLight();
            ambient.setColor(new ColorRGBA(0.3f, 0.25f, 0.2f, 1.0f));
            rootNode.addLight(ambient);

            // Secondary light
            DirectionalLight fillLight = new DirectionalLight();
            fillLight.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal());
            fillLight.setColor(ColorRGBA.White.mult(0.3f));
            rootNode.addLight(fillLight);
        }

        private void setupInputHandling() {
            // Setup Camera for my debug
            flyCam.setEnabled(false);

            inputManager.addMapping("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            inputManager.addListener(actionListener, "Select");

            // visibility of mouse
            inputManager.setCursorVisible(true);
        }

        private final ActionListener actionListener = (name, pressed, tpf) -> {
            //Check for user input on mouse
            if (name.equals("Select") && !pressed) {
                try {
                    Vector2f click2d = inputManager.getCursorPosition();
                    Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                    Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();

                    Ray ray = new Ray(click3d, dir);
                    CollisionResults results = new CollisionResults();
                    boardNode.collideWith(ray, results);

                    if (results.size() > 0) {
                        Geometry target = results.getClosestCollision().getGeometry();
                        if (target == null) return;

                        String[] parts = target.getName().split("_");
                        if (parts.length == 3 && parts[0].equals("square")) {
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            int piece = game.getBoard()[row][col];

                            if (selectedPieceNode == null) {
                                if (piece != 0 && Integer.signum(piece) == currentPlayer) {
                                    for (Spatial child : boardNode.getChildren()) {
                                        if (child instanceof Node && child.getLocalTranslation().x == col - 3.5f && child.getLocalTranslation().z == row - 3.5f) {
                                            selectedPieceNode = (Node) child;
                                            selectedPosition = new int[]{row, col};
                                            clearHighlights();
                                            currentHighlights = game.calculatePossibleMoves(row, col);
                                            highlightSquares();
                                            break;
                                        }
                                    }
                                }
                            } else {
                                boolean isValidMove = false;
                                if (currentHighlights != null) {
                                    for (int[] move : currentHighlights) {
                                        if (move[0] == row && move[1] == col) {
                                            isValidMove = true;
                                            break;
                                        }
                                    }
                                }

                                if (isValidMove) {
                                    game.getBoard()[row][col] = game.getBoard()[selectedPosition[0]][selectedPosition[1]];
                                    game.getBoard()[selectedPosition[0]][selectedPosition[1]] = 0;

                                    selectedPieceNode.setLocalTranslation(
                                            (col - 3.5f),
                                            0.2f,
                                            (row - 3.5f)
                                    );

                                    // Change player
                                    currentPlayer = -currentPlayer;

                                    if (currentPlayer == -1 && isMultiplayer) {
                                        setupCameraMultiplayer();
                                    }

                                    // AI move if not multiplayer
                                    if (!isMultiplayer && currentPlayer == -1) {
                                        ia.makeMove(game, -1);
                                        updateBoardVisuals();
                                        currentPlayer = 1;
                                    }
                                }

                                selectedPieceNode = null;
                                selectedPosition = null;
                                clearHighlights();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in mouse selection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        private void updateBoardVisuals() {
            for (Spatial child : boardNode.getChildren().toArray(new Spatial[0])) {
                if (child.getName() != null && child.getName().startsWith("piece_")) {
                    boardNode.detachChild(child);
                }
            }

            int[][] board = game.getBoard();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] != 0) {
                        Node pieceNode = loadPieceModel(board[i][j]);
                        if (pieceNode != null) {
                            pieceNode.setName("piece_" + i + "_" + j);
                            pieceNode.setLocalTranslation((j - 3.5f), 0.2f, (i - 3.5f));
                            boardNode.attachChild(pieceNode);
                        }
                    }
                }
            }
        }

        private void clearHighlights() {
            if (currentHighlights != null) {
                for (Spatial child : boardNode.getChildren()) {
                    if (child instanceof Geometry geo) {
                        String[] parts = geo.getName().split("_");
                        if (parts.length == 3) {
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            boolean isWhite = (row + col) % 2 == 0;
                            geo.setMaterial(isWhite ? whiteMaterial : blackMaterial);
                        }
                    }
                }
            }
            currentHighlights = null;
        }

        private void highlightSquares() {
            if (currentHighlights != null) {
                for (int[] pos : currentHighlights) {
                    String squareName = "square_" + pos[0] + "_" + pos[1];
                    Spatial square = boardNode.getChild(squareName);
                    if (square instanceof Geometry) {
                        ((Geometry) square).setMaterial(highlightMaterial);
                    }
                }
            }
        }

        private Node createChessBoard(Game game) {
            Node boardNode = new Node("chessBoard");
            float squareSize = 1.0f;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Box square = new Box(squareSize / 2, 0.1f, squareSize / 2);
                    Geometry squareGeo = new Geometry("square_" + row + "_" + col, square);

                    squareGeo.setUserData("row", row);
                    squareGeo.setUserData("col", col);

                    squareGeo.setMaterial((row + col) % 2 == 0 ? whiteMaterial : blackMaterial);

                    float x = (col - 3.5f) * squareSize;
                    float z = (row - 3.5f) * squareSize;
                    squareGeo.setLocalTranslation(x, 0, z);

                    boardNode.attachChild(squareGeo);
                }
            }

            int[][] board = game.getBoard();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    int piece = board[i][j];
                    if (piece != 0) {
                        Node pieceNode = loadPieceModel(piece);
                        if (pieceNode != null) {
                            float x = (j - 3.5f) * squareSize;
                            float z = (i - 3.5f) * squareSize;
                            pieceNode.setLocalTranslation(x, 0.2f, z);
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
                pieceNode.setName("piece_" + piece);

                Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

                if (piece > 0) {
                    material.setTexture("DiffuseMap", assetManager.loadTexture(texturePath + "piece_white.png"));
                    material.setColor("Ambient", new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
                } else {
                    material.setTexture("DiffuseMap", assetManager.loadTexture(texturePath + "piece_black.png"));
                    material.setColor("Ambient", new ColorRGBA(0.02f, 0.02f, 0.02f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
                }

                material.setBoolean("UseMaterialColors", true);
                material.setTransparent(false);
                material.setFloat("Shininess", 64f);

                material.getAdditionalRenderState().setBlendMode(BlendMode.Off);
                pieceNode.setQueueBucket(RenderQueue.Bucket.Opaque);

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
            chessApp.stop(true);
        }
    }
}