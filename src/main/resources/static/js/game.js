/**
 * Lógica del tablero de juego - Versión independiente
 */

(function() {
    const BOARD_SIZE = 8;
    const CELL_SIZE = 80;
    const PIECE_RADIUS = 30;

    let canvas;
    let ctx;
    let gameState = null;
    let selectedPiece = null;
    let validMoves = [];
    let myColor = null;
    let playerId = null;
    let gameStompClient = null;

    /**
     * Inicializa el juego
     */
    window.initGame = function(roomId, nickname) {
        console.log('=== INICIALIZANDO JUEGO ===');
        console.log('Room ID:', roomId);
        console.log('Nickname:', nickname);

        canvas = document.getElementById('gameBoard');
        if (!canvas) {
            console.error('Canvas no encontrado');
            return;
        }

        ctx = canvas.getContext('2d');
        playerId = sessionStorage.getItem('playerId');

        console.log('Canvas inicializado:', canvas.width, 'x', canvas.height);

        // Dibujar tablero inicial vacío
        drawEmptyBoard();

        // Configurar eventos del canvas
        canvas.addEventListener('click', handleCanvasClick);

        // Conectar WebSocket
        connectToGame(roomId, nickname);

        // Botón de abandonar
        document.getElementById('leaveGame').addEventListener('click', () => {
            if (confirm('¿Estás seguro de que quieres abandonar la partida?')) {
                if (gameStompClient) {
                    gameStompClient.disconnect();
                }
                window.location.href = '/';
            }
        });

        // Botón de volver al inicio desde modal de victoria
        document.getElementById('backToHome').addEventListener('click', () => {
            if (gameStompClient) {
                gameStompClient.disconnect();
            }
            window.location.href = '/';
        });

        // Desconectar al salir
        window.addEventListener('beforeunload', () => {
            if (gameStompClient) {
                gameStompClient.disconnect();
            }
        });
    };

    /**
     * Conecta al WebSocket del juego
     */
    function connectToGame(roomId, nickname) {
        console.log('=== CONECTANDO WEBSOCKET ===');

        const socket = new SockJS('/ws-checkers');
        gameStompClient = Stomp.over(socket);
        gameStompClient.debug = null;

        gameStompClient.connect({}, function(frame) {
            console.log('✅ WebSocket conectado');

            // Suscribirse a actualizaciones de la sala
            gameStompClient.subscribe('/topic/room/' + roomId, function(message) {
                console.log('📨 Mensaje en /topic/room/');
                const response = JSON.parse(message.body);
                handleRoomUpdate(response);
            });

            // Suscribirse a actualizaciones del juego
            gameStompClient.subscribe('/topic/game/' + roomId, function(message) {
                console.log('📨 Mensaje en /topic/game/');
                const newGameState = JSON.parse(message.body);
                console.log('Estado del juego recibido');

                // Ocultar overlay
                const overlay = document.getElementById('waitingOverlay');
                if (overlay) {
                    overlay.style.display = 'none';
                }

                onGameStateUpdate(newGameState);
            });

            // Suscribirse a errores
            gameStompClient.subscribe('/user/queue/errors', function(message) {
                const error = JSON.parse(message.body);
                console.error('❌ Error:', error);
                alert('Error: ' + error.message);
            });

            console.log('✅ Suscripciones completadas');

            // Unirse a la sala
            console.log('📤 Enviando solicitud de unión con nickname:', nickname);
            gameStompClient.send('/app/room/join', {}, JSON.stringify({
                roomId: roomId,
                playerNickname: nickname
            }));

        }, function(error) {
            console.error('❌ Error de conexión:', error);
            alert('Error al conectar. Por favor, recarga la página.');
        });
    }

    /**
     * Maneja actualizaciones de la sala
     */
    function handleRoomUpdate(response) {
        console.log('Actualización de sala:', response);

        if (response.gameStarted) {
            console.log('✅ Juego iniciado');
            const overlay = document.getElementById('waitingOverlay');
            if (overlay) {
                overlay.style.display = 'none';
            }
        }
    }

    /**
     * Dibuja un tablero vacío
     */
    function drawEmptyBoard() {
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                const x = col * CELL_SIZE;
                const y = row * CELL_SIZE;

                if ((row + col) % 2 === 0) {
                    ctx.fillStyle = '#f0d9b5';
                } else {
                    ctx.fillStyle = '#b58863';
                }

                ctx.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Maneja actualizaciones del estado del juego
     */
    function onGameStateUpdate(newGameState) {
        console.log('=== ACTUALIZANDO ESTADO ===');
        console.log('Estado completo:', newGameState);

        gameState = newGameState;

        // Actualizar información de jugadores
        if (gameState.whitePlayerNickname) {
            document.getElementById('whitePlayerName').textContent = gameState.whitePlayerNickname;
            console.log('Jugador blanco:', gameState.whitePlayerNickname);
        }
        if (gameState.blackPlayerNickname) {
            document.getElementById('blackPlayerName').textContent = gameState.blackPlayerNickname;
            console.log('Jugador negro:', gameState.blackPlayerNickname);
        }

        // Determinar mi color
        const nickname = sessionStorage.getItem('nickname');
        console.log('Mi nickname:', nickname);

        if (myColor === null) {
            if (gameState.whitePlayerNickname === nickname) {
                myColor = 'WHITE';
                console.log('✅ Soy BLANCO');
            } else if (gameState.blackPlayerNickname === nickname) {
                myColor = 'BLACK';
                console.log('✅ Soy NEGRO');
            } else {
                console.error('❌ No pude determinar mi color');
            }
        }

        console.log('Turno actual:', gameState.currentTurn);
        console.log('Mi color:', myColor);

        // Actualizar indicador de turno
        const isMyTurn = gameState.currentTurn === myColor;
        console.log('¿Es mi turno?', isMyTurn);

        document.getElementById('turnIndicator').textContent =
            `Turno de las ${gameState.currentTurn === 'WHITE' ? 'blancas' : 'negras'}`;

        document.getElementById('gameStatus').textContent =
            isMyTurn ? '¡Tu turno!' : 'Esperando al oponente...';

        // Verificar tablero
        if (gameState.board && gameState.board.grid) {
            console.log('✅ Board.grid existe');

            // Contar fichas
            let totalPieces = 0;
            for (let row = 0; row < BOARD_SIZE; row++) {
                if (gameState.board.grid[row]) {
                    for (let col = 0; col < BOARD_SIZE; col++) {
                        if (gameState.board.grid[row][col]) {
                            totalPieces++;
                        }
                    }
                }
            }
            console.log('Total fichas en tablero:', totalPieces);

            updateCapturedPieces(gameState.board);
        } else {
            console.error('❌ Board.grid no existe');
        }

        // Verificar si terminó
        if (gameState.status === 'FINISHED') {
            showWinModal(gameState.winnerNickname);
        } else if (gameState.status === 'ABANDONED') {
            alert('Partida abandonada');
            window.location.href = '/';
        }

        // Redibujar
        drawBoard();
        console.log('=== FIN ACTUALIZACIÓN ===');
    }

    /**
     * Actualiza fichas capturadas
     */
    function updateCapturedPieces(board) {
        let whiteCount = 0;
        let blackCount = 0;

        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                const piece = board.grid[row][col];
                if (piece) {
                    if (piece.color === 'WHITE') {
                        whiteCount++;
                    } else {
                        blackCount++;
                    }
                }
            }
        }

        document.getElementById('capturedWhite').innerHTML =
            `Capturadas ⚪: <strong>${12 - whiteCount}</strong>`;
        document.getElementById('capturedBlack').innerHTML =
            `Capturadas ⚫: <strong>${12 - blackCount}</strong>`;
    }

    /**
     * Dibuja el tablero
     */
    function drawBoard() {
        if (!gameState || !gameState.board || !gameState.board.grid) {
            console.log('No hay estado, dibujando tablero vacío');
            drawEmptyBoard();
            return;
        }

        console.log('Dibujando tablero con fichas...');

        // Limpiar
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Dibujar casillas
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                const x = col * CELL_SIZE;
                const y = row * CELL_SIZE;

                if ((row + col) % 2 === 0) {
                    ctx.fillStyle = '#f0d9b5';
                } else {
                    ctx.fillStyle = '#b58863';
                }

                ctx.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // Dibujar movimientos válidos
        if (validMoves.length > 0) {
            ctx.fillStyle = 'rgba(0, 255, 0, 0.3)';
            validMoves.forEach(move => {
                const x = move.toCol * CELL_SIZE;
                const y = move.toRow * CELL_SIZE;
                ctx.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            });
        }

        // Dibujar fichas
        let piecesDrawn = 0;
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                const piece = gameState.board.grid[row][col];
                if (piece) {
                    drawPiece(row, col, piece);
                    piecesDrawn++;
                }
            }
        }

        console.log('Fichas dibujadas:', piecesDrawn);

        // Resaltar pieza seleccionada
        if (selectedPiece) {
            const x = selectedPiece.col * CELL_SIZE + CELL_SIZE / 2;
            const y = selectedPiece.row * CELL_SIZE + CELL_SIZE / 2;

            ctx.strokeStyle = '#00ff00';
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.arc(x, y, PIECE_RADIUS + 5, 0, 2 * Math.PI);
            ctx.stroke();
        }
    }

    /**
     * Dibuja una ficha
     */
    function drawPiece(row, col, piece) {
        const x = col * CELL_SIZE + CELL_SIZE / 2;
        const y = row * CELL_SIZE + CELL_SIZE / 2;

        // Sombra
        ctx.shadowColor = 'rgba(0, 0, 0, 0.5)';
        ctx.shadowBlur = 5;
        ctx.shadowOffsetX = 2;
        ctx.shadowOffsetY = 2;

        // Color
        ctx.fillStyle = piece.color === 'WHITE' ? '#ffffff' : '#000000';
        ctx.beginPath();
        ctx.arc(x, y, PIECE_RADIUS, 0, 2 * Math.PI);
        ctx.fill();

        // Borde
        ctx.strokeStyle = piece.color === 'WHITE' ? '#cccccc' : '#333333';
        ctx.lineWidth = 2;
        ctx.stroke();

        // Resetear sombra
        ctx.shadowColor = 'transparent';

        // Corona si es dama
        if (piece.king) {
            ctx.fillStyle = piece.color === 'WHITE' ? '#ffd700' : '#ffff00';
            ctx.font = 'bold 30px Arial';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText('♔', x, y);
        }
    }

    /**
     * Maneja clicks
     */
    function handleCanvasClick(event) {
        if (!gameState || gameState.currentTurn !== myColor) {
            console.log('No es mi turno o no hay estado');
            return;
        }

        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        const col = Math.floor(x / CELL_SIZE);
        const row = Math.floor(y / CELL_SIZE);

        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }

        const clickedPiece = gameState.board.grid[row][col];

        // Si hay pieza seleccionada y click en movimiento válido
        if (selectedPiece && isValidMoveTarget(row, col)) {
            makeMove(row, col);
            return;
        }

        // Si click en nuestra ficha
        if (clickedPiece && clickedPiece.color === myColor) {
            selectPiece(row, col, clickedPiece);
        } else {
            // Deseleccionar
            selectedPiece = null;
            validMoves = [];
            drawBoard();
        }
    }

    /**
     * Selecciona ficha
     */
    function selectPiece(row, col, piece) {
        console.log('Ficha seleccionada:', piece);
        selectedPiece = { row, col, piece };
        validMoves = calculateValidMoves(piece);
        console.log('Movimientos válidos:', validMoves.length);
        drawBoard();
    }

    /**
     * Calcula movimientos válidos para una ficha
     */
    function calculateValidMoves(piece) {
        const moves = [];
        const directions = piece.king ?
            [{dr: -1, dc: -1}, {dr: -1, dc: 1}, {dr: 1, dc: -1}, {dr: 1, dc: 1}] :
            piece.color === 'WHITE' ?
                [{dr: -1, dc: -1}, {dr: -1, dc: 1}] :
                [{dr: 1, dc: -1}, {dr: 1, dc: 1}];

        // Primero, buscar capturas para ESTA ficha
        const capturesThisPiece = [];
        for (const dir of directions) {
            const jumpRow = piece.row + 2 * dir.dr;
            const jumpCol = piece.col + 2 * dir.dc;
            const middleRow = piece.row + dir.dr;
            const middleCol = piece.col + dir.dc;

            if (jumpRow >= 0 && jumpRow < BOARD_SIZE &&
                jumpCol >= 0 && jumpCol < BOARD_SIZE) {

                const middlePiece = gameState.board.grid[middleRow][middleCol];
                const targetPiece = gameState.board.grid[jumpRow][jumpCol];

                if (middlePiece && middlePiece.color !== piece.color && !targetPiece) {
                    capturesThisPiece.push({
                        fromRow: piece.row,
                        fromCol: piece.col,
                        toRow: jumpRow,
                        toCol: jumpCol,
                        capture: true,
                        capturedPosition: { row: middleRow, col: middleCol }
                    });
                }
            }
        }

        // Verificar si HAY CAPTURAS DISPONIBLES para CUALQUIER ficha de mi color
        const allCaptures = getAllAvailableCaptures(piece.color);

        // Si HAY capturas disponibles (de esta u otra ficha)
        if (allCaptures.length > 0) {
            // Solo devolver capturas de ESTA ficha
            return capturesThisPiece;
        }

        // Si no hay capturas obligatorias, permitir movimientos simples
        for (const dir of directions) {
            const newRow = piece.row + dir.dr;
            const newCol = piece.col + dir.dc;

            if (newRow >= 0 && newRow < BOARD_SIZE &&
                newCol >= 0 && newCol < BOARD_SIZE) {

                const targetPiece = gameState.board.grid[newRow][newCol];

                if (!targetPiece) {
                    moves.push({
                        fromRow: piece.row,
                        fromCol: piece.col,
                        toRow: newRow,
                        toCol: newCol,
                        capture: false,
                        capturedPosition: null
                    });
                }
            }
        }

        return moves;
    }

    /**
     * Obtiene todas las capturas disponibles para un color
     */
    function getAllAvailableCaptures(color) {
        const allCaptures = [];

        if (!gameState || !gameState.board || !gameState.board.grid) {
            return allCaptures;
        }

        // Recorrer todas las fichas del color especificado
        for (let row = 0; row < BOARD_SIZE; row++) {
            for (let col = 0; col < BOARD_SIZE; col++) {
                const piece = gameState.board.grid[row][col];

                if (piece && piece.color === color) {
                    // Verificar capturas posibles para esta ficha
                    const directions = piece.king ?
                        [{dr: -1, dc: -1}, {dr: -1, dc: 1}, {dr: 1, dc: -1}, {dr: 1, dc: 1}] :
                        piece.color === 'WHITE' ?
                            [{dr: -1, dc: -1}, {dr: -1, dc: 1}] :
                            [{dr: 1, dc: -1}, {dr: 1, dc: 1}];

                    for (const dir of directions) {
                        const jumpRow = piece.row + 2 * dir.dr;
                        const jumpCol = piece.col + 2 * dir.dc;
                        const middleRow = piece.row + dir.dr;
                        const middleCol = piece.col + dir.dc;

                        if (jumpRow >= 0 && jumpRow < BOARD_SIZE &&
                            jumpCol >= 0 && jumpCol < BOARD_SIZE) {

                            const middlePiece = gameState.board.grid[middleRow][middleCol];
                            const targetPiece = gameState.board.grid[jumpRow][jumpCol];

                            if (middlePiece && middlePiece.color !== piece.color && !targetPiece) {
                                allCaptures.push({
                                    fromRow: piece.row,
                                    fromCol: piece.col,
                                    toRow: jumpRow,
                                    toCol: jumpCol,
                                    capture: true,
                                    capturedPosition: { row: middleRow, col: middleCol }
                                });
                            }
                        }
                    }
                }
            }
        }

        return allCaptures;
    }

    /**
     * Verifica si destino es válido
     */
    function isValidMoveTarget(row, col) {
        return validMoves.some(move => move.toRow === row && move.toCol === col);
    }

    /**
     * Realiza movimiento
     */
    function makeMove(toRow, toCol) {
        const move = validMoves.find(m => m.toRow === toRow && m.toCol === toCol);

        if (!move) {
            console.error('No se encontró el movimiento en validMoves');
            return;
        }

        console.log('=== ENVIANDO MOVIMIENTO ===');
        console.log('Movimiento completo:', move);
        console.log('De:', move.fromRow, move.fromCol);
        console.log('A:', move.toRow, move.toCol);
        console.log('Es captura:', move.capture);
        console.log('Posición capturada:', move.capturedPosition);

        const moveRequest = {
            roomId: gameState.roomId,
            playerId: playerId,
            move: move
        };

        console.log('Request completo:', JSON.stringify(moveRequest, null, 2));

        gameStompClient.send('/app/game/move', {}, JSON.stringify(moveRequest));

        selectedPiece = null;
        validMoves = [];
    }

    /**
     * Modal de victoria
     */
    function showWinModal(winnerNickname) {
        const modal = document.getElementById('winModal');
        const title = document.getElementById('winTitle');
        const message = document.getElementById('winMessage');

        const myNickname = sessionStorage.getItem('nickname');
        const iWon = winnerNickname === myNickname;

        title.textContent = iWon ? '🎉 ¡Victoria!' : '😔 Derrota';
        message.textContent = iWon ?
            '¡Felicidades! Has ganado la partida.' :
            `${winnerNickname} ha ganado la partida.`;

        modal.style.display = 'block';
    }
})();