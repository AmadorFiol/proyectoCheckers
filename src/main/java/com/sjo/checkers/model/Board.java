package com.sjo.checkers.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de juego (8x8)
 */
@Data
public class Board {
    private Piece[][] grid; // 8x8 grid
    private static final int BOARD_SIZE = 8;

    public Board() {
        this.grid = new Piece[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    /**
     * Inicializa el tablero con las fichas en sus posiciones iniciales
     */
    private void initializeBoard() {
        // Fichas negras (arriba, filas 0-2)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    grid[row][col] = new Piece(row, col, PieceColor.BLACK);
                }
            }
        }

        // Fichas blancas (abajo, filas 5-7)
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    grid[row][col] = new Piece(row, col, PieceColor.WHITE);
                }
            }
        }
    }

    public Piece[][] getGrid() {
        return grid;
    }

    public void setGrid(Piece[][] grid) {
        this.grid = grid;
    }

    /**
     * Obtiene una ficha en una posición específica
     */
    public Piece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * Coloca una ficha en una posición
     */
    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            grid[row][col] = piece;
            if (piece != null) {
                piece.setRow(row);
                piece.setCol(col);
            }
        }
    }

    /**
     * Elimina una ficha del tablero
     */
    public void removePiece(int row, int col) {
        if (isValidPosition(row, col)) {
            grid[row][col] = null;
        }
    }

    /**
     * Verifica si una posición es válida en el tablero
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Obtiene todas las fichas de un color
     */
    public List<Piece> getPiecesByColor(PieceColor color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }
}