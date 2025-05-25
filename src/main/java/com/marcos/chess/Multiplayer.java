package com.marcos.chess;

public class Multiplayer {
    private String hostName;
    private String gameName;
    private String status;
    private String hostIP;
    private int port;

    public Multiplayer(String hostName, String gameName, String status, String hostIP, int port) {
        this.hostName = hostName;
        this.gameName = gameName;
        this.status = status;
        this.hostIP = hostIP;
        this.port = port;
    }

    public String getHostName() { return hostName; }
    public String getGameName() { return gameName; }
    public String getStatus() { return status; }
    public String getHostIP() { return hostIP; }
    public int getPort() { return port; }
    public void setStatus(String status) { this.status = status; }
}