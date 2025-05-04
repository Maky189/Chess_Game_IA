package com.marcos.chess;

import java.io.*;
import java.nio.file.*;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.ArrayList;

public class GameSaver {
    private static final String SAVE_DIR = "saved_games";

    public static void saveGame(String name, Game game, String mode) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            String filename = name.replaceAll("[^a-zA-Z0-9]", "_") + ".chess";
            Path savePath = Paths.get(SAVE_DIR, filename);
            
            try (ObjectOutputStream objects = new ObjectOutputStream(new FileOutputStream(savePath.toFile()))) {
                int[][] boardCopy = new int[game.getBoard().length][];
                for (int i = 0; i < game.getBoard().length; i++) {
                    boardCopy[i] = game.getBoard()[i].clone();
                }
                
                GameState state = new GameState(
                    boardCopy,
                    mode,
                    game.getCurrentPlayer(),
                    game.getLastMoveFromX(),
                    game.getLastMoveFromY(),
                    game.getLastMoveToX(),
                    game.getLastMoveToY(),
                    game.getLastMovePiece(),
                    game.getEnPassantTargetX(),
                    game.getEnPassantTargetY()
                );
                objects.writeObject(state);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Game loadGame(String name) {
        try {
            String filename = name.replaceAll("[^a-zA-Z0-9]", "_") + ".chess";
            Path savePath = Paths.get(SAVE_DIR, filename);
            
            try (ObjectInputStream objects = new ObjectInputStream(new FileInputStream(savePath.toFile()))) {
                GameState state = (GameState) objects.readObject();
                Game game = GameFactory.getGameInstance(8);
                game.setBoard(state.board.clone());
                game.setCurrentPlayer(state.currentPlayer);
                game.updateLastMove(
                    state.lastMoveFromX,
                    state.lastMoveFromY,
                    state.lastMoveToX,
                    state.lastMoveToY
                );
                return game;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SaveGame> getSavedGames() {
        List<SaveGame> savedGames = new ArrayList<>();
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            
            Files.list(Paths.get(SAVE_DIR)).filter(path -> path.toString().endsWith(".chess")).forEach(path -> {
                     try {
                         BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                         String name = path.getFileName().toString().replace(".chess", "");
                         String date = attrs.creationTime().toString().split("\\.")[0].replace("T", " ");
                         savedGames.add(new SaveGame(name, date, "White"));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedGames;
    }

    public static void deleteProfile(String name) {
        try {
            String filename = name.replaceAll("[^a-zA-Z0-9]", "_") + ".chess";
            Path savePath = Paths.get(SAVE_DIR, filename);
            Files.deleteIfExists(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    final int[][] board;
    final String mode;
    final int currentPlayer;
    final int lastMoveFromX;
    final int lastMoveFromY;
    final int lastMoveToX;
    final int lastMoveToY;
    final int lastMovePiece;
    final int enPassantTargetX;
    final int enPassantTargetY;

    public GameState(int[][] board, String mode, int currentPlayer, int lastMoveFromX, int lastMoveFromY, int lastMoveToX, int lastMoveToY, int lastMovePiece, int enPassantTargetX, int enPassantTargetY) {
        this.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            this.board[i] = board[i].clone();
        }
        this.mode = mode;
        this.currentPlayer = currentPlayer;
        this.lastMoveFromX = lastMoveFromX;
        this.lastMoveFromY = lastMoveFromY;
        this.lastMoveToX = lastMoveToX;
        this.lastMoveToY = lastMoveToY;
        this.lastMovePiece = lastMovePiece;
        this.enPassantTargetX = enPassantTargetX;
        this.enPassantTargetY = enPassantTargetY;
    }
}