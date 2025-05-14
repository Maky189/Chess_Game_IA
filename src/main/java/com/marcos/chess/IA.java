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
                    int positionScore = analizePosition(game, move);
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
                
                // Choose the best move randomly from the best moves
                return bestMoves.get(random.nextInt(bestMoves.size()));
            }
        }
        return null;
    }

    private int analizePosition(Game game, Move move) {
        int[][] board = game.getBoard();
        int piece = board[move.fromX][move.fromY];
        int score = 0;
        
        score += POSITION_SCORES[move.toX][move.toY] * 10;
        
        // Development bonus (moving pieces from starting positions)
        if (isStartingPosition(move.fromX, move.fromY, piece)) {
            score += 15;
        }
        
        // make the rooks smarter
        // Rooks are more valuable when they control open lines
        if (Math.abs(piece) == 2) {  // Rook
            score += verifyRookPosition(board, move, piece);
        }
        
        // Center control bonus 
        if ((move.toX >= 2 && move.toX <= 5) && (move.toY >= 2 && move.toY <= 5)) {
            score += 15;
        }
        
        // Pawn advancement increases as pawn moves forward
        if (Math.abs(piece) == 1) {
            int promotionDirection = (piece > 0) ? -1 : 1;
            int rankProgress = (piece > 0) ? (6 - move.toX) : (move.toX - 1);
            // bonus: more points as pawn advances
            score += rankProgress * 5;
            // Extra if pass the middle of the board
            if ((piece > 0 && move.toX < 3) || (piece < 0 && move.toX > 4)) {
                score += 20;
            }
        }
        
        // Avoid moving into squares controlled by the opponent
        // This is a simple heuristic to avoid moving into squares that are controlled by the opponent
        if (isSquareControlled(board, move.toX, move.toY, game.getCurrentPlayer())) {
            score -= 5;
        }
        
        return score;
    }

    private int verifyRookPosition(int[][] board, Move move, int piece) {
        int score = 0;
        boolean isEndgame = isEndgame(board);
        
        // Count empty squares in the rook line of attack
        int emptySquaresInFile = countEmptySquaresInLine(board, move.toY);
        
        // Count empty squares
        int emptySquaresInRank = countEmptySquaresInRank(board, move.toX);
        
        // Bonus for controlling open lines
        if (emptySquaresInFile >= 5) {
            score += 25;
            
            // Extra bonus if it's the only rook controlling this file
            if (!isLineControlledByEnemyRook(board, move.toY, piece)) {
                score += 15;
            }
        }
        
        // Endgame
        if (isEndgame) {
            // Bonus for being on the 7th rank
            int seventhRank = (piece > 0) ? 1 : 6;
            if (move.toX == seventhRank) {
                score += 30;
            }
            
            // Bonus for having many squares to move to
            int mobilityScore = (emptySquaresInFile + emptySquaresInRank) * 3;
            score += mobilityScore;
            
            // Bonus for being closer to enemy king in endgame
            int[] enemyKingPos = findEnemyKing(board, piece);
            if (enemyKingPos != null) {
                int distance = Math.abs(move.toX - enemyKingPos[0]) + Math.abs(move.toY - enemyKingPos[1]);
                score += (14 - distance) * 2;
            }
        }
        
        return score;
    }

    private boolean isEndgame(int[][] board) {
        int pieceCount = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                 // Count non-king pieces
                if (board[i][j] != 0 && Math.abs(board[i][j]) != 6) { 
                    pieceCount++;
                }
            }
        }
         // Consider it endgame if 10 or fewer pieces remain
        return pieceCount <= 10; 
    }

    private int countEmptySquaresInLine(int[][] board, int file) {
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            if (board[i][file] == 0) {
                count++;
            }
        }
        return count;
    }

    private int countEmptySquaresInRank(int[][] board, int rank) {
        int count = 0;
        for (int j = 0; j < board[rank].length; j++) {
            if (board[rank][j] == 0) {
                count++;
            }
        }
        return count;
    }

    private boolean isLineControlledByEnemyRook(int[][] board, int file, int piece) {
        int enemyRook = (piece > 0) ? -2 : 2;
        for (int i = 0; i < board.length; i++) {
            if (board[i][file] == enemyRook) {
                return true;
            }
        }
        return false;
    }

    private int[] findEnemyKing(int[][] board, int piece) {
        int enemyKing = (piece > 0) ? -6 : 6;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == enemyKing) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private int evaluateMove(Game game, Move move) {
        int[][] board = game.getBoard();
        int score = 0;
        int piece = board[move.fromX][move.fromY];
        int pieceValue = getPieceValue(Math.abs(piece));
        
        // Create a temporary board to simulate the move
        int[][] tempBoard = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
        }
        
        // Make the move on it
        tempBoard[move.toX][move.toY] = piece;
        tempBoard[move.fromX][move.fromY] = 0;
        
        // Check if any piece would be threatened after this move
        boolean pieceWouldBeThreatened = false;
        if (game.getCurrentPlayer() == 1) {
            // Find all the pieces that would be threatened
            for (int i = 0; i < tempBoard.length; i++) {
                for (int j = 0; j < tempBoard[i].length; j++) {
                    if (tempBoard[i][j] != 0 && Integer.signum(tempBoard[i][j]) == 1) {
                        if (isSquareUnderAttack(tempBoard, i, j, -1)) {
                            // If the threatened piece is more valuable than what we might capture
                            if (getPieceValue(Math.abs(tempBoard[i][j])) > pieceValue) {
                                pieceWouldBeThreatened = true;
                                break;
                            }
                        }
                    }
                }
                if (pieceWouldBeThreatened) break;
            }
        }
        
        // Analizing capture value
        int capturedPiece = board[move.toX][move.toY];
        if (capturedPiece != 0) {
            int capturedValue = getPieceValue(Math.abs(capturedPiece));
            
            // Check if the piece could be recaptured
            boolean couldBeRecaptured = isSquareUnderAttack(tempBoard, move.toX, move.toY, -Integer.signum(piece));
            
            if (couldBeRecaptured || pieceWouldBeThreatened) {
                // If the piece is less valuable than the captured piece,then make the trade
                if (capturedValue > pieceValue) {
                    score += (capturedValue - pieceValue) * 100;
                } else {
                    // Avoid
                    return -1;
                }
            } else {
                // Safe capture, then full value
                score += capturedValue * 100;
            }
        } else {
            if (pieceWouldBeThreatened) {
                // Penalized for moving to a position that would leave pieces threatened
                return -1;
            }
            
            // Check if the destination square is threatened
            boolean isSquareThreatened = isSquareUnderAttack(board, move.toX, move.toY, -Integer.signum(piece));
            
            if (isSquareThreatened) {
                // Is worng moving into a threatened square
                if (Math.abs(piece) == 6) {
                    return -1;
                }
                score -= pieceValue * 50;
            }
            
            // Add position score only if the move is somewhat safe
            if (!isSquareThreatened && !pieceWouldBeThreatened || score > 0) {
                score += analizePosition(game, move);
            }
        }
        
        return score;
    }

    private boolean isSquareUnderAttack(int[][] board, int x, int y, int attackerSign) {
        // Check all opponent pieces that could capture this square
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == attackerSign) {
                    // Create a temporary game state to calculate moves
                    Game tempGame = new Game(8);
                    tempGame.setBoard(board);
                    List<int[]> attackerMoves = tempGame.calculatePossibleMoves(i, j);
                    
                    // Check if any move can capture our piece
                    for (int[] move : attackerMoves) {
                        if (move[0] == x && move[1] == y) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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