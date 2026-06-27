package io.github.maradroman.waypointapi.repository;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_COST;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_AMOUNT_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_TYPE_ALLOCATE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransferRepositoryTest extends TestDataJpa {

    @Autowired
    private TransferRepository transferRepository;

    @Test
    @DisplayName("findByGoalId returns empty for goal with no transfers")
    void findByGoalId_returnsEmpty_whenGoalHasNoTransfersTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        flushAndClear();

        var actualResult = transferRepository.findByGoalId(GOAL_ID_2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByGoalId returns all transfers for goal")
    void findByGoalId_returnsAllTransfersForGoalTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        persistTransfer(TRANSFER_ID, GOAL_ID, MILESTONE_ID, TRANSFER_AMOUNT, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP);
        persistTransfer(
                TRANSFER_ID_2, GOAL_ID, MILESTONE_ID, TRANSFER_AMOUNT_2, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = transferRepository.findByGoalId(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Transfer::getId)
                .containsExactlyInAnyOrder(TRANSFER_ID, TRANSFER_ID_2);
    }

    @Test
    @DisplayName("findByGoalIdOrderByTimestampDesc returns transfers ordered by timestamp desc")
    void findByGoalIdOrderByTimestampDesc_returnsTransfersOrderedByTimestampDescTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        persistTransfer(TRANSFER_ID, GOAL_ID, MILESTONE_ID, TRANSFER_AMOUNT, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP);
        persistTransfer(
                TRANSFER_ID_2, GOAL_ID, MILESTONE_ID, TRANSFER_AMOUNT_2, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = transferRepository.findByGoalIdOrderByTimestampDesc(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Transfer::getId, Transfer::getAmount)
                .containsExactly(tuple(TRANSFER_ID_2, TRANSFER_AMOUNT_2), tuple(TRANSFER_ID, TRANSFER_AMOUNT));
    }

    @Test
    @DisplayName("findByMilestoneIdOrderByTimestampDesc returns transfers for milestone ordered by timestamp desc")
    void findByMilestoneIdOrderByTimestampDesc_returnsTransfersForMilestoneOrderedByTimestampDescTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        persistMilestone(MILESTONE_ID_2, GOAL_ID, MILESTONE_TITLE_2, MILESTONE_COST, 1);
        persistTransfer(TRANSFER_ID, GOAL_ID, MILESTONE_ID, TRANSFER_AMOUNT, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP);
        persistTransfer(
                TRANSFER_ID_2, GOAL_ID, MILESTONE_ID_2, TRANSFER_AMOUNT_2, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = transferRepository.findByMilestoneIdOrderByTimestampDesc(MILESTONE_ID);

        assertThat(actualResult)
                .hasSize(1)
                .extracting(Transfer::getId, Transfer::getAmount)
                .containsExactly(tuple(TRANSFER_ID, TRANSFER_AMOUNT));
    }
}
