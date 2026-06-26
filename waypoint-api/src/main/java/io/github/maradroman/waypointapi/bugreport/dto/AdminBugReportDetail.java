package io.github.maradroman.waypointapi.bugreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Admin bug report detail — includes metadata, reporter, and attachments with download URLs")
public record AdminBugReportDetail(
        @Schema(description = "Bug report UUID")
        UUID id,
        @Schema(description = "Bug description")
        String description,
        @Schema(description = "Reproduction metadata")
        Map<String, Object> metadata,
        @Schema(description = "Creation timestamp")
        Instant createdAt,
        @Schema(description = "Reporter user info")
        AdminBugReportListItem.ReporterInfo user,
        @Schema(description = "Attachments with presigned download URLs")
        List<AttachmentWithUrl> attachments
) {
    @Schema(description = "Attachment with presigned download URL")
    public record AttachmentWithUrl(
            @Schema(description = "Attachment UUID")
            UUID id,
            @Schema(description = "Original filename")
            String filename,
            @Schema(description = "MIME content type")
            String contentType,
            @Schema(description = "File size in bytes")
            long sizeBytes,
            @Schema(description = "Creation timestamp")
            Instant createdAt,
            @Schema(description = "Presigned download URL (time-limited)")
            String downloadUrl
    ) {}
}
