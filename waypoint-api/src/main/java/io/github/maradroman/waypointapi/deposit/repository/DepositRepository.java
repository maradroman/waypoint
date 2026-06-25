package io.github.maradroman.waypointapi.deposit.repository;

import io.github.maradroman.waypointapi.deposit.model.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    List<Deposit> findByGoalIdOrderByTimestampDesc(UUID goalId);
    List<Deposit> findByGoalId(UUID goalId);
}
