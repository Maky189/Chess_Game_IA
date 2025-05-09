package com.marcos.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class IA {
    private final Random random = new Random();

    public Random getRandom() {
        return random;
    }

    public Move makeMove(Game game, int player) {
        int[][] board = game.getBoard();
        List<Move> possibleMoves = getAllPossibleMoves(game, board, player);
        if (!possibleMoves.isEmpty()) {
            Move selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            
            // Don't update the board here anymore Marcos
            return selectedMove;
        }
        return null;
    }

    public List<Move> getAllPossibleMoves(Game game, int[][] board, int player) {
        List<Move> allMoves = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == player) {
                    List<int[]> pieceMoves = game.calculatePossibleMoves(i, j);

                    for (int[] pieceMove : pieceMoves) {
                        allMoves.add(new Move(i, j, pieceMove[0], pieceMove[1]));
                    }
                }
            }
        }
        return allMoves;
    }

    public static class Move {
        final int fromX, fromY, toX, toY;

        Move(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}