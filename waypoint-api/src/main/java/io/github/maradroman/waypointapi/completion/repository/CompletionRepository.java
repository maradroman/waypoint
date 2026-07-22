package io.github.maradroman.waypointapi.completion.repository;

import io.github.maradroman.waypointapi.completion.model.Completion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, UUID> {
    List<Completion> findByGoalIdOrderByTimestampDesc(UUID goalId);
}
