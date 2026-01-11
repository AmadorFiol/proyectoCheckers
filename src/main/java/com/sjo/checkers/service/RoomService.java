package com.sjo.checkers.service;

import com.sjo.checkers.model.Player;
import com.sjo.checkers.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar las salas de juego
 */
@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /**
     * Crea una nueva sala
     */
    public Room createRoom(String roomName, Player creator) {
        Room room = new Room(roomName, creator.getId());
        room.addPlayer(creator);
        rooms.put(room.getId(), room);
        return room;
    }

    /**
     * Obtiene una sala por su ID
     */
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * Une un jugador a una sala
     */
    public boolean joinRoom(String roomId, Player player) {
        Room room = rooms.get(roomId);
        if (room == null || room.isFull()) {
            return false;
        }
        return room.addPlayer(player);
    }

    /**
     * Obtiene todas las salas disponibles (no llenas)
     */
    public List<Room> getAvailableRooms() {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (!room.isFull()) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    /**
     * Elimina una sala
     */
    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    /**
     * Elimina salas vacías (limpieza)
     */
    public void removeEmptyRooms() {
        rooms.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Verifica si una sala existe
     */
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    /**
     * Obtiene el jugador de una sala por su session ID
     */
    public Player getPlayerBySessionId(String roomId, String sessionId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return null;
        }

        Player whitePlayer = room.getGame().getWhitePlayer();
        Player blackPlayer = room.getGame().getBlackPlayer();

        if (whitePlayer != null && whitePlayer.getSessionId().equals(sessionId)) {
            return whitePlayer;
        }
        if (blackPlayer != null && blackPlayer.getSessionId().equals(sessionId)) {
            return blackPlayer;
        }

        return null;
    }
}