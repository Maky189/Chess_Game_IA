package com.marcos.chess;

public class SaveGame {
    private String name;
    private String date;
    private String mode;

    public SaveGame(String name, String date, String mode) {
        this.name = name;
        this.date = date;
        this.mode = mode;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}