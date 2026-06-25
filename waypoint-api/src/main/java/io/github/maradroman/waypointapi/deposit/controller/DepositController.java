package io.github.maradroman.waypointapi.deposit.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.deposit.dto.CreateDepositRequest;
import io.github.maradroman.waypointapi.deposit.dto.DepositResponse;
import io.github.maradroman.waypointapi.deposit.dto.UpdateDepositRequest;
import io.github.maradroman.waypointapi.deposit.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals/{goalId}/deposits")
@RequiredArgsConstructor
@Tag(name = "Deposits", description = "Manage deposits (income) into a goal's wallet")
public class DepositController {

    private final DepositService depositService;

    @GetMapping
    @Operation(summary = "List deposits", description = "Returns all deposits for a goal, ordered by most recent first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposits retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<DepositResponse>>> listDeposits(
            @CurrentUser User user,
            @PathVariable UUID goalId
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(depositService.listDeposits(user, goalId)));
    }

    @PostMapping
    @Operation(summary = "Create deposit", description = "Adds a deposit (income) to a goal's wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<DepositResponse>> createDeposit(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @Valid @RequestBody CreateDepositRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.of(depositService.createDeposit(user, goalId, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update deposit", description = "Updates the amount of a deposit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or deposit not found")
    })
    public ResponseEntity<ResponseEnvelope<DepositResponse>> updateDeposit(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDepositRequest request
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(depositService.updateDeposit(user, goalId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete deposit", description = "Deletes a deposit")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deposit deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or deposit not found")
    })
    public ResponseEntity<Void> deleteDeposit(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id
    ) {
        depositService.deleteDeposit(user, goalId, id);
        return ResponseEntity.noContent().build();
    }
}
