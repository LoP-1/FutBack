package quantum.futback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import quantum.futback.entity.Player;
import quantum.futback.entity.DTO.PlayerRequest;
import quantum.futback.entity.DTO.StatusUpdateRequest;
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Player> createPlayer(
            @RequestPart("data") PlayerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile) {

        // Asocia el archivo al DTO antes de pasarlo al servicio
        request.setPhotoFile(photoFile);

        Player createdPlayer = playerService.createPlayer(request);
        return new ResponseEntity<>(createdPlayer, HttpStatus.CREATED);
    }

    /**
     * GET /api/players: Buscador de jugadores.
     * Filtros: ?teamId={id}&name={txt}&dni={txt}
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dni) {
        List<Player> players = playerService.getAllPlayers(teamId, name, dni);
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Player> getPlayerById(@PathVariable UUID id) {
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable UUID id,
            @RequestPart("data") PlayerRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile) {

        request.setPhotoFile(photoFile);
        Player updatedPlayer = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(updatedPlayer);
    }

    /**
     * PUT /api/players/{id}/status: Activa/Desactiva jugador (Baja de la academia).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Player> updatePlayerStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest request) {
        Player player = playerService.updatePlayerStatus(id, Boolean.TRUE.equals(request.getIsActive()));
        return ResponseEntity.ok(player);
    }

    /**
     * GET /api/players/{id}/dashboard: Retorna JSON agregado para el perfil del jugador.
     */
    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Map<String, Object>> getPlayerDashboard(@PathVariable UUID id) {
        Map<String, Object> dashboardData = playerService.getPlayerDashboard(id);
        return ResponseEntity.ok(dashboardData);
    }
}