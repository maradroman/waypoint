package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.goal.dto.CreateGoalRequest;
import io.github.maradroman.waypointapi.goal.dto.GoalResponse;
import io.github.maradroman.waypointapi.goal.dto.ReorderGoalsRequest;
import io.github.maradroman.waypointapi.goal.dto.UpdateGoalRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataGoalDto {

    public static GoalResponse goalResponse() {
        return new GoalResponse(
                GOAL_ID,
                GOAL_TITLE,
                GOAL_DESCRIPTION,
                GOAL_ICON,
                GOAL_SORT_ORDER,
                DEFAULT_TIMESTAMP,
                DEFAULT_TIMESTAMP);
    }

    public static GoalResponse goalResponse(UUID id, String title, Instant createdAt) {
        return new GoalResponse(id, title, GOAL_DESCRIPTION, GOAL_ICON, GOAL_SORT_ORDER, createdAt, createdAt);
    }

    public static CreateGoalRequest createGoalRequest() {
        return new CreateGoalRequest(GOAL_TITLE, GOAL_DESCRIPTION, GOAL_ICON);
    }

    public static CreateGoalRequest createGoalRequest(String title) {
        return new CreateGoalRequest(title, GOAL_DESCRIPTION, GOAL_ICON);
    }

    public static UpdateGoalRequest updateGoalRequest(String title) {
        return new UpdateGoalRequest(title, GOAL_DESCRIPTION, GOAL_ICON);
    }

    public static ReorderGoalsRequest reorderGoalsRequest(UUID... goalIds) {
        return new ReorderGoalsRequest(List.of(goalIds));
    }
}
