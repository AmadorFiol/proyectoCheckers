package com.sjo.checkers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para crear una sala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String roomName;
    private String playerNickname;
}