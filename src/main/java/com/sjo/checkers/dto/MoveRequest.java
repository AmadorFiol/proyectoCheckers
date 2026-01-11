package com.sjo.checkers.dto;

import com.sjo.checkers.model.Move;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para enviar un movimiento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequest {
    private String roomId;
    private String playerId;
    private Move move;
}