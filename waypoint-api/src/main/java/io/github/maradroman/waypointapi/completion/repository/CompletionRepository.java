package io.github.maradroman.waypointapi.completion.repository;

import io.github.maradroman.waypointapi.completion.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompletionRepository extends JpaRepository<Completion, UUID> {
    List<Completion> findByGoalIdOrderByTimestampDesc(UUID goalId);
}
