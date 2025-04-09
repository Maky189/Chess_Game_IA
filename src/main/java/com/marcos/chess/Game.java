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

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < board.length && y >= 0 && y < board[0].length;
    }

    public List<int[]> calculatePossibleMoves(int row, int col) {
        int piece = board[row][col];
        if (piece == 0) {
            return new ArrayList<>(); // No moves for an empty square
        }

        List<int[]> possibleMoves = new ArrayList<>();

        switch (Math.abs(piece)) {
            case 1: // Pawn
                calculatePawnMoves(row, col, piece, possibleMoves);
                break;
            case 2: // Rook
                calculateRookMoves(row, col, piece, possibleMoves);
                break;
            case 3: // Knight
                calculateKnightMoves(row, col, piece, possibleMoves);
                break;
            case 4: // Bishop
                calculateBishopMoves(row, col, piece, possibleMoves);
                break;
            case 5: // Queen
                calculateRookMoves(row, col, piece, possibleMoves);
                calculateBishopMoves(row, col, piece, possibleMoves);
                break;
            case 6: // King
                calculateKingMoves(row, col, piece, possibleMoves);
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

    private void calculateLinearMoves(int x, int y, int piece, List<int[]> moves, int dirX, int dirY) {
        int newX = x  + dirX;
        int newY = y + dirY;
        while (isInBounds(newX, newY)) {

            if(board[newX][newY] != 0) {
                moves.add(new int[]{newX, newY});
            } else if (board[newX][newY] == 0) {
                Integer.signum(piece);
                moves.add(new int[]{newX, newY});
                break;
            } else {
                break;
            }
            newX += newX;
            newY += newY;
        }
    }

    private void calculateRookMoves(int x, int y, int piece, List<int[]> moves) {
        calculateLinearMoves(x, y, piece, moves, 1, 0);
        calculateLinearMoves(x, y, piece, moves, 0, 1);
        calculateLinearMoves(x, y, piece, moves, -1, 0);
        calculateLinearMoves(x, y, piece, moves, 0, -1);
    }

    private void calculateBishopMoves(int x, int y, int piece, List<int[]> moves) {
        calculateLinearMoves(x, y, piece, moves, 1, 1);
        calculateLinearMoves(x, y, piece, moves, 1, -1);
        calculateLinearMoves(x, y, piece, moves, -1, 1);
        calculateLinearMoves(x, y, piece, moves, -1, -1);
    }

    private void calculateQueenMoves(int x, int y, int piece, List<int[]> moves) {
        calculateRookMoves(x, y, piece, moves);
        calculateBishopMoves(x, y, piece, moves);
    }

    private void calculateKingMoves(int row, int col, int piece, List<int[]> moves) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (isInBounds(row + i, col + j) && board[row + i][col + j] != Integer.signum(piece)) {
                    moves.add(new int[]{row + i, col + j});
                }
            }
        }
    }

    private void calculateKnightMoves(int row, int col, int piece, List<int[]> moves) {
        int[][] directions = new int[][]{
                {-2, -1}, {-2, 1}, {-1, -2},
                {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] direction : directions) {
            int newX = row + direction[0];
            int newY = col + direction[1];
            if (isInBounds(newX, newY) && (board[newX][newY] == 0) || (Integer.signum(board[newX][newY]) != Integer.signum(piece))) {
                moves.add(new int[]{newX, newY});
            }
        }
    }


}
