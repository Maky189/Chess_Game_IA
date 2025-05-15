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
        
        // Special evaluation for king moves
        if (Math.abs(piece) == 6) {  // King
            score += evaluateKingPosition(board, move, piece);
        } else {
            // Regular position scoring for other pieces
            score += POSITION_SCORES[move.toX][move.toY] * 10;
        }
        
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

    private int evaluateKingPosition(int[][] board, Move move, int piece) {
        int score = 0;
        boolean isEndgame = isEndgame(board);
        
        if (!isEndgame) {
            // Prefer corners and edges
            if (isCorner(move.toX, move.toY)) {
                score += 30;
            } else if (isEdge(move.toX, move.toY)) {
                score += 20;
            }
            
            // Penality for moving to center squares
            if (isCenterSquare(move.toX, move.toY)) {
                score -= 40;
            }
            
            // friendly pieces nearby = (protection) so IA get bonus kkk
            score += evaluateKingShelter(board, move, piece);
            
            // Bonus for castling
            if (isStartingPosition(move.fromX, move.fromY, piece)) {
                if (move.toY == 6 || move.toY == 2) {  // Castling moves
                    score += 50;
                }
            }
        } else {
            if (isEdge(move.toX, move.toY)) {
                score += 10;
            }
        }
        
        return score;
    }

    private boolean isCorner(int x, int y) {
        return (x == 0 || x == 7) && (y == 0 || y == 7);
    }

    private boolean isEdge(int x, int y) {
        return x == 0 || x == 7 || y == 0 || y == 7;
    }

    private boolean isCenterSquare(int x, int y) {
        return (x >= 2 && x <= 5) && (y >= 2 && y <= 5);
    }

    private int evaluateKingShelter(int[][] board, Move move, int piece) {
        int score = 0;
        int friendlySign = Integer.signum(piece);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int newX = move.toX + dx;
                int newY = move.toY + dy;
                
                if (isInBounds(newX, newY)) {
                    int squarePiece = board[newX][newY];
                    if (squarePiece != 0 && Integer.signum(squarePiece) == friendlySign) {
                        // Bonus for friendly pieces next to the king
                        score += 15;
                        
                        // Extra bonus for protecting pawns
                        if (Math.abs(squarePiece) == 1) {
                            score += 10;
                        }
                    }
                }
            }
        }
        
        return score;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
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
        int piece = board[move.fromX][move.fromY];
        int score = 0;
        
        // Check if we're in check and this is a defensive move
        if (isKingInCheck(board, game.getCurrentPlayer())) {
            score += evaluateCheckDefense(game, move, piece);
        }
        
        int pieceValue = getPieceValue(Math.abs(piece));
        
        // Create a temporary board to simulate move again not very perfomatiive but maybe there is no other way around
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
        
        // Add defense
        if (!pieceWouldBeThreatened) {
            // Check if this move helps defend friendly pieces
            score += evaluateDefense(game, tempBoard, move, piece);
        }
        
        return score;
    }

    private int evaluateDefense(Game game, int[][] board, Move move, int piece) {
        int score = 0;
        int friendlySign = Integer.signum(piece);
        
        // Check all friendly pieces
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == friendlySign) {
                    // If piece under attack
                    if (isSquareUnderAttack(board, i, j, -friendlySign)) {
                        // Count pieces defending this square
                        // Count pieces attacking this square
                        int defendersCount = countDefenders(board, i, j, friendlySign);
                        int attackersCount = countAttackers(board, i, j, -friendlySign);
                        
                        // If we're moving to a position where we can defend
                        if (canReachSquare(game, move.toX, move.toY, i, j)) {
                            int pieceValue = getPieceValue(Math.abs(board[i][j]));
                            
                            if (defendersCount <= attackersCount) {
                                score += (pieceValue / 100) * 30;
                            } else {
                                score += (pieceValue / 100) * 10;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    private int countDefenders(int[][] board, int x, int y, int friendlySign) {
        int count = 0;
        Game tempGame = new Game(8);
        tempGame.setBoard(board);
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == friendlySign &&
                    (i != x || j != y)) {
                    // Dont caunt the piece itself
                    List<int[]> moves = tempGame.calculatePossibleMoves(i, j);
                    for (int[] move : moves) {
                        if (move[0] == x && move[1] == y) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count;
    }

    private int countAttackers(int[][] board, int x, int y, int attackerSign) {
        int count = 0;
        Game tempGame = new Game(8);
        tempGame.setBoard(board);
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == attackerSign) {
                    List<int[]> moves = tempGame.calculatePossibleMoves(i, j);
                    for (int[] move : moves) {
                        if (move[0] == x && move[1] == y) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count;
    }

    private boolean canReachSquare(Game game, int fromX, int fromY, int toX, int toY) {
        // Check if a piece at (fromX, fromY) can defend a piece at (toX, toY)
        // This includes both direct protection and potential issues
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        
        // Can reach directly
        return dx <= 2 && dy <= 2;
    }

    private int evaluateCheckDefense(Game game, Move move, int piece) {
        int score = 0;
        int[][] board = game.getBoard();
        
        // Find the king's position
        int[] kingPos = findOurKing(board, game.getCurrentPlayer());
        if (kingPos == null) return 0;
        
        // If this is a king move then I dont want him
        if (Math.abs(piece) == 6) {
            score -= 200;
        } else {
            // Check if this move blocks the check
            int[][] tempBoard = new int[board.length][board.length];
            for (int i = 0; i < board.length; i++) {
                System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
            }
            
            // Make the move on temporary board
            tempBoard[move.toX][move.toY] = piece;
            tempBoard[move.fromX][move.fromY] = 0;
            
            // If this move blocks the check, give it a bonus proportional to piece value
            if (!isKingInCheck(tempBoard, game.getCurrentPlayer())) {
                int pieceValue = getPieceValue(Math.abs(piece));
                score += 1000 - pieceValue;  // Higher bonus for using cheaper pieces
                
                // Extra bonus for maintaining piece protection
                if (isSquareProtected(tempBoard, move.toX, move.toY, game.getCurrentPlayer())) {
                    score += 100;
                }
            }
        }
        
        return score;
    }

    private boolean isKingInCheck(int[][] board, int player) {
        // Find king position
        int[] kingPos = findOurKing(board, player);
        if (kingPos == null) return false;
        
        // Check if king is under attack
        return isSquareUnderAttack(board, kingPos[0], kingPos[1], -player);
    }

    private int[] findOurKing(int[][] board, int player) {
        int kingValue = player * 6;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == kingValue) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private boolean isSquareProtected(int[][] board, int x, int y, int player) {
        // Check if any friendly piece can move to this square
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == player && 
                    (i != x || j != y)) {  // Don't count the piece itself
                    Game tempGame = new Game(8);
                    tempGame.setBoard(board);
                    List<int[]> moves = tempGame.calculatePossibleMoves(i, j);
                    for (int[] move : moves) {
                        if (move[0] == x && move[1] == y) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSquareUnderAttack(int[][] board, int x, int y, int attackerSign) {
        int defendingPiece = board[x][y];
        
        // Get list of all attackers with their values
        List<Integer> attackers = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == attackerSign) {
                    Game tempGame = new Game(8);
                    tempGame.setBoard(board);
                    List<int[]> moves = tempGame.calculatePossibleMoves(i, j);
                    
                    // If this piece can attack the square
                    for (int[] move : moves) {
                        if (move[0] == x && move[1] == y) {
                            attackers.add(board[i][j]);
                            break;
                        }
                    }
                }
            }
        }
        
        if (attackers.isEmpty()) {
            return false;
        }
        
        // Sort attackers by value (prefer using lower value pieces)
        attackers.sort((a, b) -> getPieceValue(Math.abs(a)) - getPieceValue(Math.abs(b)));
        
        // If defending piece exists, only consider appropriate attackers
        if (defendingPiece != 0) {
            int defendingValue = getPieceValue(Math.abs(defendingPiece));
            // Find lowest value attacker that can take the piece
            for (int attacker : attackers) {
                int attackerValue = getPieceValue(Math.abs(attacker));
                // Only consider attack if attacker is of lower or equal value
                if (attackerValue <= defendingValue) {
                    return true;
                }
            }
            return false;
        }
        
        // For empty squares, any attack is valid
        return true;
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