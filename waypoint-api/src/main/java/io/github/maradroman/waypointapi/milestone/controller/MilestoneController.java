package io.github.maradroman.waypointapi.milestone.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.completion.service.CompletionService;
import io.github.maradroman.waypointapi.milestone.dto.CreateMilestoneRequest;
import io.github.maradroman.waypointapi.milestone.dto.MilestoneResponse;
import io.github.maradroman.waypointapi.milestone.dto.ReorderMilestonesRequest;
import io.github.maradroman.waypointapi.milestone.dto.UpdateMilestoneRequest;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
import io.github.maradroman.waypointapi.transfer.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals/{goalId}/milestones")
@RequiredArgsConstructor
@Tag(name = "Milestones", description = "Manage milestones within a goal")
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final CompletionService completionService;

    @GetMapping
    @Operation(summary = "List milestones", description = "Returns all milestones for a goal, ordered by sort order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestones retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<MilestoneResponse>>> listMilestones(
            @CurrentUser User user, @PathVariable UUID goalId) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.listMilestones(user, goalId)));
    }

    @PostMapping
    @Operation(summary = "Create milestone", description = "Creates a new milestone within a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Milestone created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<MilestoneResponse>> createMilestone(
            @CurrentUser User user, @PathVariable UUID goalId, @Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.of(milestoneService.createMilestone(user, goalId, request)));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update milestone",
            description = "Updates title, cost, details, or enabled status of a milestone")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestone updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<MilestoneResponse>> updateMilestone(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMilestoneRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.updateMilestone(user, goalId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete milestone", description = "Deletes a milestone")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Milestone deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<Void> deleteMilestone(
            @CurrentUser User user, @PathVariable UUID goalId, @PathVariable UUID id) {
        milestoneService.deleteMilestone(user, goalId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Complete milestone",
            description = "Marks a milestone as completed and creates a completion record")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestone completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<MilestoneResponse>> completeMilestone(
            @CurrentUser User user, @PathVariable UUID goalId, @PathVariable UUID id) {
        completionService.createCompletion(user, goalId, id);
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.getMilestoneWithBalance(user, goalId, id)));
    }

    @GetMapping("/{id}/transfers")
    @Operation(
            summary = "List milestone transfers",
            description = "Returns all transfers (allocations/withdrawals) for a milestone")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfers retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<List<TransferResponse>>> listMilestoneTransfers(
            @CurrentUser User user, @PathVariable UUID goalId, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.listMilestoneTransfers(user, goalId, id)));
    }

    @PostMapping("/{id}/uncomplete")
    @Operation(summary = "Uncomplete milestone", description = "Reverts a completed milestone back to incomplete")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestone uncompleted"),
        @ApiResponse(responseCode = "400", description = "Milestone is not completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<MilestoneResponse>> uncompleteMilestone(
            @CurrentUser User user, @PathVariable UUID goalId, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.uncompleteMilestone(user, goalId, id)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle milestone", description = "Toggles a milestone's enabled/disabled status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestone toggled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<MilestoneResponse>> toggleMilestone(
            @CurrentUser User user, @PathVariable UUID goalId, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.toggleMilestone(user, goalId, id)));
    }

    @PatchMapping("/reorder")
    @Operation(summary = "Reorder milestones", description = "Set the sort order of milestones within a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Milestones reordered"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<MilestoneResponse>>> reorderMilestones(
            @CurrentUser User user, @PathVariable UUID goalId, @Valid @RequestBody ReorderMilestonesRequest request) {
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.reorderMilestones(user, goalId, request)));
    }

    @PatchMapping("/toggle-all")
    @Operation(summary = "Toggle all milestones", description = "Enable or disable all milestones in a goal at once")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All milestones toggled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<MilestoneResponse>>> toggleAllMilestones(
            @CurrentUser User user, @PathVariable UUID goalId, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", false);
        return ResponseEntity.ok(ResponseEnvelope.of(milestoneService.toggleAllMilestones(user, goalId, enabled)));
    }
}
