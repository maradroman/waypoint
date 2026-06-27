package io.github.maradroman.waypointapi.bugreport.controller;

import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportDetail;
import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportListItem;
import io.github.maradroman.waypointapi.bugreport.service.BugReportService;
import io.github.maradroman.waypointapi.common.util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/bug-reports")
@RequiredArgsConstructor
@Tag(name = "Admin — Bug Reports", description = "Admin endpoints for reviewing all user bug reports")
public class AdminBugReportController {

    private final BugReportService bugReportService;

    @GetMapping
    @Operation(
            summary = "List all bug reports",
            description = "Returns all bug reports across all users, newest first. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bug reports retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — admin role required")
    })
    public ResponseEntity<ResponseEnvelope<List<AdminBugReportListItem>>> listAllBugReports() {
        return ResponseEntity.ok(ResponseEnvelope.of(bugReportService.listAllBugReports()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get bug report detail",
            description =
                    "Returns a single bug report with metadata, reporter info, and attachments with presigned download URLs. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bug report retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — admin role required"),
        @ApiResponse(responseCode = "404", description = "Bug report not found")
    })
    public ResponseEntity<ResponseEnvelope<AdminBugReportDetail>> getBugReportDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseEnvelope.of(bugReportService.getBugReportDetail(id)));
    }
}
