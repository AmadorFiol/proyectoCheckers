package com.sjo.checkers.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Player {
    private String id;
    private String nickname;
    private PieceColor color;
    private String sessionId; // Id sesion con WebSocket

    public Player(String nickname, String sessionId) {
        this.id = UUID.randomUUID().toString();
        this.nickname = nickname;
        this.sessionId = sessionId;
    }
}