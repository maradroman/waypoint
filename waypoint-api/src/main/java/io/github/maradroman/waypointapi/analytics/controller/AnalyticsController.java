package io.github.maradroman.waypointapi.analytics.controller;

import io.github.maradroman.waypointapi.analytics.dto.GoalAnalyticsResponse;
import io.github.maradroman.waypointapi.analytics.dto.SummaryResponse;
import io.github.maradroman.waypointapi.analytics.service.AnalyticsService;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Goal analytics and user summary statistics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/goals/{id}/analytics")
    @Operation(summary = "Get goal analytics", description = "Returns detailed analytics for a single goal including deposits, allocations, and progress")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<GoalAnalyticsResponse>> getGoalAnalytics(
            @CurrentUser User user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(analyticsService.getGoalAnalytics(user, id)));
    }

    @GetMapping("/analytics/summary")
    @Operation(summary = "Get user summary", description = "Returns aggregate summary statistics across all goals for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<SummaryResponse>> getSummary(@CurrentUser User user) {
        return ResponseEntity.ok(ResponseEnvelope.of(analyticsService.getSummary(user)));
    }
}
