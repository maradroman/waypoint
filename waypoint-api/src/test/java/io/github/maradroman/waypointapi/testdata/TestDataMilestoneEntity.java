package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataMilestoneEntity {

    public static Milestone buildMilestone(Goal goal) {
        return Milestone.builder()
                .id(MILESTONE_ID)
                .goal(goal)
                .title(MILESTONE_TITLE)
                .cost(MILESTONE_COST)
                .details(MILESTONE_DETAILS)
                .enabled(true)
                .completed(false)
                .sortOrder(0)
                .build();
    }

    public static Milestone buildMilestone(UUID id, Goal goal, String title, int sortOrder) {
        return Milestone.builder()
                .id(id)
                .goal(goal)
                .title(title)
                .cost(MILESTONE_COST)
                .details(MILESTONE_DETAILS)
                .enabled(true)
                .completed(false)
                .sortOrder(sortOrder)
                .build();
    }

    public static Milestone buildCompletedMilestone(Goal goal) {
        return Milestone.builder()
                .id(MILESTONE_ID)
                .goal(goal)
                .title(MILESTONE_TITLE)
                .cost(MILESTONE_COST)
                .details(MILESTONE_DETAILS)
                .enabled(true)
                .completed(true)
                .completedAt(DEFAULT_TIMESTAMP)
                .sortOrder(0)
                .build();
    }

    public static Milestone buildMilestoneWithCost(Goal goal, int cost) {
        return Milestone.builder()
                .id(MILESTONE_ID)
                .goal(goal)
                .title(MILESTONE_TITLE)
                .cost(cost)
                .details(MILESTONE_DETAILS)
                .enabled(true)
                .completed(false)
                .sortOrder(0)
                .build();
    }
}
