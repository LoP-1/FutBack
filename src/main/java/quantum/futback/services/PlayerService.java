package quantum.futback.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.*;
import quantum.futback.entity.DTO.PlayerRequest;
import quantum.futback.repository.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final ImagenService imagenService;

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository, PositionRepository positionRepository, UserService userService, ImagenService imagenService) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.imagenService = imagenService;
    }

    @Transactional
    public Player createPlayer(PlayerRequest request) {

        // 1. Validación de edad y padre
        if (request.getBirthDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birth date is required.");
        }

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();
        boolean isMinor = age < 18;

        User parentUser = null;
        if (isMinor) {
            if (request.getParentDni() == null || request.getParentFullName() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Minor players require parent DNI and full name."
                );
            }
            // 2. Búsqueda/creación de usuario PARENT (Criterio de aceptación)
            parentUser = userService.findOrCreateParentUser(request.getParentDni(), request.getParentFullName());
        }

        // 3. Subida de foto (Manejo de Multipart)
        String photoUrl = null;
        if (request.getPhotoFile() != null && !request.getPhotoFile().isEmpty()) {
            photoUrl = imagenService.uploadImage(request.getPhotoFile());
        }

        // 4. Mapeo y persistencia
        Player player = new Player();
        player.setTenantId(TenantContext.getTenantId());

        // Mapeo de relaciones (asumiendo que las repositorios buscan por ID)
        if (request.getTeamId() != null) {
            player.setTeam(teamRepository.findById(request.getTeamId()).orElse(null));
        }
        if (request.getPositionPrimaryId() != null) {
            player.setPositionPrimary(positionRepository.findById(request.getPositionPrimaryId()).orElse(null));
        }
        if (request.getPositionSecondaryId() != null) {
            player.setPositionSecondary(positionRepository.findById(request.getPositionSecondaryId()).orElse(null));
        }

        player.setParentUser(parentUser); // Asociación parent_user_id

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setBirthDate(request.getBirthDate());
        player.setDni(request.getDni());
        player.setDominantFoot(request.getDominantFoot());
        player.setJerseyNumber(request.getJerseyNumber());
        player.setPhotoUrl(photoUrl); // Ruta relativa
        player.setIsActive(true);

        return playerRepository.save(player);
    }

    // ---------------------- BACK-4.4: GET/PUT -------------------------

    /**
     * GET /api/players
     * Filtros: teamId, name, dni
     */
    public List<Player> getAllPlayers(UUID teamId, String name, String dni) {
        // Use database-level filtering for better performance
        boolean hasFilters = teamId != null || (name != null && !name.isEmpty()) || (dni != null && !dni.isEmpty());
        
        if (hasFilters) {
            return playerRepository.findByFilters(
                    teamId,
                    (name != null && !name.isEmpty()) ? name : null,
                    (dni != null && !dni.isEmpty()) ? dni : null
            );
        }
        return playerRepository.findAll();
    }

    /**
     * GET /api/players/{id}
     */
    public Player getPlayerById(UUID id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    /**
     * PUT /api/players/{id}
     */
    @Transactional
    public Player updatePlayer(UUID id, PlayerRequest request) {
        Player existingPlayer = getPlayerById(id);

        // Update fields from request
        if (request.getFirstName() != null) {
            existingPlayer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingPlayer.setLastName(request.getLastName());
        }
        if (request.getBirthDate() != null) {
            existingPlayer.setBirthDate(request.getBirthDate());
        }
        if (request.getDni() != null) {
            existingPlayer.setDni(request.getDni());
        }
        if (request.getDominantFoot() != null) {
            existingPlayer.setDominantFoot(request.getDominantFoot());
        }
        if (request.getJerseyNumber() != null) {
            existingPlayer.setJerseyNumber(request.getJerseyNumber());
        }
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
            existingPlayer.setTeam(team);
        }
        if (request.getPositionPrimaryId() != null) {
            Position position = positionRepository.findById(request.getPositionPrimaryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Primary position not found"));
            existingPlayer.setPositionPrimary(position);
        }
        if (request.getPositionSecondaryId() != null) {
            Position position = positionRepository.findById(request.getPositionSecondaryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secondary position not found"));
            existingPlayer.setPositionSecondary(position);
        }
        
        // Handle photo upload
        if (request.getPhotoFile() != null && !request.getPhotoFile().isEmpty()) {
            String photoUrl = imagenService.uploadImage(request.getPhotoFile());
            existingPlayer.setPhotoUrl(photoUrl);
        }

        return playerRepository.save(existingPlayer);
    }

    /**
     * PUT /api/players/{id}/status
     */
    @Transactional
    public Player updatePlayerStatus(UUID id, boolean isActive) {
        Player existingPlayer = getPlayerById(id);
        existingPlayer.setIsActive(isActive);
        return playerRepository.save(existingPlayer);
    }

    // ---------------------- BACK-4.5: DASHBOARD -------------------------

    /**
     * GET /api/players/{id}/dashboard
     */
    public Map<String, Object> getPlayerDashboard(UUID id) {
        Player player = getPlayerById(id);

        // Estructura de resumen (stats/asistencia vacías en Sprint 1)
        return Map.of(
                "playerInfo", player,
                "statistics", Collections.emptyMap(),
                "attendance", Collections.emptyList()
        );
    }
}