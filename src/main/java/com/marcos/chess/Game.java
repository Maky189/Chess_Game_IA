package com.marcos.chess;

import java.util.List;
import java.util.ArrayList;

public class Game {

    /*
    White Pieces:
        - Pawn = 1
        - Rook = 2
        - Knight = 3
        - Bishop = 4
        - Queen = 5
        - King = 6

    Black Pieces:
        - Pawn = -1
        - Rook = -2
        - Knight = -3
        - Bishop = -4
        - Queen = -5
        - King = -6
    */


    private int[][] board;

    private boolean hasKingMoved = false;
    private boolean hasKingsideRookMoved = false;
    private boolean hasQueensideRookMoved = false;
    private boolean whiteHasCastled = false;
    private boolean blackHasCastled = false;
    private int currentPlayer = 1;


    private int lastMoveFromX = -1;
    private int lastMoveFromY = -1;
    private int lastMoveToX = -1;
    private int lastMoveToY = -1;
    private int lastMovePiece = 0;
    private int enPassantTargetX = -1;
    private int enPassantTargetY = -1;

    public Game(int size) {
        this.board = new int[size][size];

        //Set up pawns
        for (int i = 0; i < size; i++) {
            this.board[1][i] = -1;
            this.board[6][i] = 1;
        }

        //Set up rooks
        board[0][0] = board[0][7] = -2;
        board[7][0] = board[7][7] = 2;

        //Set up knights
        board[0][1] = board[0][6] = -3;
        board[7][1] = board[7][6] = 3;

        //Set up bishops
        board[0][2] = board[0][5] = -4;
        board[7][2] = board[7][5] = 4;

        //Set up queens
        board[0][3] = -5;
        board[7][3] = 5;

        //Set up kings
        board[0][4] = -6;
        board[7][4] = 6;

    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < board.length && y >= 0 && y < board[x].length;
    }

    public List<int[]> calculatePossibleMoves(int x, int y) {
        int piece = board[x][y];
        if (piece == 0) return new ArrayList<>();
        List<int[]> moves = calculateRawMoves(board, x, y);
        if (moves.isEmpty()) return moves;

        //check for moeves that could do a check
        moves = moves.stream().filter(move -> !wouldResultInCheck(x, y, move[0], move[1])).toList();

        // check pinned piece
        if (Math.abs(piece) != 6) {
            moves = filterPinnedMoves(x, y, moves);
        }

        return moves;
    }

    private List<int[]> filterPinnedMoves(int pieceX, int pieceY, List<int[]> moves) {
        int piece = board[pieceX][pieceY];
        int kingValue = (piece > 0) ? 6 : -6;

        // Find king
        int[] kingPos = null;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == kingValue) {
                    kingPos = new int[]{i, j};
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return moves;

        int kingX = kingPos[0];
        int kingY = kingPos[1];

        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            if (!isInLine(kingX, kingY, pieceX, pieceY, dx, dy)) continue;

            // Check for pinned piece
            boolean foundPiece = false;
            int x = kingX + dx;
            int y = kingY + dy;

            while (x >= 0 && x < board.length && y >= 0 && y < board.length) {
                if (x == pieceX && y == pieceY) {
                    foundPiece = true;
                } else if (board[x][y] != 0) {
                    if (foundPiece) {
                        int enemyPiece = board[x][y];
                        if (Integer.signum(enemyPiece) != Integer.signum(piece)) {
                            boolean canPin = switch (Math.abs(enemyPiece)) {
                                case 2 -> dx == 0 || dy == 0;
                                case 4 -> Math.abs(dx) == Math.abs(dy);
                                case 5 -> true;
                                default -> false;
                            };

                            if (canPin) {
                                return moves.stream().filter(move -> isInLine(kingX, kingY, move[0], move[1], dx, dy)).toList();
                            }
                        }
                        break;
                    }
                    break;
                }
                x += dx;
                y += dy;
            }
        }

