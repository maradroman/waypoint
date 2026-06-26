package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.bugreport.model.BugReport;
import io.github.maradroman.waypointapi.bugreport.model.BugReportAttachment;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataBugReportEntity {

    public static BugReport buildBugReport(User user) {
        return BugReport.builder()
                .id(BUG_REPORT_ID)
                .user(user)
                .description(BUG_REPORT_DESCRIPTION)
                .metadata(BUG_REPORT_METADATA)
                .build();
    }

    public static BugReport buildBugReport(UUID id, User user, String description) {
        return BugReport.builder()
                .id(id)
                .user(user)
                .description(description)
                .metadata(BUG_REPORT_METADATA)
                .build();
    }

    public static BugReportAttachment buildAttachment(BugReport bugReport) {
        return BugReportAttachment.builder()
                .id(ATTACHMENT_ID)
                .bugReport(bugReport)
                .filename(ATTACHMENT_FILENAME)
                .contentType(ATTACHMENT_CONTENT_TYPE)
                .sizeBytes(ATTACHMENT_SIZE_BYTES)
                .storageKey(ATTACHMENT_STORAGE_KEY)
                .build();
    }
}
