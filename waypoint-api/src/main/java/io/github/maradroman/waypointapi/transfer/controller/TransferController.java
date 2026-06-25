package io.github.maradroman.waypointapi.transfer.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.transfer.dto.*;
import io.github.maradroman.waypointapi.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals/{goalId}/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Allocate and withdraw funds between goal wallet and milestones")
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    @Operation(summary = "List transfers", description = "Returns all transfers for a goal, ordered by most recent first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfers retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<TransferResponse>>> listTransfers(
            @CurrentUser User user,
            @PathVariable UUID goalId
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(transferService.listTransfers(user, goalId)));
    }

    @PostMapping("/allocate")
    @Operation(summary = "Allocate funds", description = "Allocate funds from the goal wallet to a milestone. Cannot exceed wallet balance or milestone remaining need.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds allocated"),
            @ApiResponse(responseCode = "400", description = "Allocation failed (insufficient funds or milestone fully funded)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<AllocateResponse>> allocate(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @Valid @RequestBody AllocateRequest request
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(transferService.allocate(user, goalId, request)));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds", description = "Withdraw previously allocated funds from a milestone back to the goal wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds withdrawn"),
            @ApiResponse(responseCode = "400", description = "Withdrawal failed (no funds allocated)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or milestone not found")
    })
    public ResponseEntity<ResponseEnvelope<AllocateResponse>> withdraw(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(transferService.withdraw(user, goalId, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update transfer", description = "Updates the amount of a transfer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or transfer not found")
    })
    public ResponseEntity<ResponseEnvelope<TransferResponse>> updateTransfer(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransferRequest request
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(
                transferService.updateTransfer(user, goalId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transfer", description = "Deletes a transfer record")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transfer deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or transfer not found")
    })
    public ResponseEntity<Void> deleteTransfer(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id
    ) {
        transferService.deleteTransfer(user, goalId, id);
        return ResponseEntity.noContent().build();
    }
}
