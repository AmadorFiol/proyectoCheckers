package com.sjo.checkers.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un movimiento en el juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private Boolean capture;
    private Position capturedPosition;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private int row;
        private int col;
    }

    // Método helper para verificar si es captura (maneja null)
    public boolean isCapture() {
        return capture != null && capture;
    }
}