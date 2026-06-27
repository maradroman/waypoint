package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.plannedfund.dto.PlannedFundResponse;
import io.github.maradroman.waypointapi.plannedfund.dto.UpsertPlannedFundRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataPlannedFundDto {

    public static UpsertPlannedFundRequest upsertPlannedFundRequest(String date, Integer amount) {
        return new UpsertPlannedFundRequest(date, amount);
    }

    public static UpsertPlannedFundRequest upsertPlannedFundRequest(LocalDate date, Integer amount) {
        return new UpsertPlannedFundRequest(date.toString(), amount);
    }

    public static PlannedFundResponse plannedFundResponse() {
        return plannedFundResponse(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT);
    }

    public static PlannedFundResponse plannedFundResponse(UUID id, UUID goalId, LocalDate date, Integer amount) {
        return new PlannedFundResponse(id, goalId, date.toString(), amount);
    }
}
