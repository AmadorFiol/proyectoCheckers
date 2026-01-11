package com.sjo.checkers.controller;

import com.sjo.checkers.dto.CreateRoomRequest;
import com.sjo.checkers.dto.CreateRoomResponse;
import com.sjo.checkers.dto.RoomInfoResponse;
import com.sjo.checkers.model.Player;
import com.sjo.checkers.model.Room;
import com.sjo.checkers.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API REST para información de salas
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomRestController {

    private final RoomService roomService;

    /**
     * Crea una nueva sala vía REST (sin WebSocket)
     */
    @PostMapping("/create")
    public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            log.info("Creando sala vía REST: {}", request.getRoomName());

            // Generar un ID temporal para el creador (será reemplazado cuando se una por WebSocket)
            String tempSessionId = UUID.randomUUID().toString();
            Player creator = new Player(request.getPlayerNickname(), tempSessionId);

            Room room = roomService.createRoom(request.getRoomName(), creator);

            room.getGame().setWhitePlayer(null);

            log.info("Sala creada vía REST: {} con ID: {}", room.getName(), room.getId());

            CreateRoomResponse response = new CreateRoomResponse(
                    room.getId(),
                    room.getName(),
                    creator.getId(),
                    "Sala creada exitosamente. Esperando jugadores..."
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al crear sala vía REST", e);
            CreateRoomResponse errorResponse = new CreateRoomResponse(
                    null,
                    null,
                    null,
                    "Error al crear la sala: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Obtiene todas las salas disponibles
     */
    @GetMapping
    public ResponseEntity<List<RoomInfoResponse>> getAvailableRooms() {
        List<Room> rooms = roomService.getAvailableRooms();

        List<RoomInfoResponse> response = rooms.stream()
                .map(room -> new RoomInfoResponse(
                        room.getId(),
                        room.getName(),
                        room.getPlayerCount(),
                        room.getMaxPlayers(),
                        room.getGame().getStatus().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene información de una sala específica
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomInfoResponse> getRoomInfo(@PathVariable String roomId) {
        Room room = roomService.getRoom(roomId);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        RoomInfoResponse response = new RoomInfoResponse(
                room.getId(),
                room.getName(),
                room.getPlayerCount(),
                room.getMaxPlayers(),
                room.getGame().getStatus().toString()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica si una sala existe
     */
    @GetMapping("/{roomId}/exists")
    public ResponseEntity<Boolean> roomExists(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.roomExists(roomId));
    }
}