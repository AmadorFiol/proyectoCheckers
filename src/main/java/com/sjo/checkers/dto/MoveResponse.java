package com.sjo.checkers.dto;

import com.sjo.checkers.model.Move;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para notificar un movimiento a los clientes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveResponse {
    private Move move;
    private boolean valid;
    private String message;
    private GameStateResponse gameState;
}