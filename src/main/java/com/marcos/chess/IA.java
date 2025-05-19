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
        {0,  5, 10, 10, 10, 10,  5,  0},
        {1, 10, 20, 30, 30, 20, 10,  1},
        {1, 10, 30, 40, 40, 30, 10,  1},
        {1, 10, 30, 40, 40, 30, 10,  1},
        {1, 10, 20, 30, 30, 20, 10,  1},
        {0,  5, 10, 10, 10, 10,  5,  0},
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
            
            // If no positive moves were found, make analise of the position to moves
            if (scoredMoves.isEmpty()) {
                for (Move move : possibleMoves) {
                    int positionScore = analizePosition(game, move);
                    if (positionScore > 0) {
                        scoredMoves.add(new ScoredMove(move, positionScore));
                    }
                }
            }
            
            if (!scoredMoves.isEmpty()) {
                // sort by descending score to put the best moves first
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
                
                // Choose the best move random from the best moves
                return bestMoves.get(random.nextInt(bestMoves.size()));
            }
        }
        return null;
    }

    private int analizePosition(Game game, Move move) {
        int[][] board = game.getBoard();
        int piece = board[move.fromX][move.fromY];
        int score = 0;
        
        // Special analisis for knights and rooks
        if (Math.abs(piece) == 3) {
            score += evaluateKnightPosition(board, move, piece);
        } else if (Math.abs(piece) == 2) {
            score += evaluateRookPosition(board, move, piece);
        } else if (Math.abs(piece) == 6) {
            score += evaluateKingPosition(board, move, piece);
        } else {
            score += POSITION_SCORES[move.toX][move.toY] * 10;
        }
        
        // Development bonus (increased for knights and rooks)
        if (isStartingPosition(move.fromX, move.fromY, piece)) {
            // First make knights develop
            if (Math.abs(piece) == 3) {
                score += 40;
            } else if (Math.abs(piece) == 2) {
                // Rook does not need much, since they are not the most active pieces in the begining
                score += 20;
            } else {
                // For other pieces
                score += 15;
            }
        }
        
        return score;
    }

    private int evaluateKnightPosition(int[][] board, Move move, int piece) {
        int score = 0;
        boolean isEarlyGame = !isEndgame(board);
        
        // Early game development bonus
        if (isEarlyGame && isStartingPosition(move.fromX, move.fromY, piece)) {
            // Strong bonus to make knights develop too, added to the 40 before
            score += 60;
            
            // Extra bonus for developing towards center
            if ((move.toX >= 2 && move.toX <= 5) && (move.toY >= 2 && move.toY <= 5)) {
                score += 40;
            }
        }
        
        // Bonus for central squares
        if ((move.toX >= 2 && move.toX <= 5) && (move.toY >= 2 && move.toY <= 5)) {
            // Gain much higher for center control
            score += 30;
        }
        
        // Check if the knight is under attack in current position
        boolean isCurrentlyThreatened = isSquareUnderAttack(board, move.fromX, move.fromY, -Integer.signum(piece));
        if (isCurrentlyThreatened) {
            // Make him move to safety
            if (!isSquareUnderAttack(board, move.toX, move.toY, -Integer.signum(piece))) {
                score += 50;
            }
        }
        
        // Bonus for bettter positions (protected by friendly pawn)
        int pawnDirection = (piece > 0) ? 1 : -1;
        if (isInBounds(move.toX + pawnDirection, move.toY - 1)) {
            if (board[move.toX + pawnDirection][move.toY - 1] == piece / Math.abs(piece)) {
                score += 25;
            }
        }
        if (isInBounds(move.toX + pawnDirection, move.toY + 1)) {
            if (board[move.toX + pawnDirection][move.toY + 1] == piece / Math.abs(piece)) {
                score += 25;
            }
        }
        
        // Count attacking squares
        int attackingSquares = countAttackers(board, move.toX, move.toY, piece);
        score += attackingSquares * 15;
        
        return score;
    }

    private int evaluateRookPosition(int[][] board, Move move, int piece) {
        int score = 0;
        boolean isEndgame = isEndgame(board);
        
        // Count empty squares in the rook's lines
        int emptySquaresInLine = countEmptySquaresInLine(board, move.toY);
        
        // Early game: prioritize safety and development
        if (!isEndgame) {
            // Check for diagonal threats to the rook
            boolean isDiagonallyThreatened = false;
            int[][] diagonalDirections = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
            
            for (int[] dir : diagonalDirections) {
                int x = move.toX + dir[0];
                int y = move.toY + dir[1];
                while (isInBounds(x, y)) {
                    int pieceAtSquare = board[x][y];
                    if (pieceAtSquare != 0) {
                        if (Integer.signum(pieceAtSquare) != Integer.signum(piece) && 
                            (Math.abs(pieceAtSquare) == 4 || Math.abs(pieceAtSquare) == 5)) {
                            isDiagonallyThreatened = true;
                        }
                        break;
                    }
                    x += dir[0];
                    y += dir[1];
                }
            }
            
            //penalize moving to diagonally threatened squares
            if (isDiagonallyThreatened) {
                score -= 80;
            }
            
            // More points for staying protected
            if (isSquareProtected(board, move.toX, move.toY, Integer.signum(piece))) {
                score += 30;
            }
            
            // More points for moving out
            if (move.fromX == (piece > 0 ? 7 : 0) && 
                !isSquareUnderAttack(board, move.toX, move.toY, -Integer.signum(piece)) &&
                isSquareProtected(board, move.toX, move.toY, Integer.signum(piece))) {
                score += 25;
            }
            
            // Points for controlling open lines
            if (emptySquaresInLine >= 5) {
                score += 25;
                if (!isLineControlledByEnemyRook(board, move.toY, piece)) {
                    // Extra
                    score += 20;
                }
            }
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
                if (move.toY == 6 || move.toY == 2) {
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
                        // POINTS for friendly pieces next to the king
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
        
        // more points for controlling open lines
        if (emptySquaresInFile >= 5) {
            score += 25;
            
            // Extra if it's the only rook controlling this line
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
                 // Count other pieces
                if (board[i][j] != 0 && Math.abs(board[i][j]) != 6) { 
                    pieceCount++;
                }
            }
        }
         // Consider endgame if 10 or fewer pieces remain
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
        int pieceValue = getPieceValue(Math.abs(piece));
        
        // Check if in check and this is a defensive move
        if (isKingInCheck(board, game.getCurrentPlayer())) {
            score += evaluateCheckDefense(game, move, piece);
        }
        
        // Create temporary board to make the move
        int[][] tempBoard = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
        }
        tempBoard[move.toX][move.toY] = piece;
        tempBoard[move.fromX][move.fromY] = 0;

        // Check if moving next to enemy king
        int[] enemyKingPos = findEnemyKing(board, piece);
        if (enemyKingPos != null) {
            boolean isNextToKing = Math.abs(move.toX - enemyKingPos[0]) <= 1 && 
                                  Math.abs(move.toY - enemyKingPos[1]) <= 1;
            
            if (isNextToKing) {
                // Only allow if square is well protected
                int defendersCount = countDefenders(tempBoard, move.toX, move.toY, Integer.signum(piece));
                if (defendersCount == 0) {
                    return -1000;
                }
            }
        }

        // Check if piece is currently threatened
        boolean isCurrentlyThreatened = isSquareUnderAttack(board, move.fromX, move.fromY, -Integer.signum(piece));
        
        // move highervalue pieces to safety
        if (isCurrentlyThreatened && Math.abs(piece) >= 4) {
            // Money to GOOOO OUTTT
            score += pieceValue * 3;
            
            // MOREEEE MONEEYYYYYY
            if (!isSquareUnderAttack(tempBoard, move.toX, move.toY, -Integer.signum(piece))) {
                score += pieceValue * 5;
            } else {
                int capturedPiece = board[move.toX][move.toY];
                if (capturedPiece == 0 || getPieceValue(Math.abs(capturedPiece)) <= pieceValue) {
                    return -1;
                }
            }
        }

        // Prevent pointless rook moves
        if (Math.abs(piece) == 2) {
            // Only move rook if:
            // 1. It's threatened
            // 2. It can capture something
            // 3. It moves to control an important file
            // 4. It's part of development in early game
            boolean isCapture = board[move.toX][move.toY] != 0;
            boolean controlsOpenFile = countEmptySquaresInLine(tempBoard, move.toY) >= 5;
            boolean isDevelopment = isStartingPosition(move.fromX, move.fromY, piece) && 
                                  isSquareProtected(tempBoard, move.toX, move.toY, Integer.signum(piece));
            
            if (!isCurrentlyThreatened && !isCapture && !controlsOpenFile && !isDevelopment) {
                return -1; // Prevent pointless rook moves
            }
        }

        // Evaluate captures
        int capturedPiece = board[move.toX][move.toY];
        if (capturedPiece != 0) {
            int capturedValue = getPieceValue(Math.abs(capturedPiece));
            boolean couldBeRecaptured = isSquareUnderAttack(tempBoard, move.toX, move.toY, -Integer.signum(piece));
            
            if (couldBeRecaptured) {
                // Only make capture if favorable
                if (capturedValue > pieceValue) {
                    score += (capturedValue - pieceValue) * 100;
                } else {
                    return -1;
                }
            } else {
                score += capturedValue * 100;
            }
        }

        // Add positional evaluation if move is safe
        if (!isSquareUnderAttack(tempBoard, move.toX, move.toY, -Integer.signum(piece)) || score > 0) {
            score += analizePosition(game, move);
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
        
        // Find enemy king position
        int[] enemyKingPos = findEnemyKing(board, piece);
        if (enemyKingPos == null) return 0;
        
        // Get all king's escape squares before our move
        List<int[]> currentEscapeSquares = getKingEscapeSquares(board, enemyKingPos[0], enemyKingPos[1]);
        
        // Simulate the move
        int[][] tempBoard = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
        }
        tempBoard[move.toX][move.toY] = piece;
        tempBoard[move.fromX][move.fromY] = 0;
        
        // Get escape squares after our move
        List<int[]> newEscapeSquares = getKingEscapeSquares(tempBoard, enemyKingPos[0], enemyKingPos[1]);
        
        // Check if we're moving to a square next to the king
        boolean isNextToKing = Math.abs(move.toX - enemyKingPos[0]) <= 1 && 
                              Math.abs(move.toY - enemyKingPos[1]) <= 1;
        
        if (isNextToKing) {
            // Only move next to king if the square is well protected
            int defendersCount = countDefenders(tempBoard, move.toX, move.toY, Integer.signum(piece));
            if (defendersCount == 0) {
                //DO NOT GO NEXT TO THE KING OR YOU LOSE POINTS IDIOT
                return -1000;
            }
        }
        
        // Bonus for reducing king's escape squares
        if (newEscapeSquares.size() < currentEscapeSquares.size()) {
            score += (currentEscapeSquares.size() - newEscapeSquares.size()) * 150;
        }
        
        // Bonus for controlling escape squares
        for (int[] escapeSquare : newEscapeSquares) {
            if (isSquareControlled(tempBoard, escapeSquare[0], escapeSquare[1], Integer.signum(piece))) {
                score += 100;
            }
        }
        
        // Coordinate attacks with other pieces
        int attackersCount = countAttackers(tempBoard, enemyKingPos[0], enemyKingPos[1], Integer.signum(piece));
        if (attackersCount > 1) {
            // As more attackers the better the chances right? i guess...
            score += attackersCount * 100;
        }
        
        // Check if move creates a discovered attack
        boolean createsDiscoveredAttack = false;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (tempBoard[i][j] != 0 && Integer.signum(tempBoard[i][j]) == Integer.signum(piece)) {
                    if (canAttackKing(tempBoard, i, j, enemyKingPos[0], enemyKingPos[1])) {
                        createsDiscoveredAttack = true;
                        break;
                    }
                }
            }
        }
        if (createsDiscoveredAttack) {
            score += 200;
        }
        
        return score;
    }

    private boolean canAttackKing(int[][] board, int fromX, int fromY, int kingX, int kingY) {
        Game tempGame = new Game(8);
        tempGame.setBoard(board);
        List<int[]> moves = tempGame.calculatePossibleMoves(fromX, fromY);
        
        for (int[] move : moves) {
            if (move[0] == kingX && move[1] == kingY) {
                return true;
            }
        }
        return false;
    }

    private List<int[]> getKingEscapeSquares(int[][] board, int kingX, int kingY) {
        List<int[]> escapeSquares = new ArrayList<>();
        int kingSign = Integer.signum(board[kingX][kingY]);
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int[] dir : directions) {
            int newX = kingX + dir[0];
            int newY = kingY + dir[1];
            
            if (isInBounds(newX, newY)) {
                // Square must be empty or having enemy piece
                if (board[newX][newY] == 0 || Integer.signum(board[newX][newY]) != kingSign) {
                    // Create temporary board to test if move would be safe
                    int[][] tempBoard = new int[board.length][board.length];
                    for (int i = 0; i < board.length; i++) {
                        System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
                    }
                    
                    // Simulate king move
                    tempBoard[newX][newY] = board[kingX][kingY];
                    tempBoard[kingX][kingY] = 0;
                    
                    // Check if square is safe
                    if (!isSquareUnderAttack(tempBoard, newX, newY, -kingSign)) {
                        escapeSquares.add(new int[]{newX, newY});
                    }
                }
            }
        }
        
        return escapeSquares;
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
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == player && (i != x || j != y)) {
                    // Don't count the piece itself
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
        
        // If defending piece exists, only consider low attackers
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
            /*
            100 -> Pawn
            500 -> Rook
            300 -> Knight
            300 -> Bishop
            900 -> Queen
            10000 -> King
             */
            case 1 -> 100;
            case 2 -> 500;
            case 3 -> 300;
            case 4 -> 300;
            case 5 -> 900;
            case 6 -> 10000;
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