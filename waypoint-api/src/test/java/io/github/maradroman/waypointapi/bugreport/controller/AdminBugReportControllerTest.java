package io.github.maradroman.waypointapi.bugreport.controller;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_FILENAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_METADATA;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_DISPLAY_NAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_EMAIL;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportDetail;
import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportListItem;
import io.github.maradroman.waypointapi.bugreport.service.BugReportService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(AdminBugReportController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AdminBugReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private BugReportService bugReportService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /admin/bug-reports returns 200 with list of all bug reports")
    void listAllBugReports_returns200_whenAdminTest() throws Exception {
        var listItem = new AdminBugReportListItem(
                BUG_REPORT_ID,
                BUG_REPORT_DESCRIPTION,
                DEFAULT_TIMESTAMP,
                new AdminBugReportListItem.ReporterInfo(USER_ID, USER_EMAIL, USER_DISPLAY_NAME),
                2);
        when(bugReportService.listAllBugReports()).thenReturn(List.of(listItem));

        var actualResult = mockMvc.perform(get("/admin/bug-reports"));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(BUG_REPORT_ID.toString()))
                .andExpect(jsonPath("$.data[0].description").value(BUG_REPORT_DESCRIPTION))
                .andExpect(jsonPath("$.data[0].user.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data[0].attachmentCount").value(2))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /admin/bug-reports/{id} returns 200 with detail including attachments")
    void getBugReportDetail_returns200_whenBugReportExistsTest() throws Exception {
        var detail = new AdminBugReportDetail(
                BUG_REPORT_ID,
                BUG_REPORT_DESCRIPTION,
                BUG_REPORT_METADATA,
                DEFAULT_TIMESTAMP,
                new AdminBugReportListItem.ReporterInfo(USER_ID, USER_EMAIL, USER_DISPLAY_NAME),
                List.of(new AdminBugReportDetail.AttachmentWithUrl(
                        io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_ID,
                        ATTACHMENT_FILENAME,
                        "image/png",
                        204800L,
                        DEFAULT_TIMESTAMP,
                        "http://minio:9000/waypoint-bug-reports/presigned-url")));
        when(bugReportService.getBugReportDetail(any())).thenReturn(detail);

        var actualResult = mockMvc.perform(get("/admin/bug-reports/{id}", BUG_REPORT_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(BUG_REPORT_ID.toString()))
                .andExpect(jsonPath("$.data.description").value(BUG_REPORT_DESCRIPTION))
                .andExpect(jsonPath("$.data.metadata.url").exists())
                .andExpect(jsonPath("$.data.user.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.attachments").isArray())
                .andExpect(jsonPath("$.data.attachments[0].filename").value(ATTACHMENT_FILENAME))
                .andExpect(jsonPath("$.data.attachments[0].downloadUrl").exists())
                .andExpect(jsonPath("$.meta").exists());
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {

        @Autowired
        private CurrentUserResolver currentUserResolver;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentUserResolver);
        }
    }
}
