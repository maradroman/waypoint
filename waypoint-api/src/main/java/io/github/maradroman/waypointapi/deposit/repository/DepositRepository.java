package io.github.maradroman.waypointapi.deposit.repository;

import io.github.maradroman.waypointapi.deposit.model.Deposit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    List<Deposit> findByGoalIdOrderByTimestampDesc(UUID goalId);

    List<Deposit> findByGoalId(UUID goalId);
}
