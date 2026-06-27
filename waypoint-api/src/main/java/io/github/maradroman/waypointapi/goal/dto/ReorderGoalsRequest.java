package io.github.maradroman.waypointapi.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = "Reorder goals request")
public record ReorderGoalsRequest(
        @NotEmpty
        @Schema(description = "Goal IDs in the desired display order", example = "[\"id1\", \"id2\", \"id3\"]")
        List<UUID> goalIds) {}
