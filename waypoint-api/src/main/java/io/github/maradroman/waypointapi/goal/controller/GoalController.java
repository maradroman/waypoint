package io.github.maradroman.waypointapi.goal.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.goal.dto.CreateGoalRequest;
import io.github.maradroman.waypointapi.goal.dto.GoalResponse;
import io.github.maradroman.waypointapi.goal.dto.ReorderGoalsRequest;
import io.github.maradroman.waypointapi.goal.dto.UpdateGoalRequest;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Create, list, update, delete and reorder goals")
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    @Operation(
            summary = "List goals",
            description = "Returns all goals for the authenticated user, ordered by sort order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goals retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<List<GoalResponse>>> listGoals(@CurrentUser User user) {
        return ResponseEntity.ok(ResponseEnvelope.of(goalService.listGoals(user)));
    }

    @PostMapping
    @Operation(summary = "Create goal", description = "Creates a new goal for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Goal created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<GoalResponse>> createGoal(
            @CurrentUser User user, @Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.of(goalService.createGoal(user, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Returns a single goal by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<GoalResponse>> getGoal(@CurrentUser User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(goalService.getGoal(user, id)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update goal", description = "Updates title, description, or icon of a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<GoalResponse>> updateGoal(
            @CurrentUser User user, @PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(goalService.updateGoal(user, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal", description = "Deletes a goal and all associated data")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Goal deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<Void> deleteGoal(@CurrentUser User user, @PathVariable UUID id) {
        goalService.deleteGoal(user, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    @Operation(
            summary = "Reorder goals",
            description = "Set the sort order of goals by providing a list of goal IDs in the desired order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goals reordered"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<List<GoalResponse>>> reorderGoals(
            @CurrentUser User user, @Valid @RequestBody ReorderGoalsRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(goalService.reorderGoals(user, request)));
    }
}
