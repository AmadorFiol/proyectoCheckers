package com.sjo.checkers.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Room {
    private String id;
    private String name;
    private Game game;
    private int maxPlayers = 2;
    private LocalDateTime createdAt;
    private String creatorId; // ID del jugador que creó la sala

    public Room(String name, String creatorId) {
        this.id = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // ID corto para facilitar
        this.name = name;
        this.game = new Game();
        this.createdAt = LocalDateTime.now();
        this.creatorId = creatorId;
    }

    /**
     * Verifica si la sala está llena
     */
    public boolean isFull() {
        return game.getWhitePlayer() != null && game.getBlackPlayer() != null;
    }

    /**
     * Verifica si la sala está vacía
     */
    public boolean isEmpty() {
        return game.getWhitePlayer() == null && game.getBlackPlayer() == null;
    }

    /**
     * Añade un jugador a la sala
     */
    public boolean addPlayer(Player player) {
        if (isFull()) {
            return false;
        }

        if (game.getWhitePlayer() == null) {
            game.setWhitePlayer(player);
            player.setColor(PieceColor.WHITE);
        } else if (game.getBlackPlayer() == null) {
            game.setBlackPlayer(player);
            player.setColor(PieceColor.BLACK);
            game.setStatus(GameStatus.IN_PROGRESS); // Ahora que hay 2 jugadores comenzamos la partida
        }

        return true;
    }

    /**
     * Obtiene el número de jugadores actuales
     */
    public int getPlayerCount() {
        int count = 0;
        if (game.getWhitePlayer() != null) count++;
        if (game.getBlackPlayer() != null) count++;
        return count;
    }
}