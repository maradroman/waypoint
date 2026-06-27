package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import java.time.Instant;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataTransferEntity {

    public static Transfer buildTransfer(Goal goal, Milestone milestone) {
        return Transfer.builder()
                .id(TRANSFER_ID)
                .goal(goal)
                .milestone(milestone)
                .amount(TRANSFER_AMOUNT)
                .type(TRANSFER_TYPE_ALLOCATE)
                .comment("")
                .timestamp(DEFAULT_TIMESTAMP)
                .build();
    }

    public static Transfer buildTransfer(
            UUID id, Goal goal, Milestone milestone, int amount, String type, Instant timestamp) {
        return Transfer.builder()
                .id(id)
                .goal(goal)
                .milestone(milestone)
                .amount(amount)
                .type(type)
                .comment("")
                .timestamp(timestamp)
                .build();
    }
}
