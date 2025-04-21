package com.marcos.chess;

import javafx.scene.Scene;

public interface Renderer {
    Scene createGameScene(Game game, int windowsWidth, int windowsHeight, boolean isMultiplayer);
    void initialize();
    void cleanup();
}