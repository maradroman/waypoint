package io.github.maradroman.waypointapi.bugreport.dto;

import io.github.maradroman.waypointapi.bugreport.model.BugReportAttachment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Bug report attachment metadata")
public record BugReportAttachmentResponse(
        @Schema(description = "Attachment UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Original filename", example = "screenshot.png")
        String filename,

        @Schema(description = "MIME content type", example = "image/png")
        String contentType,

        @Schema(description = "File size in bytes", example = "204800")
        long sizeBytes,

        @Schema(description = "Creation timestamp") Instant createdAt) {
    public static BugReportAttachmentResponse from(BugReportAttachment attachment) {
        return new BugReportAttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt());
    }
}
