package io.github.maradroman.waypointapi.repository;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.repository.GoalRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GoalRepositoryTest extends TestDataJpa {

    @Autowired
    private GoalRepository goalRepository;

    @Test
    @DisplayName("findById returns empty when not exists")
    void findById_returnsEmpty_whenNotExistsTest() {
        var actualResult = goalRepository.findById(UUID.randomUUID());

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findById returns goal when exists")
    void findById_returnsGoal_whenExistsTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        flushAndClear();

        var actualResult = goalRepository.findById(GOAL_ID);

        assertThat(actualResult)
                .isPresent()
                .get()
                .extracting(Goal::getId, Goal::getTitle)
                .containsExactly(GOAL_ID, GOAL_TITLE);
    }

    @Test
    @DisplayName("findByUserIdOrderBySortOrderAsc returns goals for user ordered by sortOrder")
    void findByUserIdOrderBySortOrderAsc_returnsGoalsOrderedBySortOrderTest() {
        persistUser(USER_ID);
        persistGoal(GOAL_ID, USER_ID, GOAL_TITLE, 0);
        persistGoal(GOAL_ID_2, USER_ID, GOAL_TITLE_2, 1);
        flushAndClear();

        var actualResult = goalRepository.findByUserIdOrderBySortOrderAsc(USER_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(Goal::getId, Goal::getTitle)
                .containsExactly(tuple(GOAL_ID, GOAL_TITLE), tuple(GOAL_ID_2, GOAL_TITLE_2));
    }

    @Test
    @DisplayName("findByUserIdOrderBySortOrderAsc returns empty for user with no goals")
    void findByUserIdOrderBySortOrderAsc_returnsEmpty_whenUserHasNoGoalsTest() {
        persistUser(USER_ID);
        flushAndClear();

        var actualResult = goalRepository.findByUserIdOrderBySortOrderAsc(USER_ID_2);

        assertThat(actualResult).isEmpty();
    }
}
