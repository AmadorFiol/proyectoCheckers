package com.sjo.checkers.dto;

import com.sjo.checkers.model.Board;
import com.sjo.checkers.model.GameStatus;
import com.sjo.checkers.model.PieceColor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para actualizar el estado del juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private String roomId;
    private Board board;
    private PieceColor currentTurn;
    private GameStatus status;
    private String whitePlayerNickname;
    private String blackPlayerNickname;
    private String winnerNickname;
    private String message; // Mensajes informativos
}