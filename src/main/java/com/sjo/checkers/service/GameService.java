package com.sjo.checkers.service;

import com.sjo.checkers.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que implementa la lógica del juego de damas
 */
@Service
@Slf4j
public class GameService {

    /**
     * Valida y ejecuta un movimiento
     */
    public boolean executeMove(Game game, Move move) {
        Board board = game.getBoard();
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());

        log.info("=== EJECUTANDO MOVIMIENTO ===");
        log.info("De: ({},{}) a ({},{})",move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
        log.info("Es captura según move: {}", move.isCapture());

        // Validaciones básicas
        if (piece == null) {
            log.warn("No hay ficha en la posición de origen");
            return false;
        }

        log.info("Ficha: color={}, king={}, pos=({},{})",piece.getColor(), piece.isKing(), piece.getRow(), piece.getCol());

        // IMPORTANTE: Verificar que sea el turno del jugador
        if (piece.getColor() != game.getCurrentTurn()) {
            log.warn("No es el turno de este jugador. Turno actual: {}, color de ficha: {}", game.getCurrentTurn(), piece.getColor());
            return false;
        }

        // Verificar si el movimiento es válido
        log.info("Validando movimiento...");
        if (!isValidMove(board, piece, move)) {
            log.warn("Movimiento no válido");
            return false;
        }

        log.info("✅ Movimiento válido, ejecutando...");

        // Ejecutar el movimiento
        board.removePiece(move.getFromRow(), move.getFromCol());
        board.setPiece(move.getToRow(), move.getToCol(), piece);

        // Si es una captura, eliminar la ficha capturada
        if (move.isCapture() && move.getCapturedPosition() != null) {
            log.info("Eliminando ficha capturada en ({},{})",
                    move.getCapturedPosition().getRow(),
                    move.getCapturedPosition().getCol());
            board.removePiece(
                    move.getCapturedPosition().getRow(),
                    move.getCapturedPosition().getCol()
            );

            // Verificar si hay más capturas disponibles con la misma ficha
            if (!hasMoreCaptures(board, piece)) {
                checkAndPromoteToKing(board, piece);
                game.switchTurn();
                log.info("Turno cambiado a: {}", game.getCurrentTurn());
            } else {
                log.info("Hay más capturas disponibles, no se cambia el turno");
            }
        } else {
            checkAndPromoteToKing(board, piece);
            game.switchTurn();
            log.info("Turno cambiado a: {}", game.getCurrentTurn());
        }

        // Verificar si hay un ganador
        checkWinCondition(game);

        log.info("=== MOVIMIENTO EJECUTADO EXITOSAMENTE ===");
        return true;
    }

    /**
     * Valida si un movimiento es legal
     */
    public boolean isValidMove(Board board, Piece piece, Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        // Verificar que el destino esté dentro del tablero
        if (!board.isValidPosition(toRow, toCol)) {
            return false;
        }

        // Verificar que el destino esté vacío
        if (board.getPiece(toRow, toCol) != null) {
            return false;
        }

        // Verificar que se mueva en diagonal
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff != colDiff) {
            return false;
        }

        // Si hay capturas obligatorias disponibles, el jugador debe capturar
        List<Move> captures = getAvailableCaptures(board, piece.getColor());
        if (!captures.isEmpty() && !move.isCapture()) {
            return false;
        }

        // Movimiento simple (1 casilla)
        if (rowDiff == 1) {
            return isValidSimpleMove(piece, fromRow, toRow);
        }

        // Captura (2 casillas)
        if (rowDiff == 2) {
            return isValidCapture(board, piece, move);
        }

