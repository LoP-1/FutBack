package quantum.futback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import quantum.futback.entity.Team;
import quantum.futback.entity.DTO.TeamRequest;
import quantum.futback.entity.DTO.StatusUpdateRequest;
import quantum.futback.services.TeamService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Team> createTeam(@RequestBody TeamRequest request) {
        Team newTeam = new Team();
        newTeam.setName(request.getName());
        newTeam.setCategory(request.getCategory());
        newTeam.setIsOwnTeam(request.getIsOwnTeam());

        Team createdTeam = teamService.createTeam(newTeam);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    /**
     * GET /api/teams: Obtiene todos los equipos del tenant actual.
     * Filtros: ?category={cat}&isActive={bool}
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<List<Team>> getAllTeams(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive) {
        List<Team> teams = teamService.getAllTeams(category, isActive);
        return ResponseEntity.ok(teams);
    }

    /**
     * GET /api/teams/{id}: Obtiene un equipo por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Team> getTeamById(@PathVariable UUID id) {
        Team team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * PUT /api/teams/{id}: Actualiza los datos de un equipo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Team> updateTeam(@PathVariable UUID id, @RequestBody TeamRequest request) {
        Team updatedTeamDetails = new Team();
        updatedTeamDetails.setName(request.getName());
        updatedTeamDetails.setCategory(request.getCategory());
        updatedTeamDetails.setIsOwnTeam(request.getIsOwnTeam());

        Team updatedTeam = teamService.updateTeam(id, updatedTeamDetails);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * PUT /api/teams/{id}/status: Actualiza el estado de actividad del equipo.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COACH')")
    public ResponseEntity<Team> updateTeamStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest request) {
        Team team = teamService.updateTeamStatus(id, Boolean.TRUE.equals(request.getIsActive()));
        return ResponseEntity.ok(team);
    }
}