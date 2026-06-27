package io.github.maradroman.waypointapi.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = "Reorder milestones request")
public record ReorderMilestonesRequest(
        @NotEmpty
        @Schema(description = "Milestone IDs in the desired display order", example = "[\"id1\", \"id2\", \"id3\"]")
        List<UUID> milestoneIds) {}
