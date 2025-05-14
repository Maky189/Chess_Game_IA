package com.marcos.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class IA {
    private final Random random = new Random();
    
    // map of points for each position on the board
    // The ideia is prioritize development and center control
    // while also considering the value of the piece being moved
    private static final int[][] POSITION_SCORES = {
        {1,  1,  1,  1,  1,  1,  1,  1},
        {1,  5, 10, 10, 10, 10,  5,  1},
        {1, 10, 20, 30, 30, 20, 10,  1},
        {1, 10, 30, 40, 40, 30, 10,  1},
        {1, 10, 30, 40, 40, 30, 10,  1},
        {1, 10, 20, 30, 30, 20, 10,  1},
        {1,  5, 10, 10, 10, 10,  5,  1},
        {1,  1,  1,  1,  1,  1,  1,  1}
    };

    public Move makeMove(Game game, int player) {
        int[][] board = game.getBoard();
        List<Move> possibleMoves = getAllPossibleMoves(game, board, player);
        
        if (!possibleMoves.isEmpty()) {
            List<ScoredMove> scoredMoves = new ArrayList<>();
            
            for (Move move : possibleMoves) {
                int score = evaluateMove(game, move);
                // Only moves that has a good score and avoid pointless moves
                // that don't change the position of the pieces
                if (score > 0) {
                    scoredMoves.add(new ScoredMove(move, score));
                }
            }
            
            // If no positive moves found, evaluate position-only moves
            if (scoredMoves.isEmpty()) {
                for (Move move : possibleMoves) {
                    int positionScore = evaluatePosition(game, move);
                    if (positionScore > 0) {
                        scoredMoves.add(new ScoredMove(move, positionScore));
                    }
                }
            }
            
            if (!scoredMoves.isEmpty()) {
                // sort by descending score
                // This will sort the moves by score, and in case of a tie, it will sort by the move itself
                Collections.sort(scoredMoves, Comparator.comparingInt(ScoredMove::score).reversed());
                
                // Get all moves with the best score
                int bestScore = scoredMoves.get(0).score();
                List<Move> bestMoves = new ArrayList<>();
                
                for (ScoredMove sm : scoredMoves) {
                    if (sm.score() == bestScore) {
                        bestMoves.add(sm.move());
                    } else {
                        break;
                    }
                }
                
                // Choose  the best move randomly from the best moves
                return bestMoves.get(random.nextInt(bestMoves.size()));
            }
        }
        return null;
    }

    private int evaluateMove(Game game, Move move) {
        int[][] board = game.getBoard();
        int score = 0;
        
        // Evaluate capture value
        int capturedPiece = board[move.toX][move.toY];
        if (capturedPiece != 0) {
            score += getPieceValue(Math.abs(capturedPiece)) * 100;
        }
        
        // Add position score
        score += evaluatePosition(game, move);
        
        return score;
    }

    private int evaluatePosition(Game game, Move move) {
        int[][] board = game.getBoard();
        int piece = board[move.fromX][move.fromY];
        int score = 0;
        
        // Position score for the target square
        score += POSITION_SCORES[move.toX][move.toY] * 10;
        
        // Development bonus (moving pieces from their starting positions)
        if (isStartingPosition(move.fromX, move.fromY, piece)) {
            score += 15;
        }
        
        // Center control bonus
        if ((move.toX >= 2 && move.toX <= 5) && (move.toY >= 2 && move.toY <= 5)) {
            score += 20;
        }
        
        // Penalize moving to already controlled squares
        if (isSquareControlled(board, move.toX, move.toY, game.getCurrentPlayer())) {
            score -= 10;
        }
        
        return score;
    }

    private boolean isStartingPosition(int x, int y, int piece) {
        if (piece > 0) {
            return (piece == 1 && x == 6) || x == 7;
        } else {
            return (piece == -1 && x == 1) || x == 0;
        }
    }

    private boolean isSquareControlled(int[][] board, int x, int y, int player) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == player) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getPieceValue(int piece) {
        return switch (piece) {
            case 1 -> 100;   // Pawn
            case 2 -> 500;   // Rook
            case 3 -> 300;   // Knight
            case 4 -> 300;   // Bishop
            case 5 -> 900;   // Queen
            case 6 -> 10000; // King
            default -> 0;
        };
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