package com.sjo.checkers.controller;

import com.sjo.checkers.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para servir las vistas HTML
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final RoomService roomService;

    /**
     * Página principal (index)
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("availableRooms", roomService.getAvailableRooms());
        return "index";
    }

    /**
     * Página de sala de espera - redirige al juego
     */
    @GetMapping("/room/{roomId}/lobby")
    public String roomLobby(@PathVariable String roomId,@RequestParam(required = false) String nickname) {
        if (!roomService.roomExists(roomId)) {
            return "redirect:/?error=room_not_found";
        }

        // Redirigir al juego con el nickname
        if (nickname != null && !nickname.isEmpty()) {
            return "redirect:/room/" + roomId + "/game?nickname=" + nickname;
        } else {
            return "redirect:/room/" + roomId + "/game";
        }
    }

    /**
     * Página del juego
     */
    @GetMapping("/room/{roomId}/game")
    public String game(@PathVariable String roomId,@RequestParam(required = false) String nickname, Model model) {
        if (!roomService.roomExists(roomId)) {
            return "redirect:/?error=room_not_found";
        }

        model.addAttribute("roomId", roomId);

        // Si hay nickname en la URL, pasarlo al modelo
        if (nickname != null && !nickname.isEmpty()) {
            model.addAttribute("nickname", nickname);
        }

        return "game";
    }
}