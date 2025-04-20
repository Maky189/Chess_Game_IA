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
        chessApp = new ChessGame();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(windowsWidth);
        settings.setHeight(windowsHeight);
        settings.setSamples(4);
        settings.setFrameRate(60);
        settings.setGammaCorrection(false);
        chessApp.setSettings(settings);
        chessApp.start(); // Standalone window
        // You can return null or a dummy scene, since JME handles its own window
        return null;
    }

    private class ChessGame extends SimpleApplication {
        @Override
        public void simpleInitApp() {
            System.out.println("JME3 simpleInitApp called!");

            cam.setLocation(new Vector3f(0, 5, 10));
            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-1, -2, -3).normalizeLocal());
            rootNode.addLight(sun);

            // Attach the chessboard
            rootNode.attachChild(createChessBoard());
        }

        private Node createChessBoard() {
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
                    Box square = new Box(squareSize/2, 0.1f, squareSize/2);
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
            
            return boardNode;
        }
    }

    @Override
    public void cleanup() {
        if (chessApp != null) {
            chessApp.stop();
        }
    }
}