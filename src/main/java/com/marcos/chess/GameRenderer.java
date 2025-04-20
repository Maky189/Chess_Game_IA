package com.marcos.chess;

import javafx.scene.Scene;

public interface GameRenderer {
    Scene createGameScene(Game game, int windowsWidth, int windowsHeight, boolean isMultiplayer);
    void initialize();
    void cleanup();
}