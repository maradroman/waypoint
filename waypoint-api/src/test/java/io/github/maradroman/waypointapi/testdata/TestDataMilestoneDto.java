package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.milestone.dto.CreateMilestoneRequest;
import io.github.maradroman.waypointapi.milestone.dto.MilestoneResponse;
import io.github.maradroman.waypointapi.milestone.dto.ReorderMilestonesRequest;
import io.github.maradroman.waypointapi.milestone.dto.UpdateMilestoneRequest;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataMilestoneDto {

    public static MilestoneResponse milestoneResponse() {
        return new MilestoneResponse(
                MILESTONE_ID,
                GOAL_ID,
                MILESTONE_TITLE,
                MILESTONE_COST,
                MILESTONE_DETAILS,
                true,
                false,
                null,
                0,
                0,
                DEFAULT_TIMESTAMP,
                DEFAULT_TIMESTAMP);
    }

    public static MilestoneResponse milestoneResponse(UUID id, String title, int balance, int cost) {
        return new MilestoneResponse(
                id,
                GOAL_ID,
                title,
                cost,
                MILESTONE_DETAILS,
                true,
                false,
                null,
                0,
                balance,
                DEFAULT_TIMESTAMP,
                DEFAULT_TIMESTAMP);
    }

    public static CreateMilestoneRequest createMilestoneRequest() {
        return new CreateMilestoneRequest(MILESTONE_TITLE, MILESTONE_COST, MILESTONE_DETAILS, true);
    }

    public static CreateMilestoneRequest createMilestoneRequest(String title, Integer cost, Boolean enabled) {
        return new CreateMilestoneRequest(title, cost, MILESTONE_DETAILS, enabled);
    }

    public static UpdateMilestoneRequest updateMilestoneRequest(String title, Integer cost) {
        return new UpdateMilestoneRequest(title, cost, MILESTONE_DETAILS, true);
    }

    public static ReorderMilestonesRequest reorderMilestonesRequest(UUID... milestoneIds) {
        return new ReorderMilestonesRequest(List.of(milestoneIds));
    }
}
