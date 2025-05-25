package com.marcos.chess.networking;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChessServer {
    private final int port;
    private ServerSocket serverSocket;
    private final GameSession gameSession;
    private final ExecutorService pool;
    private boolean isRunning;

    public ChessServer(int port, GameSession gameSession) {
        this.port = port;
        this.gameSession = gameSession;
        this.pool = Executors.newFixedThreadPool(2);
        this.isRunning = false;
    }

    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on port " + port);

            pool.execute(() -> {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    gameSession.setClientSocket(clientSocket);
                    gameSession.listenForOpponentMove(null);
                } catch (IOException e) {
                    if (isRunning) {
                        return;
                    }
                }
            });
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void stop() {
        isRunning = false;
        pool.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            return;
        }
    }
}