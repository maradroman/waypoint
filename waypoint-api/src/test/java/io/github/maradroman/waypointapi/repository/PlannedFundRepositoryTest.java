package io.github.maradroman.waypointapi.repository;

import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class PlannedFundRepositoryTest extends TestDataJpa {

    @Autowired
    private PlannedFundRepository plannedFundRepository;

    @Test
    @DisplayName("findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc returns only non-deleted future funds")
    void findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc_returnsOnlyNonDeletedFutureFundsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT, false);
        persistPlannedFund(PLANNED_FUND_ID_2, GOAL_ID, PLANNED_FUND_DATE_2, PLANNED_FUND_AMOUNT_2, true); // deleted
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(
                GOAL_ID, LocalDate.now());

        assertThat(actualResult)
                .hasSize(1)
                .extracting(PlannedFund::getId, PlannedFund::getAmount)
                .containsExactly(tuple(PLANNED_FUND_ID, PLANNED_FUND_AMOUNT));
    }

    @Test
    @DisplayName("findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc filters past dates")
    void findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc_filtersPastDatesTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PAST_DATE, PLANNED_FUND_AMOUNT, false);
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(
                GOAL_ID, LocalDate.now());

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc orders by date ascending")
    void findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc_ordersByDateAscendingTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID_2, GOAL_ID, PLANNED_FUND_DATE_2, PLANNED_FUND_AMOUNT_2, false);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT, false);
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(
                GOAL_ID, LocalDate.now());

        assertThat(actualResult)
                .hasSize(2)
                .extracting(PlannedFund::getId, PlannedFund::getDate)
                .containsExactly(
                        tuple(PLANNED_FUND_ID, PLANNED_FUND_DATE),
                        tuple(PLANNED_FUND_ID_2, PLANNED_FUND_DATE_2)
                );
    }

    @Test
    @DisplayName("findByGoalIdAndDateAndIsDeletedFalse returns planned fund when not deleted")
    void findByGoalIdAndDateAndIsDeletedFalse_returnsPlannedFundWhenNotDeletedTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT, false);
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndDateAndIsDeletedFalse(GOAL_ID, PLANNED_FUND_DATE);

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get())
                .extracting(PlannedFund::getId, PlannedFund::getAmount)
                .containsExactly(PLANNED_FUND_ID, PLANNED_FUND_AMOUNT);
    }

    @Test
    @DisplayName("findByGoalIdAndDateAndIsDeletedFalse returns empty when deleted")
    void findByGoalIdAndDateAndIsDeletedFalse_returnsEmptyWhenDeletedTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT, true);
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndDateAndIsDeletedFalse(GOAL_ID, PLANNED_FUND_DATE);

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByGoalIdAndDate returns planned fund regardless of deleted status")
    void findByGoalIdAndDate_returnsPlannedFundRegardlessOfDeletedStatusTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT, true);
        flushAndClear();

        var actualResult = plannedFundRepository.findByGoalIdAndDate(GOAL_ID, PLANNED_FUND_DATE);

        assertThat(actualResult).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("hardDeletePastPlannedFunds deletes all past planned funds")
    void hardDeletePastPlannedFunds_deletesAllPastPlannedFundsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PAST_DATE, PLANNED_FUND_AMOUNT, false);
        persistPlannedFund(PLANNED_FUND_ID_2, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT_2, false);
        flushAndClear();

        int deletedCount = plannedFundRepository.hardDeletePastPlannedFunds(LocalDate.now());
        flushAndClear();

        assertThat(deletedCount).isEqualTo(1);
        var remainingFunds = plannedFundRepository.findAll();
        assertThat(remainingFunds)
                .hasSize(1)
                .extracting(PlannedFund::getId)
                .containsExactly(PLANNED_FUND_ID_2);
    }

    @Test
    @Transactional
    @DisplayName("hardDeletePastPlannedFunds deletes both deleted and non-deleted past funds")
    void hardDeletePastPlannedFunds_deletesBothDeletedAndNonDeletedPastFundsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistPlannedFund(PLANNED_FUND_ID, GOAL_ID, PAST_DATE, PLANNED_FUND_AMOUNT, false);
        persistPlannedFund(PLANNED_FUND_ID_2, GOAL_ID, PAST_DATE.plusDays(1), PLANNED_FUND_AMOUNT_2, true);
        flushAndClear();

        int deletedCount = plannedFundRepository.hardDeletePastPlannedFunds(LocalDate.now());
        flushAndClear();

        assertThat(deletedCount).isEqualTo(2);
        assertThat(plannedFundRepository.findAll()).isEmpty();
    }
}
