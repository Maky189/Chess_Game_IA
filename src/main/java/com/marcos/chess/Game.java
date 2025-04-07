package com.marcos.chess;

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

}
