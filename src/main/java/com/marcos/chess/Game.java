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
        return this.board;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < board.length && y >= 0 && y < board[x].length;
    }

    public List<int[]> calculatePossibleMoves(int x, int y) {
        int piece = board[x][y];
        if (piece == 0) {
            // No moves
            return new ArrayList<>();
        }

        List<int[]> possibleMoves = new ArrayList<>();

        switch (Math.abs(piece)) {
            case 1: 
                calculatePawnMoves(x, y, piece, possibleMoves);
                break;
            case 2: 
                calculateRookMoves(x, y, piece, possibleMoves);
                break;
            case 3: 
                calculateKnightMoves(x, y, piece, possibleMoves);
                break;
            case 4: 
                calculateBishopMoves(x, y, piece, possibleMoves);
                break;
            case 5: 
                calculateQueenMoves(x, y, piece, possibleMoves);
                break;
            case 6: 
                calculateKingMoves(x, y, piece, possibleMoves);
                break;
        }

        return possibleMoves;
    }

    private void calculatePawnMoves(int x, int y, int piece, List<int[]> moves) {
        int direction = (piece > 0) ? -1 : 1;

        // Move one square d
        int newX = x + direction;
        if (isInBounds(newX, y) && board[newX][y] == 0) {
            moves.add(new int[]{newX, y});
        }

        // Move two squares in the begining
        if ((piece > 0 && x == 6) || (piece < 0 && x == 1)) {
            int twoSquares = x + 2 * direction;
            if (isInBounds(twoSquares, y) && board[twoSquares][y] == 0 && board[newX][y] == 0) {
                moves.add(new int[]{twoSquares, y});
            }
        }

        // Capture piece
        int[] captures = {-1, 1};
        for (int capture : captures) {
            int newY = y + capture;
            if (isInBounds(newX, newY) && board[newX][newY] != 0 &&
                    Integer.signum(board[newX][newY]) != Integer.signum(piece)) {
                moves.add(new int[]{newX, newY});
            }
        }
    }

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
        // Possible king moves
        int[][] possibilities = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] possibility : possibilities) {
            int newX = x + possibility[0];
            int newY = y + possibility[1];

            // Validate if is in bounds and not opponent in square
            if (isInBounds(newX, newY) &&
                    (board[newX][newY] == 0 || Integer.signum(board[newX][newY]) != Integer.signum(piece))) {
                moves.add(new int[]{newX, newY});
            }
        }
    }

    private void calculateKnightMoves(int x, int y, int piece, List<int[]> moves) {
        // Possible knight moves
        int[][] possibilities = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};

        for (int[] possibility : possibilities) {
            int newX = x + possibility[0];
            int newY = y + possibility[1];

            // Validate indext
            if (isInBounds(newX, newY) &&
                    (board[newX][newY] == 0 || Integer.signum(board[newX][newY]) != Integer.signum(piece))) {
                moves.add(new int[]{newX, newY});
            }
        }
    }

    public boolean isKingInCheck(int player) {
        int kingX = -1, kingY = -1;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == (player > 0 ? 6 : -6)) {
                    kingX = i;
                    kingY = j;
                    break;
                }
            }
        }

        if (kingX == -1 || kingY == -1) {
            System.out.println("King not found for player: " + player);
            return false;
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (Integer.signum(board[i][j]) != player && board[i][j] != 0) {
                    List<int[]> moves = calculatePossibleMoves(i, j);
                    for (int[] move : moves) {
                        if (move[0] == kingX && move[1] == kingY) {
                            System.out.println("King is in check! Player: " + player + ", Attacker: " + board[i][j]);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}