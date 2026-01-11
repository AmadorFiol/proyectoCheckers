package com.sjo.checkers.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Game {
    private String id;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private PieceColor currentTurn;
    private GameStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastMoveAt;
    private Player winner;

    public Game() {
        this.board = new Board();
        this.currentTurn = PieceColor.WHITE; // Las blancas empiezan
        this.status = GameStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Cambia el turno al otro jugador
     */
    public void switchTurn() {
        this.currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        this.lastMoveAt = LocalDateTime.now();
    }

    /**
     * Verifica si es el turno de un jugador específico
     */
    public boolean isPlayerTurn(Player player) {
        return player.getColor() == currentTurn;
    }
}