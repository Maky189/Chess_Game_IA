package com.marcos.chess;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.scene.Spatial;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class Test3D extends SimpleApplication {
    private Node pieceNode;
    private float modelScale = 0.5f;

    public static void launch() {
        Test3D app = new Test3D();
        AppSettings settings = new AppSettings(true);
        settings.setFullscreen(true);
        
        java.awt.DisplayMode displayMode = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode();
            
        settings.setResolution(displayMode.getWidth(), displayMode.getHeight());
        settings.setFrequency(displayMode.getRefreshRate());
        settings.setGammaCorrection(true);
        settings.setSamples(4);
        
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(1f, 0.5f, 0f, 1f));
        flyCam.setMoveSpeed(10f);
        cam.setLocation(new Vector3f(0, 3, 5));
        cam.lookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);

        setupLighting();

        pieceNode = loadPawnModel();
        if (pieceNode != null) {
            pieceNode.setLocalScale(modelScale);
            pieceNode.setLocalTranslation(0, 0.5f, 0);
            rootNode.attachChild(pieceNode);
        } else {
            return;
        }

        setupInput();
        inputManager.setCursorVisible(true);
    }

    private void setupLighting() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(sun);

        com.jme3.light.AmbientLight ambient = new com.jme3.light.AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);

        DirectionalLight fill = new DirectionalLight();
        fill.setDirection(new Vector3f(1f, -0.5f, 0.5f).normalizeLocal());
        fill.setColor(ColorRGBA.White.mult(0.7f));
        rootNode.addLight(fill);
    }

    private Node loadPawnModel() {
        try {
            Node pawnNode = new Node("pawn");
            Spatial model = assetManager.loadModel("assets/3Dpieces/pawn_white.j3o");

            Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            material.setTexture("DiffuseMap", assetManager.loadTexture("textures/piece_white.png"));
            material.setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
            material.setColor("Diffuse", new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
            material.setColor("Specular", new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
            material.setFloat("Shininess", 64f);
            material.setBoolean("UseMaterialColors", true);
            material.setColor("GlowColor", new ColorRGBA(0.05f, 0.05f, 0.05f, 1.0f));
            material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            material.getAdditionalRenderState().setDepthTest(true);
            material.getAdditionalRenderState().setDepthWrite(true);
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Off);

            model.setMaterial(material);
            pawnNode.attachChild(model);
            
            
            return pawnNode;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupInput() {
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener((ActionListener) (name, pressed, tpf) -> {
            if (name.equals("Exit") && !pressed) {
                stop();
            }
        }, "Exit");

        // Enable fly cam for testing
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(10f);
    }
}