package quantum.futback.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.Position;
import quantum.futback.repository.PlayerRepository;
import quantum.futback.repository.PositionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final PlayerRepository playerRepository;

    public PositionService(PositionRepository positionRepository, PlayerRepository playerRepository) {
        this.positionRepository = positionRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Position createPosition(Position position) {
        position.setTenantId(TenantContext.getTenantId());
        return positionRepository.save(position);
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public Position getPositionById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Position not found"));
    }

    @Transactional
    public Position updatePosition(UUID id, Position updatedPosition) {
        Position existingPosition = getPositionById(id);

        existingPosition.setName(updatedPosition.getName());
        existingPosition.setAbbreviation(updatedPosition.getAbbreviation());
        existingPosition.setArea(updatedPosition.getArea());

        return positionRepository.save(existingPosition);
    }

    @Transactional
    public void deletePosition(UUID id) {
        Position position = getPositionById(id);

        long assignedPlayersCount = playerRepository.countByPositionPrimaryOrPositionSecondary(position, position);

        if (assignedPlayersCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete position '" + position.getName() + "' because it is currently assigned to one or more players."
            );
        }

        positionRepository.delete(position);
    }
}