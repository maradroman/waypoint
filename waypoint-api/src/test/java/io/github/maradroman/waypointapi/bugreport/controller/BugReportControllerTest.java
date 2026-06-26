package io.github.maradroman.waypointapi.bugreport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportResponse;
import io.github.maradroman.waypointapi.bugreport.service.BugReportService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_FILENAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportDto.attachmentResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportDto.bugReportResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportDto.createBugReportRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BugReportController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class BugReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private BugReportService bugReportService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        User mockUser = TestDataUserEntity.buildUser();
        lenient().when(currentUserResolver.supportsParameter(any())).thenReturn(true);
        lenient().when(currentUserResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    @Test
    @DisplayName("POST /bug-reports returns 201 with created bug report")
    void createBugReport_returns201_whenValidTest() throws Exception {
        when(bugReportService.createBugReport(any(), any())).thenReturn(bugReportResponse());

        var actualResult = mockMvc.perform(post("/bug-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBugReportRequest())));

        actualResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(BUG_REPORT_ID.toString()))
                .andExpect(jsonPath("$.data.description").value(BUG_REPORT_DESCRIPTION))
                .andExpect(jsonPath("$.data.metadata.url").exists())
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /bug-reports returns 400 when description is blank")
    void createBugReport_returns400_whenDescriptionBlankTest() throws Exception {
        var invalidRequest = new io.github.maradroman.waypointapi.bugreport.dto.CreateBugReportRequest("", null);

        var actualResult = mockMvc.perform(post("/bug-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        actualResult.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /bug-reports returns 200 with list of bug reports")
    void listBugReports_returns200_whenAuthenticatedTest() throws Exception {
        when(bugReportService.listBugReports(any())).thenReturn(List.of(bugReportResponse()));

        var actualResult = mockMvc.perform(get("/bug-reports"));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(BUG_REPORT_ID.toString()))
                .andExpect(jsonPath("$.data[0].description").value(BUG_REPORT_DESCRIPTION))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /bug-reports/{id} returns 200 with bug report")
    void getBugReport_returns200_whenBugReportExistsTest() throws Exception {
        when(bugReportService.getBugReport(any(), any())).thenReturn(bugReportResponse());

        var actualResult = mockMvc.perform(get("/bug-reports/{id}", BUG_REPORT_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(BUG_REPORT_ID.toString()))
                .andExpect(jsonPath("$.data.description").value(BUG_REPORT_DESCRIPTION))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /bug-reports/{id}/attachments returns 201 with attachment metadata")
    void addAttachments_returns201_whenFilesUploadedTest() throws Exception {
        when(bugReportService.addAttachments(any(), any(), any())).thenReturn(List.of(attachmentResponse()));

        var file = new MockMultipartFile("files", "screenshot.png", "image/png", new byte[]{1, 2, 3});

        var actualResult = mockMvc.perform(multipart("/bug-reports/{id}/attachments", BUG_REPORT_ID)
                .file(file));

        actualResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].filename").value(ATTACHMENT_FILENAME))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /bug-reports/{id}/attachments returns 200 with attachment list")
    void listAttachments_returns200_whenBugReportExistsTest() throws Exception {
        when(bugReportService.listAttachments(any(), any())).thenReturn(List.of(attachmentResponse()));

        var actualResult = mockMvc.perform(get("/bug-reports/{id}/attachments", BUG_REPORT_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].filename").value(ATTACHMENT_FILENAME))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /bug-reports/{id}/attachments/{attachmentId} returns 302 with Location header")
    void downloadAttachment_returns302_whenAttachmentExistsTest() throws Exception {
        var presignedUrl = "http://minio:9000/waypoint-bug-reports/test-key?X-Amz-Signature=abc";
        when(bugReportService.getAttachmentDownloadUrl(any(), any(), any())).thenReturn(presignedUrl);

        var actualResult = mockMvc.perform(get("/bug-reports/{id}/attachments/{attachmentId}",
                BUG_REPORT_ID, io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_ID));

        actualResult.andExpect(status().isFound())
                .andExpect(redirectedUrl(presignedUrl));
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
