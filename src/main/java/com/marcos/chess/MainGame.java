package com.marcos.chess;

public class MainGame {
    private static Game sharedGameInstance;

    public static Game getGameInstance(int size) {
        if (sharedGameInstance == null) {
            sharedGameInstance = new Game(size);
        }
        return sharedGameInstance;
    }

    public static void resetGameInstance(int size) {
        sharedGameInstance = new Game(size);
    }
}