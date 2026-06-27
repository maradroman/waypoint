package io.github.maradroman.waypointapi.repository;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_COST;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.repository.MilestoneRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MilestoneRepositoryTest extends TestDataJpa {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Test
    @DisplayName("findById returns empty when not exists")
    void findById_returnsEmpty_whenNotExistsTest() {
        var actualResult = milestoneRepository.findById(UUID.randomUUID());

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findById returns milestone when exists")
    void findById_returnsMilestone_whenExistsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        flushAndClear();

        var actualResult = milestoneRepository.findById(MILESTONE_ID);

        assertThat(actualResult)
                .isPresent()
                .get()
                .extracting(Milestone::getId, Milestone::getTitle)
                .containsExactly(MILESTONE_ID, MILESTONE_TITLE);
    }

    @Test
    @DisplayName("findByGoalId returns all milestones for goal")
    void findByGoalId_returnsAllMilestonesForGoalTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        persistMilestone(MILESTONE_ID_2, GOAL_ID, MILESTONE_TITLE_2, MILESTONE_COST, 1);
        flushAndClear();

        var actualResult = milestoneRepository.findByGoalId(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Milestone::getId)
                .containsExactlyInAnyOrder(MILESTONE_ID, MILESTONE_ID_2);
    }

    @Test
    @DisplayName("findByGoalIdOrderBySortOrderAsc returns milestones ordered by sortOrder")
    void findByGoalIdOrderBySortOrderAsc_returnsMilestonesOrderedBySortOrderTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 1);
        persistMilestone(MILESTONE_ID_2, GOAL_ID, MILESTONE_TITLE_2, MILESTONE_COST, 0);
        flushAndClear();

        var actualResult = milestoneRepository.findByGoalIdOrderBySortOrderAsc(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Milestone::getId, Milestone::getTitle)
                .containsExactly(tuple(MILESTONE_ID_2, MILESTONE_TITLE_2), tuple(MILESTONE_ID, MILESTONE_TITLE));
    }
}
