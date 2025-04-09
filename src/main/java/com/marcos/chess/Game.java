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

    public Game(int size) {
        this.board = new int[size][size];

        //Set up pawns
        for (int i = 0; i < size; i++) {
            this.board[1][i] = -1; //Black
            this.board[6][i] = 1; //White
        }

        //Set up rooks
        board[0][0] = board[0][7] = -2; //Black
        board[7][0] = board[7][7] = 2; //White

        //Set up knights
        board[0][1] = board[0][6] = -3; //Black
        board[7][1] = board[7][6] = 3; //White

        //Set up bishops
        board[0][2] = board[0][5] = -4; //Black
        board[7][2] = board[7][5] = 4; //White

        //Set up queens
        board[0][3] = -5; //Black
        board[7][3] = 5; //White

        //Set up kings
        board[0][4] = -6; //Black
        board[7][4] = 6; //White

    }
    public int[][] getBoard() {
        return this.board;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[row].length;
    }

    public List<int[]> calculatePossibleMoves(int x, int y) {
        int piece = board[x][y];
        if (piece == 0) {
            return new ArrayList<>(); // No moves for an empty square
        }

        List<int[]> possibleMoves = new ArrayList<>();

        switch (Math.abs(piece)) {
            case 1: // Pawn
                calculatePawnMoves(x, y, piece, possibleMoves);
                break;
            case 2: // Rook
                calculateRookMoves(x, y, piece, possibleMoves);
                break;
            case 3: // Knight
                calculateKnightMoves(x, y, piece, possibleMoves);
                break;
            case 4: // Bishop
                calculateBishopMoves(x, y, piece, possibleMoves);
                break;
            case 5: // Queen
                calculateQueenMoves(x, y, piece, possibleMoves);
                break;
            case 6: // King
                calculateKingMoves(x, y, piece, possibleMoves);
                break;
        }

        return possibleMoves;
    }

    private void calculatePawnMoves(int row, int col, int piece, List<int[]> moves) {
        int direction = piece > 0 ? -1 : 1; // White moves up (-1), Black moves down (+1)

        // Move forward
        if (isInBounds(row + direction, col) && board[row + direction][col] == 0) {
            moves.add(new int[]{row + direction, col});
        }

        // Capture diagonally
        if (isInBounds(row + direction, col - 1) && board[row + direction][col - 1] != 0 &&
                Integer.signum(board[row + direction][col - 1]) != Integer.signum(piece)) {
            moves.add(new int[]{row + direction, col - 1});
        }

        if (isInBounds(row + direction, col + 1) && board[row + direction][col + 1] != 0 &&
                Integer.signum(board[row + direction][col + 1]) != Integer.signum(piece)) {
            moves.add(new int[]{row + direction, col + 1});
        }
    }

    private void calculateLinearMoves(int row, int col, int piece, List<int[]> moves, int rowDir, int colDir) {
        int newRow = row + rowDir;
        int newCol = col + colDir;

        while (isInBounds(newRow, newCol)) {
            // If the square is empty, add it
            if (board[newRow][newCol] == 0) {
                moves.add(new int[]{newRow, newCol});
            }
            // If the square is occupied, check if by an opponent
            else if (Integer.signum(board[newRow][newCol]) != Integer.signum(piece)) {
                moves.add(new int[]{newRow, newCol}); // Add opponent's piece
                break; // Stop further moves in this direction
            } else {
                // Friendly piece blocks the path
                break;
            }

            // Move further in the same direction
            newRow += rowDir;
            newCol += colDir;
        }
    }

    private void calculateRookMoves(int row, int col, int piece, List<int[]> moves) {
        // Rook moves in straight lines (horizontal and vertical)
        calculateLinearMoves(row, col, piece, moves, -1, 0); // Up
        calculateLinearMoves(row, col, piece, moves, 1, 0);  // Down
        calculateLinearMoves(row, col, piece, moves, 0, -1); // Left
        calculateLinearMoves(row, col, piece, moves, 0, 1);  // Right
    }

    private void calculateBishopMoves(int row, int col, int piece, List<int[]> moves) {
        // Bishop moves diagonally
        calculateLinearMoves(row, col, piece, moves, -1, -1); // Up-Left
        calculateLinearMoves(row, col, piece, moves, -1, 1);  // Up-Right
        calculateLinearMoves(row, col, piece, moves, 1, -1);  // Down-Left
        calculateLinearMoves(row, col, piece, moves, 1, 1);   // Down-Right
    }

    private void calculateQueenMoves(int row, int col, int piece, List<int[]> moves) {
        // Queen combines Rook and Bishop moves
        calculateRookMoves(row, col, piece, moves);
        calculateBishopMoves(row, col, piece, moves);
    }

    private void calculateKingMoves(int row, int col, int piece, List<int[]> moves) {
        // Possible king move offsets
        int[][] deltas = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},         {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] delta : deltas) {
            int newRow = row + delta[0];
            int newCol = col + delta[1];

            // Validate within board bounds and check for empty or opponent's piece
            if (isInBounds(newRow, newCol) &&
                    (board[newRow][newCol] == 0 || Integer.signum(board[newRow][newCol]) != Integer.signum(piece))) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    }

    private void calculateKnightMoves(int row, int col, int piece, List<int[]> moves) {
        // Possible knight move offsets
        int[][] deltas = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] delta : deltas) {
            int newRow = row + delta[0];
            int newCol = col + delta[1];

            // Validate within board bounds and check for empty or opponent's piece
            if (isInBounds(newRow, newCol) &&
                    (board[newRow][newCol] == 0 || Integer.signum(board[newRow][newCol]) != Integer.signum(piece))) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    }


}
