package io.github.maradroman.waypointapi.repository;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_AMOUNT_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DepositRepositoryTest extends TestDataJpa {

    @Autowired
    private DepositRepository depositRepository;

    @Test
    @DisplayName("findByGoalId returns empty for goal with no deposits")
    void findByGoalId_returnsEmpty_whenGoalHasNoDepositsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        flushAndClear();

        var actualResult = depositRepository.findByGoalId(GOAL_ID_2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByGoalId returns all deposits for goal")
    void findByGoalId_returnsAllDepositsForGoalTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistDeposit(DEPOSIT_ID, GOAL_ID, DEPOSIT_AMOUNT, DEFAULT_TIMESTAMP);
        persistDeposit(DEPOSIT_ID_2, GOAL_ID, DEPOSIT_AMOUNT_2, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = depositRepository.findByGoalId(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Deposit::getId)
                .containsExactlyInAnyOrder(DEPOSIT_ID, DEPOSIT_ID_2);
    }

    @Test
    @DisplayName("findByGoalIdOrderByTimestampDesc returns deposits ordered by timestamp desc")
    void findByGoalIdOrderByTimestampDesc_returnsDepositsOrderedByTimestampDescTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistDeposit(DEPOSIT_ID, GOAL_ID, DEPOSIT_AMOUNT, DEFAULT_TIMESTAMP);
        persistDeposit(DEPOSIT_ID_2, GOAL_ID, DEPOSIT_AMOUNT_2, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = depositRepository.findByGoalIdOrderByTimestampDesc(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Deposit::getId, Deposit::getAmount)
                .containsExactly(tuple(DEPOSIT_ID_2, DEPOSIT_AMOUNT_2), tuple(DEPOSIT_ID, DEPOSIT_AMOUNT));
    }
}
