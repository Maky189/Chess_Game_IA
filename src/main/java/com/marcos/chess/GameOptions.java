package com.marcos.chess;

import java.io.*;
import java.nio.file.*;

public class GameOptions {
    private static final String OPTIONS_FILE = "game_options.dat";
    private static GameOptions instance;
    
    private boolean is3DMode;
    // Add other options here as needed

    private GameOptions() {
        loadOptions();
    }

    public static GameOptions getInstance() {
        if (instance == null) {
            instance = new GameOptions();
        }
        return instance;
    }

    public boolean is3DModeEnabled() {
        return is3DMode;
    }

    public void set3DMode(boolean enabled) {
        this.is3DMode = enabled;
        saveOptions();
    }

    private void loadOptions() {
        try {
            if (Files.exists(Paths.get(OPTIONS_FILE))) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(OPTIONS_FILE))) {
                    OptionsData data = (OptionsData) ois.readObject();
                    this.is3DMode = data.is3DMode;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // If there's an error, use defaults
            this.is3DMode = false;
        }
    }

    private void saveOptions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OPTIONS_FILE))) {
            OptionsData data = new OptionsData(is3DMode);
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class OptionsData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean is3DMode;

        public OptionsData(boolean is3DMode) {
            this.is3DMode = is3DMode;
        }
    }
}