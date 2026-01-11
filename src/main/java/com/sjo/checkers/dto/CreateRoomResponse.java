package com.sjo.checkers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para la respuesta de creación de sala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomResponse {
    private String roomId;
    private String roomName;
    private String playerId;
    private String message;
}