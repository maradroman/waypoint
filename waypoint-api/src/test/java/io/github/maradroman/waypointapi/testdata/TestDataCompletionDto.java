package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.completion.dto.CompletionResponse;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataCompletionDto {

    public static CompletionResponse completionResponse() {
        return new CompletionResponse(COMPLETION_ID, GOAL_ID, MILESTONE_ID, COMPLETION_AMOUNT,
                DEFAULT_TIMESTAMP, DEFAULT_TIMESTAMP);
    }

    public static CompletionResponse completionResponse(UUID id, int amount, Instant timestamp) {
        return new CompletionResponse(id, GOAL_ID, MILESTONE_ID, amount, timestamp, timestamp);
    }
}
