package com.sjo.checkers.scheduler;

import com.sjo.checkers.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas para mantenimiento
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final RoomService roomService;

    /**
     * Limpia salas vacías cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void cleanupEmptyRooms() {
        log.info("Ejecutando limpieza de salas vacías...");
        roomService.removeEmptyRooms();
    }
}