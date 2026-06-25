package io.github.maradroman.waypointapi.completion.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.github.maradroman.waypointapi.completion.dto.CompletionResponse;
import io.github.maradroman.waypointapi.completion.service.CompletionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals/{goalId}/completions")
@RequiredArgsConstructor
@Tag(name = "Completions", description = "View and delete milestone completion records")
public class CompletionController {

    private final CompletionService completionService;

    @GetMapping
    @Operation(summary = "List completions", description = "Returns all completion records for a goal, ordered by most recent first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Completions retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<ResponseEnvelope<List<CompletionResponse>>> listCompletions(
            @CurrentUser User user,
            @PathVariable UUID goalId
    ) {
        return ResponseEntity.ok(ResponseEnvelope.of(completionService.listCompletions(user, goalId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete completion", description = "Deletes a completion record and reverts the milestone to incomplete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Completion deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Goal or completion not found")
    })
    public ResponseEntity<Void> deleteCompletion(
            @CurrentUser User user,
            @PathVariable UUID goalId,
            @PathVariable UUID id
    ) {
        completionService.deleteCompletion(user, goalId, id);
        return ResponseEntity.noContent().build();
    }
}
