package io.github.maradroman.waypointapi.transfer.repository;

import io.github.maradroman.waypointapi.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    List<Transfer> findByGoalIdOrderByTimestampDesc(UUID goalId);
    List<Transfer> findByMilestoneIdOrderByTimestampDesc(UUID milestoneId);
    List<Transfer> findByGoalId(UUID goalId);
}
