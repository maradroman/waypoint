package io.github.maradroman.waypointapi.testdata;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class TestDataConstant {

    // User
    public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final String USER_EMAIL = "alice@example.com";
    public static final String USER_EMAIL_2 = "bob@example.com";
    public static final String USER_DISPLAY_NAME = "Alice";
    public static final String USER_PASSWORD_HASH = "$2a$10$hashedpassword";
    public static final String USER_LOCALE = "en";
    public static final String USER_CURRENCY = "USD";
    public static final String USER_THEME = "light";
    public static final String USER_ROLE = "USER";
    public static final String USER_ROLE_ADMIN = "ADMIN";

    // Goal
    public static final UUID GOAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    public static final UUID GOAL_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    public static final String GOAL_TITLE = "Emergency Fund";
    public static final String GOAL_TITLE_2 = "Vacation";
    public static final String GOAL_DESCRIPTION = "Save for emergencies";
    public static final String GOAL_ICON = "target";
    public static final int GOAL_SORT_ORDER = 0;

    // Milestone
    public static final UUID MILESTONE_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    public static final UUID MILESTONE_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000021");
    public static final String MILESTONE_TITLE = "First $1000";
    public static final String MILESTONE_TITLE_2 = "First $5000";
    public static final int MILESTONE_COST = 100000;
    public static final String MILESTONE_DETAILS = "Save the first $1000";

    // Deposit
    public static final UUID DEPOSIT_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
    public static final UUID DEPOSIT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000031");
    public static final int DEPOSIT_AMOUNT = 50000;
    public static final int DEPOSIT_AMOUNT_2 = 25000;
    public static final String DEPOSIT_NOTE = "Monthly savings";

    // Transfer
    public static final UUID TRANSFER_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");
    public static final UUID TRANSFER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000041");
    public static final int TRANSFER_AMOUNT = 10000;
    public static final int TRANSFER_AMOUNT_2 = 5000;
    public static final String TRANSFER_TYPE_ALLOCATE = "allocate";
    public static final String TRANSFER_TYPE_WITHDRAW = "withdraw";

    // Completion
    public static final UUID COMPLETION_ID = UUID.fromString("00000000-0000-0000-0000-000000000050");
    public static final UUID COMPLETION_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000051");
    public static final int COMPLETION_AMOUNT = 100000;

    // Auth
    public static final String REFRESH_TOKEN_VALUE = "refresh-token-value-123";
    public static final String REFRESH_TOKEN_VALUE_2 = "refresh-token-value-456";
    public static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJpYXQiOjE3MTkzNjAwMDAsImV4cCI6OTk5OTk5OTk5OX0.test";

    // Bug Report
    public static final UUID BUG_REPORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000060");
    public static final UUID BUG_REPORT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000061");
    public static final String BUG_REPORT_DESCRIPTION = "Clicked Allocate and the page went blank";
    public static final String BUG_REPORT_DESCRIPTION_2 = "Progress bar shows 110% on completed goals";
    public static final Map<String, Object> BUG_REPORT_METADATA = Map.of(
            "url", "/goals/00000000-0000-0000-0000-000000000010",
            "userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            "viewport", Map.of("width", 1280, "height", 720)
    );

    // Bug Report Attachment
    public static final UUID ATTACHMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000070");
    public static final String ATTACHMENT_FILENAME = "screenshot.png";
    public static final String ATTACHMENT_CONTENT_TYPE = "image/png";
    public static final long ATTACHMENT_SIZE_BYTES = 204800L;
    public static final String ATTACHMENT_STORAGE_KEY = "bug-reports/00000000-0000-0000-0000-000000000060/uuid-screenshot.png";

    // Timestamps
    public static final Instant DEFAULT_TIMESTAMP = Instant.parse("2024-01-15T10:00:00Z");
    public static final Instant DEFAULT_TIMESTAMP_2 = Instant.parse("2024-02-20T14:30:00Z");
}
