package com.marcos.chess.networking;

import java.net.Socket;
import java.io.*;

public class ChessClient {
    private Socket socket;
    private final String host;
    private final int port;
    private final GameSession gameSession;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ChessClient(String host, int port, GameSession gameSession) {
        this.host = host;
        this.port = port;
        this.gameSession = gameSession;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            gameSession.setClientSocket(socket);
            gameSession.listenForOpponentMove(null);
            return true;
        } catch (IOException e) {
            cleanup();
            return false;
        }
    }

    public void disconnect() {
        cleanup();
    }

    private void cleanup() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}