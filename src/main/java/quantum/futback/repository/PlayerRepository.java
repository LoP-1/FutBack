package quantum.futback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantum.futback.entity.Player;
import quantum.futback.entity.Position;

import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    long countByPositionPrimaryOrPositionSecondary(Position positionPrimary, Position positionSecondary);
    long countByPositionPrimary(Position position);
    long countByPositionSecondary(Position position);
}