package com.marcos.chess;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class OptionsMultiplayer {
    private static final int PORT = 5000;
    private List<Multiplayer> activeGames;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean isHost;
    private Game game;

    public OptionsMultiplayer() {
        this.activeGames = new ArrayList<>();
    }

    public void hostGame(String gameName) {
        isHost = true;
        try {
            serverSocket = new ServerSocket(PORT);
            // Create new multiplayer game instance
            Multiplayer newGame = new Multiplayer(
                System.getProperty("user.name"), 
                gameName,
                "Waiting for player"
            );
            activeGames.add(newGame);
            // Listen for connections
            listenForPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForPlayer() {
        new Thread(() -> {
            try {
                clientSocket = serverSocket.accept();
                // When player connects, start the game
                startGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startGame() {
        MainGame.resetGameInstance(8);
        game = MainGame.getGameInstance(8);
        // Game will be initialized when both players are ready
    }

    public List<Multiplayer> getActiveGames() {
        return activeGames;
    }
}