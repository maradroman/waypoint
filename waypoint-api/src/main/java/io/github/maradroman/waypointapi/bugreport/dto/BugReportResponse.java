package io.github.maradroman.waypointapi.bugreport.dto;

import io.github.maradroman.waypointapi.bugreport.model.BugReport;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Bug report response")
public record BugReportResponse(
        @Schema(description = "Bug report UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Bug description", example = "Clicked Allocate and the page went blank")
        String description,

        @Schema(description = "Reproduction metadata captured from the client")
        Map<String, Object> metadata,

        @Schema(description = "Creation timestamp") Instant createdAt) {
    public static BugReportResponse from(BugReport bugReport) {
        return new BugReportResponse(
                bugReport.getId(), bugReport.getDescription(), bugReport.getMetadata(), bugReport.getCreatedAt());
    }
}
