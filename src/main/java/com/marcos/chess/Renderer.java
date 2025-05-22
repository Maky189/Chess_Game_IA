package com.marcos.chess;

import javafx.scene.Scene;

public interface Renderer {
    Scene createGameScene(int windowsWidth, int windowsHeight, boolean isMultiplayer);
    void initialize();
    void clean();
}