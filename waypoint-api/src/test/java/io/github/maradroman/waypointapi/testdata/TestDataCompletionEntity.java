package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.completion.model.Completion;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataCompletionEntity {

    public static Completion buildCompletion(Goal goal, Milestone milestone) {
        return Completion.builder()
                .id(COMPLETION_ID)
                .goal(goal)
                .milestone(milestone)
                .amount(COMPLETION_AMOUNT)
                .timestamp(DEFAULT_TIMESTAMP)
                .build();
    }

    public static Completion buildCompletion(UUID id, Goal goal, Milestone milestone, int amount, Instant timestamp) {
        return Completion.builder()
                .id(id)
                .goal(goal)
                .milestone(milestone)
                .amount(amount)
                .timestamp(timestamp)
                .build();
    }
}
