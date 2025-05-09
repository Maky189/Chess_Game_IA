package com.marcos.chess;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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
import com.jme3.material.TechniqueDef.LightMode;
import com.jme3.input.MouseInput;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.material.RenderState;
import com.jme3.scene.Spatial;
import javafx.scene.Scene;

import java.util.List;

import com.jme3.scene.control.AbstractControl;
import com.jme3.renderer.ViewPort;
import com.jme3.math.FastMath;

public class Render_3D implements Renderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private ChessGame chessApp;
    private Game game = MainGame.getGameInstance(8);

    public Render_3D(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    @Override
    public void initialize() {
        if (chessApp != null) {
            return;
        }
    }

    @Override
    public Scene createGameScene(int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        chessApp = new ChessGame(game, isMultiplayer);
        AppSettings settings = new AppSettings(true);

        settings.setFullscreen(true);

        java.awt.DisplayMode displayMode = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
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
        private static IA ia;
        private Material whiteMaterial;
        private Material blackMaterial;
        private boolean flyCamEnabled = false;
        private Vector3f lastCameraPosition;
        private Vector3f lastCameraDirection;

        public ChessGame(Game game, boolean isMultiplayer) {
            super();
            ChessGame.game = game;
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

            // Initialize audio only if not already playing
            Audio.getInstance(assetManager).initializeIfNeeded();
        }

        private void initializeMaterials() {
            whiteMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            whiteMaterial.setColor("Diffuse", ColorRGBA.White);
            whiteMaterial.setBoolean("UseMaterialColors", true);

            blackMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            blackMaterial.setColor("Diffuse", ColorRGBA.DarkGray.mult(0.2f));
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
            sun.setColor(ColorRGBA.White.mult(1.0f));
            rootNode.addLight(sun);

            com.jme3.light.AmbientLight ambient = new com.jme3.light.AmbientLight();
            ambient.setColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
            rootNode.addLight(ambient);

            DirectionalLight fillLight = new DirectionalLight();
            fillLight.setDirection(new Vector3f(1f, -0.8f, 0.8f).normalizeLocal());
            fillLight.setColor(ColorRGBA.White.mult(0.3f));
            rootNode.addLight(fillLight);

            DirectionalLight backLight = new DirectionalLight();
            backLight.setDirection(new Vector3f(0.5f, -0.5f, -1f).normalizeLocal());
            backLight.setColor(ColorRGBA.White.mult(0.2f));
            rootNode.addLight(backLight);

            DirectionalLight rimLight = new DirectionalLight();
            rimLight.setDirection(new Vector3f(0.0f, -0.5f, 1.0f).normalizeLocal());
            rimLight.setColor(ColorRGBA.White.mult(0.4f));
            rootNode.addLight(rimLight);

            DirectionalLight groundLight = new DirectionalLight();
            groundLight.setDirection(new Vector3f(0.0f, 1.0f, 0.0f).normalizeLocal());
            groundLight.setColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
            rootNode.addLight(groundLight);

            renderManager.setPreferredLightMode(LightMode.SinglePass);
            renderManager.setSinglePassLightBatchSize(3);
        }

        private void setupInputHandling() {
            // Setup Camera for my debug
            flyCam.setEnabled(false);

            inputManager.addMapping("Toggle2D", new KeyTrigger(KeyInput.KEY_F3));
            inputManager.addListener(actionListener, "Toggle2D");

            inputManager.addMapping("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            inputManager.addListener(actionListener, "Select");

            inputManager.addMapping("ToggleFlyCam", new KeyTrigger(KeyInput.KEY_F6));
            inputManager.addListener(actionListener, "ToggleFlyCam");

            lastCameraPosition = new Vector3f(0, 10, 7);
            lastCameraDirection = Vector3f.ZERO;

            // visibility of mouse
            inputManager.setCursorVisible(true);


        }

        private final ActionListener actionListener = (name, pressed, tpf) -> {

            if (name.equals("ToggleFlyCam") && !pressed) {
                flyCamEnabled = !flyCamEnabled;
                if (flyCamEnabled) {
                    lastCameraPosition = cam.getLocation().clone();
                    lastCameraDirection = cam.getDirection().clone();
                    flyCam.setEnabled(true);
                    inputManager.setCursorVisible(false);
                } else {
                    flyCam.setEnabled(false);
                    inputManager.setCursorVisible(true);
                    cam.setLocation(lastCameraPosition);
                    cam.lookAt(lastCameraDirection, Vector3f.UNIT_Y);
                }
            }


            if (name.equals("Toggle2D") && !pressed) {
                stop();
            }

            if (name.equals("Select") && !pressed) {
                try {
                    // From camera to the point I click
                    Vector2f click2d = inputManager.getCursorPosition();
                    Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                    Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                    Ray ray = new Ray(click3d, dir);

                    // Chek for colisions
                    CollisionResults results = new CollisionResults();
                    boardNode.collideWith(ray, results);

                    if (results.size() > 0) {
                        // Check for the first colision
                        for (int i = 0; i < results.size(); i++) {
                            Geometry target = results.getCollision(i).getGeometry();
                            if (target == null) continue;

                            // Try to get the coordenates
                            int[] coords = null;
                            if (target.getName().startsWith("piece_")) {
                                coords = getPieceCoordinates(target);
                            } else if (target.getName().startsWith("square_")) {
                                coords = getSquareCoordinates(target);
                            }

                            if (coords != null) {
                                handleSelection(coords[0], coords[1]);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        private int[] getPieceCoordinates(Geometry pieceGeometry) {
            Node parent = pieceGeometry.getParent();
            if (parent != null) {
                Vector3f pos = parent.getLocalTranslation();
                int col = Math.round(pos.x + 3.5f);
                int row = Math.round(pos.z + 3.5f);
                return new int[]{row, col};
            }
            return null;
        }

        private int[] getSquareCoordinates(Geometry squareGeometry) {
            String[] parts = squareGeometry.getName().split("_");
            if (parts.length >= 3) {
                try {
                    int i = Integer.parseInt(parts[1]);
                    int j = Integer.parseInt(parts[2]);
                    return new int[]{i, j};
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }

        private void handleSelection(int x, int y) {
            int piece = game.getBoard()[x][y];

            if (selectedPieceNode == null) {
                if (piece != 0 && Integer.signum(piece) == game.getCurrentPlayer()) {
                    Node pieceNode = findPieceNodeAt(x, y);
                    if (pieceNode != null) {
                        selectedPieceNode = pieceNode;
                        selectedPosition = new int[]{x, y};
                        clearHighlights();
                        currentHighlights = game.calculatePossibleMoves(x, y);
                        highlightSquares();
                    }
                }
            } else {
                // move the piece that is selected
                boolean validMove = isValidMove(x, y);
                if (validMove) {
                    int[] startPos = selectedPosition.clone();
                    Node movingPiece = selectedPieceNode;

                    movePiece(startPos[0], startPos[1], x, y, movingPiece);

                    selectedPieceNode = null;
                    selectedPosition = null;
                    clearHighlights();
                } else {
                    selectedPieceNode = null;
                    selectedPosition = null;
                    clearHighlights();
                }
            }
        }

        private Node findPieceNodeAt(int x, int y) {
            for (Spatial child : boardNode.getChildren()) {
                if (child instanceof Node) {
                    Vector3f pos = child.getLocalTranslation();
                    if (Math.abs(pos.x - (y - 3.5f)) < 0.1f &&
                            Math.abs(pos.z - (x - 3.5f)) < 0.1f) {
                        return (Node) child;
                    }
                }
            }
            return null;
        }

        private boolean isValidMove(int x, int y) {
            if (currentHighlights != null) {
                for (int[] move : currentHighlights) {
                    if (move[0] == x && move[1] == y) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void movePiece(int fromX, int fromY, int toX, int toY, Node pieceNode) {
            int movingPiece = game.getBoard()[fromX][fromY];
            float liftHeight = 1.0f;
            float moveDuration = 0.5f;

            // Check if it's a capture or castling move
            boolean isCapture = game.getBoard()[toX][toY] != 0 || game.isEnPassantCapture(fromX, fromY, toX, toY);
            boolean isCastling = Math.abs(movingPiece) == 6 && Math.abs(fromY - toY) == 2;

            // Play appropriate sound
            if (isCapture) {
                Audio.getInstance(assetManager).playCaptureSound();
            } else if (isCastling) {
                Audio.getInstance(assetManager).playCastleSound();
            } else {
                Audio.getInstance(assetManager).playMoveSound(); // Regular move sound
            }

            // Check for en passant capture
            if (game.isEnPassantCapture(fromX, fromY, toX, toY)) {
                // For en passant, we need to remove the pawn that just moved two squares
                Node capturedPawn = findPieceNodeAt(game.getLastMoveToX(), game.getLastMoveToY());
                if (capturedPawn != null) {
                    boardNode.detachChild(capturedPawn);
                }
            } else {
                Node capturedPiece = findPieceNodeAt(toX, toY);
                if (capturedPiece != null) {
                    boardNode.detachChild(capturedPiece);
                }
            }

            // Check for castling move
            if (isCastling) {
                // see the ype of castling
                boolean isKingside = toY > fromY;
                int rookFromCol = isKingside ? 7 : 0;
                int rookToCol = isKingside ? 5 : 3;

                // Find rook
                Node rookNode = findPieceNodeAt(fromX, rookFromCol);
                if (rookNode != null) {
                    AnimationControl rookAnim = new AnimationControl(rookNode, new Vector3f((rookFromCol - 3.5f), 0.2f, (fromX - 3.5f)), new Vector3f((rookToCol - 3.5f), 0.2f, (fromX - 3.5f)), liftHeight, moveDuration, null);
                    rookNode.addControl(rookAnim);
                }
            }

            AnimationControl kingAnim = new AnimationControl(pieceNode, new Vector3f((fromY - 3.5f), 0.2f, (fromX - 3.5f)), new Vector3f((toY - 3.5f), 0.2f, (toX - 3.5f)), liftHeight, moveDuration, () -> {
                        game.getBoard()[toX][toY] = movingPiece;
                        game.getBoard()[fromX][fromY] = 0;

                        if (game.isEnPassantCapture(fromX, fromY, toX, toY)) {
                            game.performEnPassantCapture(toX, toY);
                        }

                        // Update last move AFTER updating the board but BEFORE castling
                        game.updateLastMove(fromX, fromY, toX, toY);

                        if (isCastling) {
                            if (toY > fromY) {
                                game.performKingsideCastle(fromX);
                            } else {
                                game.performQueensideCastle(fromX);
                            }
                        }

                        changePlayer();
                        if (!isMultiplayer && game.getCurrentPlayer() == -1) {
                            handleAIMove();
                        }
                    });

            pieceNode.addControl(kingAnim);
        }

        private void changePlayer() {
            game.switchPlayer();
        }

        private void handleAIMove() {
            IA.Move aiMove = ia.makeMove(game, -1);
            if (aiMove != null) {
                int piece = game.getBoard()[aiMove.fromX][aiMove.fromY];

                Node pieceToMove = findPieceNodeAt(aiMove.fromX, aiMove.fromY);
                if (pieceToMove != null) {
                    Node capturedPiece = findPieceNodeAt(aiMove.toX, aiMove.toY);
                    if (capturedPiece != null) {
                        boardNode.detachChild(capturedPiece);
                    }

                    float liftHeight = 1.0f;
                    float moveDuration = 0.5f;

                    AnimationControl anim = new AnimationControl(pieceToMove, new Vector3f((aiMove.fromY - 3.5f), 0.2f, (aiMove.fromX - 3.5f)), new Vector3f((aiMove.toY - 3.5f), 0.2f, (aiMove.toX - 3.5f)), liftHeight, moveDuration, () -> {
                                game.getBoard()[aiMove.toX][aiMove.toY] = piece;
                                game.getBoard()[aiMove.fromX][aiMove.fromY] = 0;

                                if (game.isEnPassantCapture(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY)) {
                                    game.performEnPassantCapture(aiMove.toX, aiMove.toY);
                                }
                                game.updateLastMove(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY);

                                changePlayer();
                            });

                    pieceToMove.addControl(anim);
                }
            }
        }

        private class AnimationControl extends AbstractControl {
            private final Vector3f startPos;
            private final Vector3f endPos;
            private final float liftHeight;
            private final float duration;
            private float time = 0;
            private final Runnable onComplete;

            public AnimationControl(Node target, Vector3f start, Vector3f end, float liftHeight, float duration, Runnable onComplete) {
                this.startPos = start;
                this.endPos = end;
                this.liftHeight = liftHeight;
                this.duration = duration;
                this.onComplete = onComplete;
            }

            @Override
            protected void controlUpdate(float tpf) {
                time += tpf;
                float progress = Math.min(time / duration, 1.0f);

                float smoothProgress = progress * progress * (3 - 2 * progress);

                // Vertical movement
                float heightProgress = 4 * smoothProgress * (1 - smoothProgress);
                float currentHeight = 0.2f + (liftHeight * heightProgress);

                // Horizontal movement
                float x = FastMath.interpolateLinear(smoothProgress, startPos.x, endPos.x);
                float z = FastMath.interpolateLinear(smoothProgress, startPos.z, endPos.z);

                // Position update
                spatial.setLocalTranslation(x, currentHeight, z);

                // When the animation is complete
                if (progress >= 1.0f) {
                    spatial.removeControl(this);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {
                // I am probably gonna use this later
            }
        }

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
                    // Reduced brightness for white pieces
                    material.setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
                    material.setFloat("Shininess", 64f);
                    material.setBoolean("UseMaterialColors", true);
                    material.setColor("GlowColor", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
                } else {
                    material.setTexture("DiffuseMap", assetManager.loadTexture(texturePath + "piece_black.png"));
                    material.setColor("Ambient", new ColorRGBA(0.02f, 0.02f, 0.02f, 1.0f));
                    material.setColor("Diffuse", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
                    material.setColor("Specular", new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));

                    material.setFloat("Shininess", 96f);
                    material.setBoolean("UseMaterialColors", true);
                    material.setColor("GlowColor", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
                }

                material.setBoolean("UseMaterialColors", true);
                material.setTransparent(false);
                material.setFloat("Shininess", 64f);

                material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                material.getAdditionalRenderState().setDepthTest(true);
                material.getAdditionalRenderState().setDepthWrite(true);

                material.getAdditionalRenderState().setBlendMode(BlendMode.Off);
                pieceNode.setQueueBucket(RenderQueue.Bucket.Opaque);

                pieceNode.depthFirstTraversal(spatial -> {
                    if (spatial instanceof Geometry) {
                        ((Geometry) spatial).setMaterial(material);
                        ((Geometry) spatial).setQueueBucket(RenderQueue.Bucket.Opaque);
                        ((Geometry) spatial).setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                        ((Geometry) spatial).getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Off);
                        ((Geometry) spatial).getMaterial().getAdditionalRenderState().setDepthTest(true);
                        ((Geometry) spatial).getMaterial().getAdditionalRenderState().setDepthWrite(true);
                        ((Geometry) spatial).getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                        ((Geometry) spatial).setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
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