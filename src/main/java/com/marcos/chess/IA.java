package com.marcos.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class IA {
    private final Random random = new Random();

    public Random getRandom() {
        return random;
    }

    public Move makeMove(Game game, int player) {
        int[][] board = game.getBoard();
        List<Move> possibleMoves = getAllPossibleMoves(game, board, player);
        
        if (!possibleMoves.isEmpty()) {
            // Sort the moves by score
            List<ScoredMove> scoredMoves = new ArrayList<>();
            
            for (Move move : possibleMoves) {
                int score = evaluateMove(game, move);
                scoredMoves.add(new ScoredMove(move, score));
            }
            
            // descending sort by score
            Collections.sort(scoredMoves, Comparator.comparingInt(ScoredMove::score).reversed());
            
            // Chose one of the best moves
            int bestScore = scoredMoves.get(0).score();
            List<Move> bestMoves = new ArrayList<>();
            
            for (ScoredMove sm : scoredMoves) {
                if (sm.score() == bestScore) {
                    bestMoves.add(sm.move());
                } else {
                    break;
                }
            }
            
            // If found multiple best moves, pick one randomly
            return bestMoves.get(random.nextInt(bestMoves.size()));
        }
        return null;
    }

    private int evaluateMove(Game game, Move move) {
        int[][] board = game.getBoard();
        int capturedPiece = board[move.toX][move.toY];
        
        // Check the value of the captured piece
        // Make the number bigger for more value
        if (capturedPiece != 0) {
            return Math.abs(capturedPiece) * 100;
        }
        
        // If there is no capture, prefer developing the center
        int score = random.nextInt(10);
        
        // Bonus for controlling center squares (d4, d5, e4, e5)
        if ((move.toX == 3 || move.toX == 4) && (move.toY == 3 || move.toY == 4)) {
            score += 20;
        }
        
        // If is going forward, then is better
        if (game.getCurrentPlayer() == 1 && move.toX < move.fromX) {
            score += 10;
        } else if (game.getCurrentPlayer() == -1 && move.toX > move.fromX) {
            score += 10;
        }
        
        return score;
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

    private record ScoredMove(Move move, int score) {}

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