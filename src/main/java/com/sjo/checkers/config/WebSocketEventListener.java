package com.sjo.checkers.config;

import com.sjo.checkers.model.GameStatus;
import com.sjo.checkers.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Manejador de eventos de conexión/desconexión WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final RoomService roomService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("Nueva conexión WebSocket: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("Desconexión WebSocket: {}", sessionId);

        handlePlayerDisconnection(sessionId);
    }

    private void handlePlayerDisconnection(String sessionId) {
        // Buscar en todas las salas si el jugador estaba en alguna
        for (var room : roomService.getAvailableRooms()) {
            var game = room.getGame();
            boolean wasInRoom = false;

            if (game.getWhitePlayer() != null &&
                    game.getWhitePlayer().getSessionId().equals(sessionId)) {
                wasInRoom = true;
                log.info("Jugador blanco {} desconectado de la sala {}",
                        game.getWhitePlayer().getNickname(), room.getId());
            }

            if (game.getBlackPlayer() != null &&
                    game.getBlackPlayer().getSessionId().equals(sessionId)) {
                wasInRoom = true;
                log.info("Jugador negro {} desconectado de la sala {}",
                        game.getBlackPlayer().getNickname(), room.getId());
            }

            if (wasInRoom && game.getStatus() == GameStatus.IN_PROGRESS) {
                game.setStatus(GameStatus.ABANDONED);
                log.info("Sala {} marcada como abandonada", room.getId());
            }
        }
    }
}