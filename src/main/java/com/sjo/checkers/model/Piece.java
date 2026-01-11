package com.sjo.checkers.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa una ficha en el tablero
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private int row;
    private int col;
    private PieceColor color;
    private boolean isKing;

    public Piece(int row, int col, PieceColor color) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.isKing = false;
    }

    // Lombok genera automáticamente estos getters/setters pero los incluimos explícitamente por si acaso
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public PieceColor getColor() {
        return color;
    }

    public boolean isKing() {
        return isKing;
    }
}