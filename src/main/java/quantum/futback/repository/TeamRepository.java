package quantum.futback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantum.futback.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);
    List<Team> findByCategory(String category);
    List<Team> findByIsActive(Boolean isActive);
    List<Team> findByCategoryAndIsActive(String category, Boolean isActive);
}