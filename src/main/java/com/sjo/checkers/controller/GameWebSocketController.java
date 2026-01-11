package com.sjo.checkers.controller;

import com.sjo.checkers.dto.*;
import com.sjo.checkers.model.*;
import com.sjo.checkers.service.GameService;
import com.sjo.checkers.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador WebSocket para manejar los mensajes del juego
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketController {

    private final GameService gameService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    // Mapa para rastrear qué sessionId está en qué sala
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    /**
     * Maneja cuando un jugador se une a una sala
     */
    @MessageMapping("/room/join")
    public void joinRoom(@Payload JoinRoomRequest request,SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            log.info("Jugador {} (session: {}) intentando unirse a sala {}",
                    request.getPlayerNickname(), sessionId, request.getRoomId());

            if (!roomService.roomExists(request.getRoomId())) {
                log.warn("Sala no existe: {}", request.getRoomId());
                sendErrorToSession(sessionId, "La sala no existe");
                return;
            }

            Room room = roomService.getRoom(request.getRoomId());

            // Verificar si esta sesión ya está en la sala
            if (sessionToRoom.containsKey(sessionId) && sessionToRoom.get(sessionId).equals(request.getRoomId())) {
                log.info("Sesión {} ya está en la sala {}, enviando estado actual",sessionId, request.getRoomId());

                // Si la sala está llena, enviar estado del juego
                if (room.isFull()) {
                    log.info("Sala llena, enviando estado del juego a sesión reconectada");
                    sendGameState(room);
                } else {
                    sendRoomUpdate(room, "Reconectado a la sala");
                }
                return;
            }

            if (room.isFull()) {
                log.warn("Sala llena: {}", request.getRoomId());
                sendErrorToSession(sessionId, "La sala está llena");
                return;
            }

            Player player = new Player(request.getPlayerNickname(), sessionId);
            boolean joined = roomService.joinRoom(request.getRoomId(), player);

            if (joined) {
                // Registrar que esta sesión se unió a esta sala
                sessionToRoom.put(sessionId, room.getId());

                log.info("Jugador {} se unió exitosamente a sala {} (Total: {}/2)",player.getNickname(), room.getId(), room.getPlayerCount());

                // Notificar a todos en la sala sobre el nuevo jugador
                PlayerJoinedResponse response = new PlayerJoinedResponse(
                        room.getId(),
                        player.getNickname(),
                        player.getColor().toString(),
                        room.getPlayerCount(),
                        room.isFull(),
                        player.getNickname() + " se ha unido a la sala"
                );

                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);

                // Si la sala está llena, iniciar el juego CON UN PEQUEÑO DELAY
                if (room.isFull()) {
                    log.info("Sala {} completa con {} jugadores, enviando estado del juego",room.getId(), room.getPlayerCount());

                    // Enviar el estado del juego inmediatamente
                    sendGameState(room);
                }
            } else {
                sendErrorToSession(sessionId, "No se pudo unir a la sala");
            }
        } catch (Exception e) {
            log.error("Error al unirse a sala", e);
            sendErrorToSession(headerAccessor.getSessionId(),"Error al unirse: " + e.getMessage());
        }
    }

    /**
     * Envía actualización de sala
     */
    private void sendRoomUpdate(Room room, String message) {
        Game game = room.getGame();

        PlayerJoinedResponse response = new PlayerJoinedResponse(
                room.getId(),
                "", // nickname
                "", // color
                room.getPlayerCount(),
                room.isFull(),
                message
        );

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
    }

    /**
     * Maneja un movimiento
     */
    @MessageMapping("/game/move")
    public void makeMove(@Payload MoveRequest request,SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            log.info("Movimiento recibido para sala {} de sesión {}",request.getRoomId(), sessionId);

            Room room = roomService.getRoom(request.getRoomId());

            if (room == null) {
                log.warn("Sala no encontrada: {}", request.getRoomId());
                return;
            }

            // Obtener el jugador por sessionId
            Player player = roomService.getPlayerBySessionId(request.getRoomId(), sessionId);
            if (player == null) {
                log.warn("Jugador no encontrado en sala {}", request.getRoomId());
                sendErrorToSession(sessionId, "No estás en esta sala");
                return;
            }

            Game game = room.getGame();

            // Verificar que es el turno del jugador
            if (!game.isPlayerTurn(player)) {
                log.warn("No es el turno del jugador {} en sala {}",player.getNickname(), request.getRoomId());
                sendErrorToSession(sessionId, "No es tu turno");
                return;
            }

            // Ejecutar el movimiento
            boolean valid = gameService.executeMove(game, request.getMove());

            if (valid) {
                log.info("Movimiento válido ejecutado en sala {} por {}",request.getRoomId(), player.getNickname());
                sendGameState(room);
            } else {
                log.warn("Movimiento inválido en sala {} por {}",request.getRoomId(), player.getNickname());
                sendErrorToSession(sessionId, "Movimiento inválido");
            }

        } catch (Exception e) {
            log.error("Error al ejecutar movimiento", e);
            sendErrorToSession(headerAccessor.getSessionId(),"Error al ejecutar movimiento: " + e.getMessage());
        }
    }

    /**
     * Envía el estado del juego a todos los jugadores de una sala
     */
    private void sendGameState(Room room) {
        Game game = room.getGame();

        log.info("=== ENVIANDO ESTADO DEL JUEGO ===");
        log.info("Sala: {}", room.getId());
        log.info("Jugador Blanco: {}", game.getWhitePlayer() != null ? game.getWhitePlayer().getNickname() : "null");
        log.info("Jugador Negro: {}", game.getBlackPlayer() != null ? game.getBlackPlayer().getNickname() : "null");
        log.info("Turno actual: {}", game.getCurrentTurn());
        log.info("Estado: {}", game.getStatus());

        // Contar fichas en el tablero
        int whiteCount = game.getBoard().getPiecesByColor(PieceColor.WHITE).size();
        int blackCount = game.getBoard().getPiecesByColor(PieceColor.BLACK).size();
        log.info("Fichas blancas: {}, Fichas negras: {}", whiteCount, blackCount);

        GameStateResponse response = new GameStateResponse(
                room.getId(),
                game.getBoard(),
                game.getCurrentTurn(),
                game.getStatus(),
                game.getWhitePlayer() != null ? game.getWhitePlayer().getNickname() : null,
                game.getBlackPlayer() != null ? game.getBlackPlayer().getNickname() : null,
                game.getWinner() != null ? game.getWinner().getNickname() : null,
                game.getStatus() == GameStatus.IN_PROGRESS ?
                        "Turno de " + (game.getCurrentTurn() == PieceColor.WHITE ? "blancas" : "negras") :
                        game.getStatus() == GameStatus.FINISHED ?
                                "¡Juego terminado! Ganador: " + game.getWinner().getNickname() : ""
        );

        log.info("Enviando estado a /topic/game/{}", room.getId());
        messagingTemplate.convertAndSend("/topic/game/" + room.getId(), response);
        log.info("=== ESTADO ENVIADO ===");
    }

    /**
     * Envía un error a una sesión específica
     */
    private void sendErrorToSession(String sessionId, String errorMessage) {
        ErrorResponse error = new ErrorResponse("ERROR", errorMessage);
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error
        );
    }
}