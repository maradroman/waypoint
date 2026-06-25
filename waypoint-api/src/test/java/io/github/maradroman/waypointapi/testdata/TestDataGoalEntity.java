package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.goal.model.Goal;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataGoalEntity {

    public static Goal buildGoal(User user) {
        return Goal.builder()
                .id(GOAL_ID)
                .user(user)
                .title(GOAL_TITLE)
                .description(GOAL_DESCRIPTION)
                .icon(GOAL_ICON)
                .sortOrder(GOAL_SORT_ORDER)
                .build();
    }

    public static Goal buildGoal(UUID id, User user, String title) {
        return Goal.builder()
                .id(id)
                .user(user)
                .title(title)
                .description(GOAL_DESCRIPTION)
                .icon(GOAL_ICON)
                .sortOrder(GOAL_SORT_ORDER)
                .build();
    }

    public static Goal buildGoalWithSortOrder(User user, int sortOrder) {
        return Goal.builder()
                .id(GOAL_ID)
                .user(user)
                .title(GOAL_TITLE)
                .description(GOAL_DESCRIPTION)
                .icon(GOAL_ICON)
                .sortOrder(sortOrder)
                .build();
    }
}
