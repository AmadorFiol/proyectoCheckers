package com.sjo.checkers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para información de una sala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoResponse {
    private String roomId;
    private String roomName;
    private int currentPlayers;
    private int maxPlayers;
    private String status;
}