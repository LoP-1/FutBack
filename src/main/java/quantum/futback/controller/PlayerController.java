package quantum.futback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import quantum.futback.entity.Player;
import quantum.futback.entity.DTO.PlayerRequest;
import quantum.futback.services.PlayerService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Player> createPlayer(
            @RequestPart("data") PlayerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile) {

        // Asocia el archivo al DTO antes de pasarlo al servicio
        request.setPhotoFile(photoFile);

        Player createdPlayer = playerService.createPlayer(request);
        return new ResponseEntity<>(createdPlayer, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers(@RequestParam(required = false) String name) {
        // Nota: Si se requiere filtrado real, el PlayerService debe implementarlo
        // usando el parámetro 'name' y métodos de repositorio como findByFirstNameContainingIgnoreCase
        List<Player> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable UUID id) {
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Player> updatePlayer(
            @PathVariable UUID id,
            @RequestPart("data") PlayerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile) {

        request.setPhotoFile(photoFile);
        Player updatedPlayer = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(updatedPlayer);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Player> updatePlayerStatus(@PathVariable UUID id, @RequestParam boolean isActive) {
        Player player = playerService.updatePlayerStatus(id, isActive);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<Map<String, Object>> getPlayerDashboard(@PathVariable UUID id) {
        Map<String, Object> dashboardData = playerService.getPlayerDashboard(id);
        return ResponseEntity.ok(dashboardData);
    }
}