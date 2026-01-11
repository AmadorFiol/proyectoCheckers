package com.sjo.checkers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para notificar cuando un jugador se une
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerJoinedResponse {
    private String roomId;
    private String playerNickname;
    private String color;
    private int playerCount;
    private boolean gameStarted;
    private String message;
}