package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.analytics.dto.GoalAnalyticsResponse;
import io.github.maradroman.waypointapi.analytics.dto.SummaryResponse;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataAnalyticsDto {

    public static GoalAnalyticsResponse goalAnalyticsResponse() {
        return new GoalAnalyticsResponse(GOAL_ID, 50000, 10000, 40000,
                100000, 10000, 1, 2, 10,
                MILESTONE_ID, MILESTONE_TITLE, LocalDate.of(2026, 12, 31));
    }

    public static SummaryResponse summaryResponse() {
        return new SummaryResponse(150000, 500000, 3, 5);
    }
}
