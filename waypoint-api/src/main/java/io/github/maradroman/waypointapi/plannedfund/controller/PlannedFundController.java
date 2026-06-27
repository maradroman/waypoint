package io.github.maradroman.waypointapi.plannedfund.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.plannedfund.dto.PlannedFundResponse;
import io.github.maradroman.waypointapi.plannedfund.dto.UpsertPlannedFundRequest;
import io.github.maradroman.waypointapi.plannedfund.service.PlannedFundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals/{goalId}/planned-funds")
@RequiredArgsConstructor
@Tag(name = "Planned Funds", description = "Manage planned future deposits for a goal")
public class PlannedFundController {

    private final PlannedFundService plannedFundService;

    @GetMapping
    @Operation(summary = "List planned funds", description = "Returns all planned funds for a goal, ordered by date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planned funds retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<PlannedFundResponse>>> listPlannedFunds(
            @CurrentUser User user,
            @PathVariable UUID goalId
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(plannedFundService.listPlannedFunds(user, goalId)));
    }

    @PutMapping("/{date}")
    @Operation(summary = "Upsert planned fund", description = "Creates or updates a planned fund for a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planned fund upserted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<PlannedFundResponse>> upsertPlannedFund(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable String date,
            @Valid @RequestBody UpsertPlannedFundRequest request
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(plannedFundService.upsertPlannedFund(user, goalId, request)));
    }

    @DeleteMapping("/{date}")
    @Operation(summary = "Delete planned fund", description = "Deletes a planned fund for a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Planned fund deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<Void> deletePlannedFund(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable String date
    ) {
        plannedFundService.deletePlannedFund(user, goalId, LocalDate.parse(date));
        return ResponseEntity.noContent().build();
    }
}
