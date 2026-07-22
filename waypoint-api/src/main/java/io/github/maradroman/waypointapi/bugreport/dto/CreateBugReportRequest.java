package io.github.maradroman.waypointapi.bugreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Schema(description = "Create bug report request")
public record CreateBugReportRequest(
        @NotBlank
        @Size(max = 10000)
        @Schema(
                description = "Bug description — what happened, what was expected, steps to reproduce",
                example = "Clicked Allocate and the page went blank")
        String description,

        @Schema(description = "Optional client-side metadata for reproduction (user agent, viewport, route, etc.)")
        Map<String, Object> metadata) {}
