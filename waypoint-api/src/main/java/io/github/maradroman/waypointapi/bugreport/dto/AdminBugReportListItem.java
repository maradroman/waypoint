package io.github.maradroman.waypointapi.bugreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Admin bug report list item — includes reporter info")
public record AdminBugReportListItem(
        @Schema(description = "Bug report UUID")
        UUID id,
        @Schema(description = "Bug description")
        String description,
        @Schema(description = "Creation timestamp")
        Instant createdAt,
        @Schema(description = "Reporter user info")
        ReporterInfo user,
        @Schema(description = "Number of attachments")
        int attachmentCount
) {
    @Schema(description = "Reporter user info")
    public record ReporterInfo(
            @Schema(description = "User UUID")
            UUID id,
            @Schema(description = "Email")
            String email,
            @Schema(description = "Display name")
            String displayName
    ) {}
}
