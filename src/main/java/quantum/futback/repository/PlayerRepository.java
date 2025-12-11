package quantum.futback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quantum.futback.entity.Player;
import quantum.futback.entity.Position;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    long countByPositionPrimaryOrPositionSecondary(Position positionPrimary, Position positionSecondary);
    long countByPositionPrimary(Position position);
    long countByPositionSecondary(Position position);
    
    List<Player> findByTeamId(UUID teamId);
    
    @Query("SELECT p FROM Player p WHERE " +
           "(:teamId IS NULL OR p.team.id = :teamId) AND " +
           "(:name IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:dni IS NULL OR p.dni LIKE CONCAT('%', :dni, '%'))")
    List<Player> findByFilters(@Param("teamId") UUID teamId, @Param("name") String name, @Param("dni") String dni);
}