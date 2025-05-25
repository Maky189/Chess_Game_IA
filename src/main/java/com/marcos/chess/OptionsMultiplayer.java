package com.marcos.chess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import org.lwjgl.Sys;

public class OptionsMultiplayer {
    private static final List<Multiplayer> activeGames = new ArrayList<>();
    private static final int PORT = 5000;
    private static final int DISCOVERY_PORT = 5001;
    private static DatagramSocket discoverySocket;

    static {
        try {
            discoverySocket = new DatagramSocket(null);
            discoverySocket.setReuseAddress(true);
            discoverySocket.setBroadcast(true);
            discoverySocket.bind(new InetSocketAddress(DISCOVERY_PORT));
            startDiscoveryListener();
        } catch (IOException e) {
            System.out.println("Error or whatever: " + e.getMessage());
        }
    }

    private static void startDiscoveryListener() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (true) {
                try {
                    discoverySocket.receive(packet);
                } catch (IOException e) {
                    return;
                }
            }
        }).start();
    }

    private static void broadcastGame(String hostName, String gameName, String status, String hostIP, int port) {
        try {
            String message = String.format("%s,%s,%s,%s,%d", hostName, gameName, status, hostIP, port);
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                // DO A BROADCAST using the broadcast ip, learned in Network class kkk
                InetAddress.getByName("255.255.255.255"),
                DISCOVERY_PORT
            );

            discoverySocket.setBroadcast(true);
            discoverySocket.send(packet);

        } catch (IOException e) {
            return;
        }
    }


    public static synchronized void addGame(String hostName, String gameName, String status, String hostIP, int port) {
        System.out.println("Adding game: " + gameName); // Debug print
        removeGame(gameName);
        Multiplayer game = new Multiplayer(hostName, gameName, status, hostIP, port);
        activeGames.add(game);
        broadcastGame(hostName, gameName, status, hostIP, port);
    }

    public static synchronized List<Multiplayer> getActiveGames() {
        return new ArrayList<>(activeGames);
    }

    public static synchronized void removeGame(String gameName) {
        activeGames.removeIf(game -> game.getGameName().equals(gameName));
    }

    public static synchronized void updateGameStatus(String gameName, String status) {
        for (Multiplayer game : activeGames) {
            if (game.getGameName().equals(gameName)) {
                game.setStatus(status);
                break;
            }
        }
    }

    // NOT WORKING SO FOR NOW I LEFT THIS PART
    // IF ANYONE WANT TO MAKE IT IN THE FUTURE, GO AHEAD, BECAUSE I AM DONE
    private static void refreshGamesList() {
        Platform.runLater(() -> {
            System.out.println("whatever");
        });
    }
}