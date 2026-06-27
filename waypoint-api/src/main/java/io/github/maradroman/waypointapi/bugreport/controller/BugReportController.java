package io.github.maradroman.waypointapi.bugreport.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportAttachmentResponse;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportResponse;
import io.github.maradroman.waypointapi.bugreport.dto.CreateBugReportRequest;
import io.github.maradroman.waypointapi.bugreport.service.BugReportService;
import io.github.maradroman.waypointapi.common.security.CurrentUser;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bug-reports")
@RequiredArgsConstructor
@Tag(name = "Bug Reports", description = "Submit and list user bug reports for UAT feedback")
public class BugReportController {

    private final BugReportService bugReportService;

    @PostMapping
    @Operation(
            summary = "Create bug report",
            description = "Submits a bug report with a description and client-side metadata captured for reproduction")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bug report created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<BugReportResponse>> createBugReport(
            @CurrentUser User user, @Valid @RequestBody CreateBugReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.of(bugReportService.createBugReport(user, request)));
    }

    @GetMapping
    @Operation(
            summary = "List bug reports",
            description = "Returns all bug reports submitted by the authenticated user, newest first")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bug reports retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseEnvelope<List<BugReportResponse>>> listBugReports(@CurrentUser User user) {
        return ResponseEntity.ok(ResponseEnvelope.of(bugReportService.listBugReports(user)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bug report by ID", description = "Returns a single bug report by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bug report retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Bug report not found")
    })
    public ResponseEntity<ResponseEnvelope<BugReportResponse>> getBugReport(
            @CurrentUser User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(bugReportService.getBugReport(user, id)));
    }

    @PostMapping("/{id}/attachments")
    @Operation(
            summary = "Upload attachments",
            description =
                    "Uploads screenshot or screen recording files to an existing bug report. Accepts multipart/form-data with one or more files.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Attachments uploaded"),
        @ApiResponse(responseCode = "400", description = "Validation error (file type, size, count)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Bug report not found")
    })
    public ResponseEntity<ResponseEnvelope<List<BugReportAttachmentResponse>>> addAttachments(
            @CurrentUser User user, @PathVariable UUID id, @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.of(bugReportService.addAttachments(user, id, files)));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List attachments", description = "Returns metadata for all attachments on a bug report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Attachments retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Bug report not found")
    })
    public ResponseEntity<ResponseEnvelope<List<BugReportAttachmentResponse>>> listAttachments(
            @CurrentUser User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(bugReportService.listAttachments(user, id)));
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    @Operation(
            summary = "Download attachment",
            description = "Redirects to a short-lived presigned URL for downloading the attachment")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to presigned download URL"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Bug report or attachment not found")
    })
    public ResponseEntity<Void> downloadAttachment(
            @CurrentUser User user, @PathVariable UUID id, @PathVariable UUID attachmentId) {
        String url = bugReportService.getAttachmentDownloadUrl(user, id, attachmentId);
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
    }
}
