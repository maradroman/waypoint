package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.bugreport.dto.BugReportAttachmentResponse;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportResponse;
import io.github.maradroman.waypointapi.bugreport.dto.CreateBugReportRequest;
import lombok.experimental.UtilityClass;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataBugReportDto {

    public static BugReportResponse bugReportResponse() {
        return new BugReportResponse(BUG_REPORT_ID, BUG_REPORT_DESCRIPTION, BUG_REPORT_METADATA, DEFAULT_TIMESTAMP);
    }

    public static CreateBugReportRequest createBugReportRequest() {
        return new CreateBugReportRequest(BUG_REPORT_DESCRIPTION, BUG_REPORT_METADATA);
    }

    public static CreateBugReportRequest createBugReportRequest(String description) {
        return new CreateBugReportRequest(description, BUG_REPORT_METADATA);
    }

    public static BugReportAttachmentResponse attachmentResponse() {
        return new BugReportAttachmentResponse(ATTACHMENT_ID, ATTACHMENT_FILENAME, ATTACHMENT_CONTENT_TYPE, ATTACHMENT_SIZE_BYTES, DEFAULT_TIMESTAMP);
    }
}
