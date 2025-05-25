package com.marcos.chess.networking;

import com.marcos.chess.DragHandler;
import com.marcos.chess.Game;
import com.marcos.chess.MainGame;

import javafx.application.Platform;

import java.net.Socket;
import java.io.*;

public class GameSession {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Game game;
    private boolean isHost;
    private String gameName;
    private DragHandler dragHandler;

    public GameSession(boolean isHost) {
        this.isHost = isHost;
        this.game = MainGame.getGameInstance(8);
    }

    public void setGameName(String name) {
        this.gameName = name;
    }

    public String getGameName() {
        return gameName;
    }

    public void setClientSocket(Socket socket) {
        try {
            this.clientSocket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeMove(int fromX, int fromY, int toX, int toY) {
        try {
            if (out != null) {
                Move move = new Move(fromX, fromY, toX, toY);
                out.writeObject(move);
                out.flush();
            }
        } catch (IOException e) {
            return;
        }
    }

    public void listenForOpponentMove(DragHandler handler) {
        this.dragHandler = handler;
        new Thread(() -> {
            try {
                while (clientSocket != null && !clientSocket.isClosed() && in != null) {
                    Move move = (Move) in.readObject();
                    if (move != null) {
                        Platform.runLater(() -> {
                            game.getBoard()[move.toX][move.toY] = game.getBoard()[move.fromX][move.fromY];
                            game.getBoard()[move.fromX][move.fromY] = 0;
                            game.updateLastMove(move.fromX, move.fromY, move.toX, move.toY);
                            game.switchPlayer();
                            if (handler != null) {
                                handler.redrawBoard();
                            }
                        });
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isHost() {
        return isHost;
    }

    public void cleanup() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStreams(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
    }
}

class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int fromX, fromY, toX, toY;

    public Move(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }
}