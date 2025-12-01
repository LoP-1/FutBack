package quantum.futback.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.Team;
import quantum.futback.repository.TeamRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Team createTeam(Team team) {
        if (teamRepository.findByName(team.getName()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Team with name '" + team.getName() + "' already exists in this tenant."
            );
        }
        team.setTenantId(TenantContext.getTenantId());

        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    @Transactional
    public Team updateTeam(UUID id, Team updatedTeam) {
        Team existingTeam = getTeamById(id);

        if (!existingTeam.getName().equalsIgnoreCase(updatedTeam.getName())) {
            if (teamRepository.findByName(updatedTeam.getName()).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Team with name '" + updatedTeam.getName() + "' already exists in this tenant."
                );
            }
        }

        // Actualizar campos
        existingTeam.setName(updatedTeam.getName());
        existingTeam.setCategory(updatedTeam.getCategory());
        existingTeam.setIsOwnTeam(updatedTeam.getIsOwnTeam());


        return teamRepository.save(existingTeam);
    }
    @Transactional
    public Team updateTeamStatus(UUID id, boolean isActive) {
        Team existingTeam = getTeamById(id);
        existingTeam.setIsActive(isActive);
        return teamRepository.save(existingTeam);
    }
}
