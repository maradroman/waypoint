package io.github.maradroman.waypointapi.repository;

import io.github.maradroman.waypointapi.completion.model.Completion;
import io.github.maradroman.waypointapi.completion.repository.CompletionRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.COMPLETION_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.COMPLETION_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.COMPLETION_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_COST;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CompletionRepositoryTest extends TestDataJpa {

    @Autowired
    private CompletionRepository completionRepository;

    @Test
    @DisplayName("findByGoalIdOrderByTimestampDesc returns empty for goal with no completions")
    void findByGoalIdOrderByTimestampDesc_returnsEmpty_whenGoalHasNoCompletionsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        flushAndClear();

        var actualResult = completionRepository.findByGoalIdOrderByTimestampDesc(GOAL_ID_2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findByGoalIdOrderByTimestampDesc returns completions ordered by timestamp desc")
    void findByGoalIdOrderByTimestampDesc_returnsCompletionsOrderedByTimestampDescTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistMilestone(MILESTONE_ID, GOAL_ID, MILESTONE_TITLE, MILESTONE_COST, 0);
        persistCompletion(COMPLETION_ID, GOAL_ID, MILESTONE_ID, COMPLETION_AMOUNT, DEFAULT_TIMESTAMP);
        persistCompletion(COMPLETION_ID_2, GOAL_ID, MILESTONE_ID, COMPLETION_AMOUNT, DEFAULT_TIMESTAMP_2);
        flushAndClear();

        var actualResult = completionRepository.findByGoalIdOrderByTimestampDesc(GOAL_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Completion::getId)
                .containsExactly(COMPLETION_ID_2, COMPLETION_ID);
    }
}
