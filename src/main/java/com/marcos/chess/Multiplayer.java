package com.marcos.chess;

public class Multiplayer {
    private String hostName;
    private String gameName;
    private String status;

    public Multiplayer(String hostName, String gameName, String status) {
        this.hostName = hostName;
        this.gameName = gameName;
        this.status = status;
    }

    // Getters
    public String getHostName() { return hostName; }
    public String getGameName() { return gameName; }
    public String getStatus() { return status; }
}