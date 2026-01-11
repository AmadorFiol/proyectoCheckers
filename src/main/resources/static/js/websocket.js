/**
 * Módulo para manejo de WebSocket
 */

let stompClient = null;
let currentRoomId = null;

/**
 * Conecta al servidor WebSocket
 */
function connectWebSocket(onConnected) {
    const socket = new SockJS('/ws-checkers');
    stompClient = Stomp.over(socket);

    // Desactivar logs de debug (opcional)
    stompClient.debug = null;

    stompClient.connect({}, function(frame) {
        console.log('WebSocket conectado:', frame);
        if (onConnected) {
            onConnected();
        }
    }, function(error) {
        console.error('Error de conexión WebSocket:', error);
        alert('Error al conectar con el servidor. Por favor, recarga la página.');
    });
}

/**
 * Desconecta del servidor WebSocket
 */
function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log('WebSocket desconectado');
    }
}

/**
 * Crea una nueva sala - Usa HTTP REST
 */
async function createRoom(roomName, nickname) {
    try {
        const response = await fetch('/api/rooms/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                roomName: roomName,
                playerNickname: nickname
            })
        });

        if (!response.ok) {
            throw new Error('Error al crear la sala');
        }

        const data = await response.json();

        if (data.roomId) {
            console.log('Sala creada:', data);
            sessionStorage.setItem('nickname', nickname);

            // Redirigir directamente al juego
            window.location.href = `/room/${data.roomId}/game?nickname=${encodeURIComponent(nickname)}`;
        } else {
            throw new Error(data.message || 'Error al crear la sala');
        }
    } catch (error) {
        throw error;
    }
}

/**
 * Se une a una sala existente
 */
function joinRoom(roomId, nickname, onPlayerJoined, onGameStarted) {
    currentRoomId = roomId;

    connectWebSocket(() => {
        // Suscribirse a actualizaciones de la sala
        stompClient.subscribe('/topic/room/' + roomId, function(message) {
            const response = JSON.parse(message.body);
            console.log('Actualización de sala:', response);

            if (onPlayerJoined) {
                onPlayerJoined(response);
            }

            if (response.gameStarted && onGameStarted) {
                onGameStarted(response);
            }
        });

        // Suscribirse a errores
        stompClient.subscribe('/user/queue/errors', function(message) {
            const error = JSON.parse(message.body);
            console.error('Error recibido:', error);
            alert('Error: ' + error.message);
        });

        // Enviar solicitud de unión
        stompClient.send('/app/room/join', {}, JSON.stringify({
            roomId: roomId,
            playerNickname: nickname
        }));
    });
}

/**
 * Suscribirse a actualizaciones del juego
 */
function subscribeToGame(roomId, onGameUpdate) {
    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket no está conectado');
        return;
    }

    stompClient.subscribe('/topic/game/' + roomId, function(message) {
        const gameState = JSON.parse(message.body);
        console.log('Estado del juego actualizado:', gameState);

        if (onGameUpdate) {
            onGameUpdate(gameState);
        }
    });
}

/**
 * Envía un movimiento al servidor
 */
function sendMove(roomId, playerId, move) {
    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket no está conectado');
        return;
    }

    const moveRequest = {
        roomId: roomId,
        playerId: playerId,
        move: move
    };

    console.log('Enviando movimiento:', moveRequest);
    stompClient.send('/app/game/move', {}, JSON.stringify(moveRequest));
}

/**
 * Verifica si WebSocket está conectado
 */
function isWebSocketConnected() {
    return stompClient !== null && stompClient.connected;
}