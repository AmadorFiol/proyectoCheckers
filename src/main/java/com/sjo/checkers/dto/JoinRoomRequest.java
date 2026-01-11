package com.sjo.checkers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para crear o unirse a una sala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
    private String roomId;
    private String playerNickname;
}