        return moves;
    }

    private boolean isInLine(int x1, int y1, int x2, int y2, int dx, int dy) {
        if (dx == 0) return y1 == y2;
        if (dy == 0) return x1 == x2;
        return Math.abs(x1 - x2) == Math.abs(y1 - y2) &&
                Integer.signum(x2 - x1) == Integer.signum(dx) &&
                Integer.signum(y2 - y1) == Integer.signum(dy);
    }

    private int[] findKingPosition(int[][] board, int kingValue) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == kingValue) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private boolean isKingThreatened(int[][] board, int kingX, int kingY, int opponentSign) {
        // Add parameter to prevent infinite recursion
        return isKingThreatened(board, kingX, kingY, opponentSign, false);
    }

    private boolean isKingThreatened(int[][] board, int kingX, int kingY, int opponentSign, boolean isCastlingCheck) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != 0 && Integer.signum(board[i][j]) == opponentSign) {
                    // For castling checks, we only need to check direct attacks
                    if (isCastlingCheck) {
                        // Simple check for direct attacks without recursion
                        List<int[]> directMoves = calculateDirectAttacks(board, i, j);
                        for (int[] move : directMoves) {
                            if (move[0] == kingX && move[1] == kingY) {
                                return true;
                            }
                        }
                    } else {
                        List<int[]> moves = calculateRawMoves(board, i, j);
                        for (int[] move : moves) {
                            if (move[0] == kingX && move[1] == kingY) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<int[]> calculateDirectAttacks(int[][] board, int x, int y) {
        List<int[]> attacks = new ArrayList<>();
        int piece = Math.abs(board[x][y]);
        
        switch (piece) {
            case 1: // Pawn
                int direction = Integer.signum(board[x][y]);
                if (isInBounds(x + direction, y - 1)) {
                    attacks.add(new int[]{x + direction, y - 1});
                }
                if (isInBounds(x + direction, y + 1)) {
                    attacks.add(new int[]{x + direction, y + 1});
                }
                break;
                
            case 2: // Rook
                addLinearAttacks(board, x, y, attacks, 0, 1);  // right
                addLinearAttacks(board, x, y, attacks, 0, -1); // left
                addLinearAttacks(board, x, y, attacks, 1, 0);  // down
                addLinearAttacks(board, x, y, attacks, -1, 0); // up
                break;
                
            case 3: // Knight
                int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
                for (int[] move : knightMoves) {
                    int newX = x + move[0];
                    int newY = y + move[1];
                    if (isInBounds(newX, newY)) {
                        attacks.add(new int[]{newX, newY});
                    }
                }
                break;
                
            case 4: // Bishop
                addLinearAttacks(board, x, y, attacks, 1, 1);   // down-right
                addLinearAttacks(board, x, y, attacks, 1, -1);  // down-left
                addLinearAttacks(board, x, y, attacks, -1, 1);  // up-right
                addLinearAttacks(board, x, y, attacks, -1, -1); // up-left
                break;
                
            case 5: // Queen
                addLinearAttacks(board, x, y, attacks, 0, 1);   // right
                addLinearAttacks(board, x, y, attacks, 0, -1);  // left
                addLinearAttacks(board, x, y, attacks, 1, 0);   // down
                addLinearAttacks(board, x, y, attacks, -1, 0);  // up
                addLinearAttacks(board, x, y, attacks, 1, 1);   // down-right
                addLinearAttacks(board, x, y, attacks, 1, -1);  // down-left
                addLinearAttacks(board, x, y, attacks, -1, 1);  // up-right
                addLinearAttacks(board, x, y, attacks, -1, -1); // up-left
                break;
                
            case 6: // King
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        if (isInBounds(x + i, y + j)) {
                            attacks.add(new int[]{x + i, y + j});
                        }
                    }
                }
                break;
        }
        return attacks;
    }

    private void addLinearAttacks(int[][] board, int x, int y, List<int[]> attacks, int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;
        while (isInBounds(newX, newY)) {
            attacks.add(new int[]{newX, newY});
            if (board[newX][newY] != 0) break; // Stop at first piece encountered
            newX += dx;
            newY += dy;
        }
    }

    private boolean wouldResultInCheck(int pieceX, int pieceY, int newX, int newY) {
        int piece = board[pieceX][pieceY];
        int kingValue = (piece > 0) ? 6 : -6;

        // Create a temporary board
        int[][] tempBoard = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
        }

        // Test the move
        tempBoard[newX][newY] = tempBoard[pieceX][pieceY];
        tempBoard[pieceX][pieceY] = 0;

        // Find king position
        int kingX, kingY;
        int[] kingPos = findKingPosition(tempBoard, kingValue);
        if (kingPos == null) return true;  // Missing this check
        kingX = kingPos[0];
        kingY = kingPos[1];

        int opponentSign = (piece > 0) ? -1 : 1;

        // Check all threats
        return isDiagonallyThreatened(tempBoard, kingX, kingY, opponentSign) ||
               isThreatened(tempBoard, kingX, kingY, opponentSign) ||
               isKnightThreatened(tempBoard, kingX, kingY, opponentSign) ||
               isPawnThreatened(tempBoard, kingX, kingY, opponentSign);
    }

    private boolean isDiagonallyThreatened(int[][] tempBoard, int kingX, int kingY, int opponentSign) {
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        for (int[] dir : directions) {
            int x = kingX + dir[0];
            int y = kingY + dir[1];
            while (x >= 0 && x < tempBoard.length && y >= 0 && y < tempBoard.length) {
                if (tempBoard[x][y] != 0) {
                    if (Integer.signum(tempBoard[x][y]) == opponentSign) {
                        int piece = Math.abs(tempBoard[x][y]);
                        if (piece == 4 || piece == 5) return true;
                    }
                    break;
                }
                x += dir[0];
                y += dir[1];
            }
        }
        return false;
    }

    //This cheks in a perpendicular way, this is not optimal, I have to find another solution for this too
    private boolean isThreatened(int[][] tempBoard, int kingX, int kingY, int opponentSign) {
        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        for (int[] direction : directions) {
            int x = kingX + direction[0];
            int y = kingY + direction[1];
            while (x >= 0 && x < tempBoard.length && y >= 0 && y < tempBoard.length) {
                if (tempBoard[x][y] != 0) {
                    if (Integer.signum(tempBoard[x][y]) == opponentSign) {
                        int piece = Math.abs(tempBoard[x][y]);
                        if (piece == 2 || piece == 5) return true;
                    }
                    break;
                }
                x += direction[0];
                y += direction[1];
            }
        }
        return false;
    }

    private boolean isKnightThreatened(int[][] tempBoard, int kingX, int kingY, int opponentSign) {
        int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
        for (int[] move : knightMoves) {
            int x = kingX + move[0];
            int y = kingY + move[1];
            if (x >= 0 && x < tempBoard.length && y >= 0 && y < tempBoard.length) {
                if (tempBoard[x][y] != 0 &&
                        Integer.signum(tempBoard[x][y]) == opponentSign &&
                        Math.abs(tempBoard[x][y]) == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPawnThreatened(int[][] tempBoard, int kingX, int kingY, int opponentSign) {
        // Check the two diagonal squares where enemy pawns could attack from
        int pawnDirection = opponentSign > 0 ? 1 : -1;  // Direction pawns move
        int[] pawnColumns = {-1, 1};  // Left and right diagonal

        for (int colOffset : pawnColumns) {
            int x = kingX + pawnDirection;  // One row in the direction pawns come from
            int y = kingY + colOffset;      // Left or right diagonal
            
            if (isInBounds(x, y)) {
                int piece = tempBoard[x][y];
                // Check if there's an enemy pawn in this position
                if (piece != 0 && 
                    Integer.signum(piece) == opponentSign && 
                    Math.abs(piece) == 1) {  // 1 represents pawn
                    return true;
                }
            }
        }
        return false;
    }

    private List<int[]> calculateRawMoves(int[][] board, int x, int y) {
        int piece = board[x][y];
        List<int[]> moves = new ArrayList<>();

        switch (Math.abs(piece)) {
            case 1: calculatePawnMoves(x, y, piece, moves); break;
            case 2: calculateRookMoves(x, y, piece, moves); break;
            case 3: calculateKnightMoves(x, y, piece, moves); break;
            case 4: calculateBishopMoves(x, y, piece, moves); break;
            case 5: calculateQueenMoves(x, y, piece, moves); break;
            case 6: calculateKingMoves(x, y, piece, moves); break;
        }

        return moves;
    }

    private void calculatePawnMoves(int x, int y, int piece, List<int[]> moves) {
        int direction = (piece > 0) ? -1 : 1;

        // Normal pawn moves (one square forward)
        int newX = x + direction;
        if (isInBounds(newX, y) && board[newX][y] == 0) {
            moves.add(new int[]{newX, y});

            // Initial two-square move
            if ((piece > 0 && x == 6) || (piece < 0 && x == 1)) {
                int twoSquares = x + 2 * direction;
                if (isInBounds(twoSquares, y) && board[twoSquares][y] == 0) {
                    moves.add(new int[]{twoSquares, y});
                }
            }
        }

        // Normal captures
        for (int offset : new int[]{-1, 1}) {
            int newY = y + offset;
            if (isInBounds(newX, newY)) {
                // Regular capture
                if (board[newX][newY] != 0 && Integer.signum(board[newX][newY]) != Integer.signum(piece)) {
                    moves.add(new int[]{newX, newY});
                }
                // En passant capture
                else if (newX == enPassantTargetX && newY == enPassantTargetY && Math.abs(lastMovePiece) == 1 && Integer.signum(lastMovePiece) != Integer.signum(piece)) {
                    moves.add(new int[]{newX, newY});
                }
            }
        }
    }

    public void updateLastMove(int fromX, int fromY, int toX, int toY) {
        lastMoveFromX = fromX;
        lastMoveFromY = fromY;
        lastMoveToX = toX;
        lastMoveToY = toY;
        lastMovePiece = board[toX][toY];

        if (Math.abs(lastMovePiece) == 1 && Math.abs(fromX - toX) == 2) {
            enPassantTargetX = (fromX + toX) / 2;
            enPassantTargetY = toY;
        } else {
            enPassantTargetX = -1;
            enPassantTargetY = -1;
        }
    }

    public boolean isEnPassantCapture(int fromX, int fromY, int toX, int toY) {
        int piece = board[fromX][fromY];
        if (Math.abs(piece) != 1) return false;
        if (Math.abs(fromY - toY) != 1) return false;

        return toX == enPassantTargetX && toY == enPassantTargetY;
    }

    public void performEnPassantCapture(int toX, int toY) {
        board[enPassantTargetX][enPassantTargetY] = 0;
    }

    public int getLastMoveToX() {
        return lastMoveToX;
    }

    public int getLastMoveToY() {
        return lastMoveToY;
    }

    public int getLastMovePiece() {
        return lastMovePiece;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
    }

    public int getLastMoveFromX() {
        return lastMoveFromX;
    }

    public int getLastMoveFromY() {
        return lastMoveFromY;
    }

    public void switchPlayer() {
        currentPlayer = -currentPlayer;
    }

    public int getEnPassantTargetX() { return enPassantTargetX; }
    public int getEnPassantTargetY() { return enPassantTargetY; }

    private void calculateLinearMoves(int x, int y, int piece, List<int[]> moves, int DirX, int DirY) {
        int newX = x + DirX;
        int newY = y + DirY;

        while (isInBounds(newX, newY)) {
            // If the square is empty, add it
            if (board[newX][newY] == 0) {
                moves.add(new int[]{newX, newY});
            }
            // If the square is occupied check if opponent
            else if (Integer.signum(board[newX][newY]) != Integer.signum(piece)) {
                moves.add(new int[]{newX, newY});
                break;
            } else {
                break;
            }

            newX += DirX;
            newY += DirY;
        }
    }

    private void calculateRookMoves(int x, int y, int piece, List<int[]> moves) {
        // Rook moves in straight lines horizontal and vertical
        calculateLinearMoves(x, y, piece, moves, -1, 0);
        calculateLinearMoves(x, y, piece, moves, 1, 0);
        calculateLinearMoves(x, y, piece, moves, 0, -1);
        calculateLinearMoves(x, y, piece, moves, 0, 1);
    }

    private void calculateBishopMoves(int x, int y, int piece, List<int[]> moves) {
        // Bishop moves diagonally
        calculateLinearMoves(x, y, piece, moves, -1, -1);
        calculateLinearMoves(x, y, piece, moves, -1, 1);
        calculateLinearMoves(x, y, piece, moves, 1, -1);
        calculateLinearMoves(x, y, piece, moves, 1, 1);
    }

    private void calculateQueenMoves(int x, int y, int piece, List<int[]> moves) {
        // Queen uses Rook and Bishop moves
        calculateRookMoves(x, y, piece, moves);
        calculateBishopMoves(x, y, piece, moves);
    }

    private void calculateKingMoves(int x, int y, int piece, List<int[]> moves) {
        // Regular king moves
        int[][] possibilities = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        int opponentSign = Integer.signum(piece) * -1;

        for (int[] possibility : possibilities) {
            int newX = x + possibility[0];
            int newY = y + possibility[1];

            if (isInBounds(newX, newY)) {
                // Check if square is empty or has opponent's piece
                if (board[newX][newY] == 0 || Integer.signum(board[newX][newY]) != Integer.signum(piece)) {
                    // Create a temporary board to test the move
                    int[][] tempBoard = new int[board.length][board.length];
                    for (int i = 0; i < board.length; i++) {
                        System.arraycopy(board[i], 0, tempBoard[i], 0, board[i].length);
                    }
                    
                    // Try the move on the temporary board
                    tempBoard[newX][newY] = piece;
                    tempBoard[x][y] = 0;
                    
                    // Only add the move if it doesn't put/leave the king in check
                    if (!isDiagonallyThreatened(tempBoard, newX, newY, opponentSign) &&
                        !isThreatened(tempBoard, newX, newY, opponentSign) &&
                        !isKnightThreatened(tempBoard, newX, newY, opponentSign) &&
                        !isPawnThreatened(tempBoard, newX, newY, opponentSign)) {
                        moves.add(new int[]{newX, newY});
                    }
                }
            }
        }

        // Castling logic
        int row = (piece > 0) ? 7 : 0;
        if (x == row && y == 4) {
            if (canCastleKingside(row, piece)) {
                moves.add(new int[]{row, 6});
            }
            if (canCastleQueenside(row, piece)) {
                moves.add(new int[]{row, 2});
            }
        }
    }

    private boolean canCastleKingside(int row, int piece) {
        if ((piece > 0 && whiteHasCastled) || (piece < 0 && blackHasCastled)) {
            return false;
        }

        if (board[row][5] != 0 || board[row][6] != 0) {
            return false;
        }

        if (board[row][7] != piece / Math.abs(piece) * 2) {
            return false;
        }

        int opponentSign = piece > 0 ? -1 : 1;
        return !isKingThreatened(board, row, 4, opponentSign, true) && 
               !isKingThreatened(board, row, 5, opponentSign, true) && 
               !isKingThreatened(board, row, 6, opponentSign, true);
    }

    private boolean canCastleQueenside(int row, int piece) {
        if ((piece > 0 && whiteHasCastled) || (piece < 0 && blackHasCastled)) {
            return false;
        }

        if (board[row][1] != 0 || board[row][2] != 0 || board[row][3] != 0) {
            return false;
        }

        if (board[row][0] != piece / Math.abs(piece) * 2) {
            return false;
        }

        int opponentSign = piece > 0 ? -1 : 1;
        return !isKingThreatened(board, row, 4, opponentSign) && !isKingThreatened(board, row, 3, opponentSign) && !isKingThreatened(board, row, 2, opponentSign);
    }

    public void performKingsideCastle(int kingRow) {
        board[kingRow][5] = board[kingRow][7];
        board[kingRow][7] = 0;
        if (kingRow == 7) {
            whiteHasCastled = true;
        } else {
            blackHasCastled = true;
        }
    }

    public void performQueensideCastle(int kingRow) {
        board[kingRow][3] = board[kingRow][0];
        board[kingRow][0] = 0;
        if (kingRow == 7) {
            whiteHasCastled = true;
        } else {
            blackHasCastled = true;
        }
    }

    public boolean isCastlingMove(int fromX, int fromY, int toX, int toY) {
        int piece = Math.abs(board[fromX][fromY]);
        if (piece != 6) return false;

        return fromX == toX && Math.abs(fromY - toY) == 2;
    }

    private void calculateKnightMoves(int x, int y, int piece, List<int[]> moves) {
        int[][] possibilities = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};

        for (int[] possibility : possibilities) {
            int newX = x + possibility[0];
            int newY = y + possibility[1];

            // Validate indext
            if (isInBounds(newX, newY) && (board[newX][newY] == 0 || Integer.signum(board[newX][newY]) != Integer.signum(piece))) {
                moves.add(new int[]{newX, newY});
            }
        }
    }
}