package com.sjo.checkers.model;

public enum GameStatus {
    WAITING,    // Esperando al segundo jugador
    IN_PROGRESS, // Partida en curso
    FINISHED,    // Partida terminada
    ABANDONED    // Partida abandonada por desconexión
}