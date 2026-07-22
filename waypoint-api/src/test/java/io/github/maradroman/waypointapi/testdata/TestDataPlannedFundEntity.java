package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import java.time.LocalDate;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataPlannedFundEntity {

    public static PlannedFund buildPlannedFund(Goal goal) {
        return buildPlannedFund(PLANNED_FUND_ID, goal, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT);
    }

    public static PlannedFund buildPlannedFund(UUID id, Goal goal, LocalDate date, Integer amount) {
        return PlannedFund.builder()
                .id(id)
                .goal(goal)
                .date(date)
                .amount(amount)
                .isDeleted(false)
                .build();
    }
}