        return false;
    }

    /**
     * Verifica si un movimiento simple es válido
     */
    private boolean isValidSimpleMove(Piece piece, int fromRow, int toRow) {
        if (piece.isKing()) {
            return true;
        }

        // Fichas blancas se mueven hacia arriba (row decrece)
        if (piece.getColor() == PieceColor.WHITE) {
            return toRow < fromRow;
        }

        // Fichas negras se mueven hacia abajo (row crece)
        return toRow > fromRow;
    }

    /**
     * Verifica si una captura es válida
     */
    private boolean isValidCapture(Board board, Piece piece, Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        log.debug("Validando captura: de ({},{}) a ({},{})", fromRow, fromCol, toRow, toCol);

        // Calcular la posición de la ficha a capturar
        int capturedRow = (fromRow + toRow) / 2;
        int capturedCol = (fromCol + toCol) / 2;

        log.debug("Posición de captura calculada: ({},{})", capturedRow, capturedCol);

        Piece capturedPiece = board.getPiece(capturedRow, capturedCol);

        // Debe haber una ficha del oponente en el medio
        if (capturedPiece == null) {
            log.warn("No hay ficha en la posición de captura ({},{})", capturedRow, capturedCol);
            return false;
        }

        if (capturedPiece.getColor() == piece.getColor()) {
            log.warn("La ficha a capturar es del mismo color");
            return false;
        }

        log.debug("Ficha a capturar: color={}, pos=({},{})",
                capturedPiece.getColor(), capturedRow, capturedCol);

        // Si no es una dama, verificar dirección
        if (!piece.isKing()) {
            if (piece.getColor() == PieceColor.WHITE && toRow > fromRow) {
                log.warn("Ficha blanca intentando capturar hacia atrás");
                return false;
            }
            if (piece.getColor() == PieceColor.BLACK && toRow < fromRow) {
                log.warn("Ficha negra intentando capturar hacia atrás");
                return false;
            }
        }

        // Actualizar el movimiento con la posición capturada
        move.setCapture(true);
        move.setCapturedPosition(new Move.Position(capturedRow, capturedCol));

        log.debug("Captura válida confirmada");
        return true;
    }

    /**
     * Obtiene todos los movimientos válidos para una ficha
     */
    public List<Move> getValidMoves(Board board, Piece piece) {
        List<Move> moves = new ArrayList<>();

        // Primero buscar capturas
        List<Move> captures = getValidCapturesForPiece(board, piece);
        if (!captures.isEmpty()) {
            return captures;
        }

        // Si no hay capturas, buscar movimientos simples
        int[] directions = piece.isKing() ?
                new int[]{-1, 1} : // Damas pueden ir en ambas direcciones
                (piece.getColor() == PieceColor.WHITE ? new int[]{-1} : new int[]{1}); // Normal solo adelante

        for (int rowDir : directions) {
            for (int colDir : new int[]{-1, 1}) {
                int newRow = piece.getRow() + rowDir;
                int newCol = piece.getCol() + colDir;

                Move move = new Move(piece.getRow(), piece.getCol(), newRow, newCol, false, null);
                if (isValidMove(board, piece, move)) {
                    moves.add(move);
                }
            }
        }

        return moves;
    }

    /**
     * Obtiene todas las capturas disponibles para un color
     */
    public List<Move> getAvailableCaptures(Board board, PieceColor color) {
        List<Move> allCaptures = new ArrayList<>();
        List<Piece> pieces = board.getPiecesByColor(color);

        log.debug("Buscando capturas para color: {}, fichas: {}", color, pieces.size());

        for (Piece piece : pieces) {
            // Obtener capturas válidas para esta ficha SIN validar el movimiento completo
            List<Move> capturesForPiece = getValidCapturesForPiece(board, piece);
            allCaptures.addAll(capturesForPiece);
        }

        log.debug("Total capturas encontradas: {}", allCaptures.size());
        return allCaptures;
    }

    /**
     * Obtiene capturas válidas para una ficha específica (sin validación completa de movimiento)
     */
    private List<Move> getValidCapturesForPiece(Board board, Piece piece) {
        List<Move> captures = new ArrayList<>();

        // Determinar direcciones según si es dama o no
        int[][] directions;
        if (piece.isKing()) {
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else if (piece.getColor() == PieceColor.WHITE) {
            directions = new int[][]{{-1, -1}, {-1, 1}}; // Blancas hacia arriba
        } else {
            directions = new int[][]{{1, -1}, {1, 1}}; // Negras hacia abajo
        }

        for (int[] dir : directions) {
            int jumpRow = piece.getRow() + (2 * dir[0]);
            int jumpCol = piece.getCol() + (2 * dir[1]);
            int middleRow = piece.getRow() + dir[0];
            int middleCol = piece.getCol() + dir[1];

            // Verificar que el salto esté dentro del tablero
            if (!board.isValidPosition(jumpRow, jumpCol)) {
                continue;
            }

            // Verificar que haya una ficha enemiga en el medio
            Piece middlePiece = board.getPiece(middleRow, middleCol);
            if (middlePiece == null || middlePiece.getColor() == piece.getColor()) {
                continue;
            }

            // Verificar que el destino esté vacío
            Piece targetPiece = board.getPiece(jumpRow, jumpCol);
            if (targetPiece != null) {
                continue;
            }

            // Es una captura válida
            Move captureMove = new Move(
                    piece.getRow(),
                    piece.getCol(),
                    jumpRow,
                    jumpCol,
                    true,
                    new Move.Position(middleRow, middleCol)
            );

            captures.add(captureMove);
        }

        return captures;
    }

    /**
     * Verifica si una ficha tiene más capturas disponibles
     */
    private boolean hasMoreCaptures(Board board, Piece piece) {
        return !getValidCapturesForPiece(board, piece).isEmpty();
    }

    /**
     * Verifica y convierte una ficha en dama si llega al final
     */
    private void checkAndPromoteToKing(Board board, Piece piece) {
        if (piece.isKing()) {
            return;
        }

        // Fichas blancas se coronan en la fila 0
        if (piece.getColor() == PieceColor.WHITE && piece.getRow() == 0) {
            piece.setKing(true);
        }

        // Fichas negras se coronan en la fila 7
        if (piece.getColor() == PieceColor.BLACK && piece.getRow() == 7) {
            piece.setKing(true);
        }
    }

    /**
     * Verifica si hay un ganador
     */
    private void checkWinCondition(Game game) {
        Board board = game.getBoard();

        PieceColor currentColor = game.getCurrentTurn();
        List<Piece> currentPieces = board.getPiecesByColor(currentColor);

        // Si no quedan fichas, el otro jugador gana
        if (currentPieces.isEmpty()) {
            game.setStatus(GameStatus.FINISHED);
            Player winner = (currentColor == PieceColor.WHITE) ?
                    game.getBlackPlayer() : game.getWhitePlayer();
            game.setWinner(winner);
            return;
        }

        // Si no hay movimientos válidos, el jugador pierde (está bloqueado)
        boolean hasValidMoves = false;
        for (Piece piece : currentPieces) {
            if (!getValidMoves(board, piece).isEmpty()) {
                hasValidMoves = true;
                break;
            }
        }

        if (!hasValidMoves) {
            game.setStatus(GameStatus.FINISHED);
            Player winner = (currentColor == PieceColor.WHITE) ?
                    game.getBlackPlayer() : game.getWhitePlayer();
            game.setWinner(winner);
        }
    }

    /**
     * Verifica si el juego ha terminado
     */
    public boolean isGameOver(Game game) {
        return game.getStatus() == GameStatus.FINISHED ||
                game.getStatus() == GameStatus.ABANDONED;
    }
}