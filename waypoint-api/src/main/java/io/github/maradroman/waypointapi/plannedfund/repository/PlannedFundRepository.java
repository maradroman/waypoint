package io.github.maradroman.waypointapi.plannedfund.repository;

import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannedFundRepository extends JpaRepository<PlannedFund, UUID> {
    List<PlannedFund> findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(
            UUID goalId, LocalDate fromDate);

    Optional<PlannedFund> findByGoalIdAndDateAndIsDeletedFalse(UUID goalId, LocalDate date);

    Optional<PlannedFund> findByGoalIdAndDate(UUID goalId, LocalDate date);

    @Modifying
    @Query("DELETE FROM PlannedFund pf WHERE pf.date < :today")
    int hardDeletePastPlannedFunds(@Param("today") LocalDate today);
}
