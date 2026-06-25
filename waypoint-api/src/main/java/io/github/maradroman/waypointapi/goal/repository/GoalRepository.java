package io.github.maradroman.waypointapi.goal.repository;

import io.github.maradroman.waypointapi.goal.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserIdOrderBySortOrderAsc(UUID userId);
}
