package io.github.maradroman.waypointapi.milestone.repository;

import io.github.maradroman.waypointapi.milestone.model.Milestone;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByGoalIdOrderBySortOrderAsc(UUID goalId);

    List<Milestone> findByGoalId(UUID goalId);
}
