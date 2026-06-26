package io.github.maradroman.waypointapi.bugreport.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportAttachmentResponse;
import io.github.maradroman.waypointapi.bugreport.dto.BugReportResponse;
import io.github.maradroman.waypointapi.bugreport.dto.CreateBugReportRequest;
import io.github.maradroman.waypointapi.bugreport.model.BugReport;
import io.github.maradroman.waypointapi.bugreport.model.BugReportAttachment;
import io.github.maradroman.waypointapi.bugreport.repository.BugReportAttachmentRepository;
import io.github.maradroman.waypointapi.bugreport.repository.BugReportRepository;
import io.github.maradroman.waypointapi.common.exception.BadRequestException;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.common.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_FILENAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.ATTACHMENT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_METADATA;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportDto.createBugReportRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportEntity.buildAttachment;
import static io.github.maradroman.waypointapi.testdata.TestDataBugReportEntity.buildBugReport;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BugReportServiceTest {

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private BugReportAttachmentRepository attachmentRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private BugReportService bugReportService;

    private final User user = buildUser();
    private final User otherUser = buildUser(USER_ID_2);

    @Nested
    @DisplayName("CreateBugReport")
    class CreateBugReport {

        @Test
        void createBugReport_savesAndReturnsBugReportTest() {
            var request = createBugReportRequest();
            when(bugReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = bugReportService.createBugReport(user, request);

            assertThat(actualResult)
                    .extracting(BugReportResponse::description, BugReportResponse::metadata)
                    .containsExactly(BUG_REPORT_DESCRIPTION, BUG_REPORT_METADATA);
        }

        @Test
        void createBugReport_usesEmptyMapWhenMetadataNullTest() {
            var request = new CreateBugReportRequest(BUG_REPORT_DESCRIPTION, null);
            when(bugReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = bugReportService.createBugReport(user, request);

            assertThat(actualResult)
                    .extracting(BugReportResponse::description, BugReportResponse::metadata)
                    .containsExactly(BUG_REPORT_DESCRIPTION, Map.of());
        }
    }

    @Nested
    @DisplayName("ListBugReports")
    class ListBugReports {

        @Test
        void listBugReports_returnsReportsForUserNewestFirstTest() {
            var report1 = buildBugReport(user);
            var report2 = buildBugReport(USER_ID_2, user, "Second report");
            when(bugReportRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                    .thenReturn(List.of(report2, report1));

            var actualResult = bugReportService.listBugReports(user);

            assertThat(actualResult)
                    .hasSize(2)
                    .extracting(BugReportResponse::id, BugReportResponse::description)
                    .containsExactly(
                            tuple(report2.getId(), report2.getDescription()),
                            tuple(report1.getId(), report1.getDescription())
                    );
        }

        @Test
        void listBugReports_returnsEmptyListWhenNoneSubmittedTest() {
            when(bugReportRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                    .thenReturn(List.of());

            var actualResult = bugReportService.listBugReports(user);

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetBugReport")
    class GetBugReport {

        @Test
        void getBugReport_returnsReportWhenFoundAndOwnedByUserTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            var actualResult = bugReportService.getBugReport(user, bugReport.getId());

            assertThat(actualResult)
                    .extracting(BugReportResponse::id, BugReportResponse::description)
                    .containsExactly(bugReport.getId(), bugReport.getDescription());
        }

        @Test
        void getBugReport_throwsResourceNotFoundExceptionWhenNotFoundTest() {
            when(bugReportRepository.findById(BUG_REPORT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bugReportService.getBugReport(user, BUG_REPORT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }

        @Test
        void getBugReport_throwsResourceNotFoundExceptionWhenOwnedByDifferentUserTest() {
            var bugReport = buildBugReport(otherUser);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            assertThatThrownBy(() -> bugReportService.getBugReport(user, bugReport.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("FindBugReportForUser")
    class FindBugReportForUser {

        @Test
        void findBugReportForUser_returnsReportWhenOwnedByUserTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            var actualResult = bugReportService.findBugReportForUser(user, bugReport.getId());

            assertThat(actualResult)
                    .extracting(BugReport::getId, BugReport::getDescription)
                    .containsExactly(bugReport.getId(), bugReport.getDescription());
        }

        @Test
        void findBugReportForUser_throwsResourceNotFoundExceptionWhenNotFoundTest() {
            when(bugReportRepository.findById(BUG_REPORT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bugReportService.findBugReportForUser(user, BUG_REPORT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }

        @Test
        void findBugReportForUser_throwsResourceNotFoundExceptionWhenOwnedByDifferentUserTest() {
            var bugReport = buildBugReport(otherUser);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            assertThatThrownBy(() -> bugReportService.findBugReportForUser(user, bugReport.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("AddAttachments")
    class AddAttachments {

        @Test
        void addAttachments_storesFilesAndReturnsResponsesTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            when(attachmentRepository.findByBugReportId(bugReport.getId())).thenReturn(List.of());
            when(attachmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var file = new MockMultipartFile("files", "screenshot.png", "image/png", new byte[]{1, 2, 3});
            var actualResult = bugReportService.addAttachments(user, bugReport.getId(), List.<MultipartFile>of(file));

            assertThat(actualResult).hasSize(1);
            assertThat(actualResult.get(0))
                    .extracting(BugReportAttachmentResponse::filename, BugReportAttachmentResponse::contentType)
                    .containsExactly("screenshot.png", "image/png");
            verify(storageService).store(anyString(), any(), anyLong(), eq("image/png"));
        }

        @Test
        void addAttachments_throwsWhenNoFilesTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            assertThatThrownBy(() -> bugReportService.addAttachments(user, bugReport.getId(), List.of()))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("code", "NO_FILES");
        }

        @Test
        void addAttachments_throwsWhenTooManyAttachmentsTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            var existing = java.util.stream.Stream.generate(() -> buildAttachment(bugReport))
                    .limit(10)
                    .toList();
            when(attachmentRepository.findByBugReportId(bugReport.getId())).thenReturn(existing);

            List<MultipartFile> files = List.of(
                    new MockMultipartFile("files", "a.png", "image/png", new byte[]{1}),
                    new MockMultipartFile("files", "b.png", "image/png", new byte[]{1})
            );

            assertThatThrownBy(() -> bugReportService.addAttachments(user, bugReport.getId(), files))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("code", "TOO_MANY_ATTACHMENTS");
        }

        @Test
        void addAttachments_throwsWhenUnsupportedContentTypeTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            when(attachmentRepository.findByBugReportId(bugReport.getId())).thenReturn(List.of());

            var file = new MockMultipartFile("files", "doc.pdf", "application/pdf", new byte[]{1, 2});
            assertThatThrownBy(() -> bugReportService.addAttachments(user, bugReport.getId(), List.<MultipartFile>of(file)))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("code", "UNSUPPORTED_FILE_TYPE");
            verify(storageService, never()).store(anyString(), any(), anyLong(), anyString());
        }

        @Test
        void addAttachments_throwsWhenBugReportNotFoundTest() {
            when(bugReportRepository.findById(BUG_REPORT_ID)).thenReturn(Optional.empty());

            var file = new MockMultipartFile("files", "screenshot.png", "image/png", new byte[]{1});
            assertThatThrownBy(() -> bugReportService.addAttachments(user, BUG_REPORT_ID, List.<MultipartFile>of(file)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }

        @Test
        void addAttachments_throwsWhenBugReportOwnedByDifferentUserTest() {
            var bugReport = buildBugReport(otherUser);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            var file = new MockMultipartFile("files", "screenshot.png", "image/png", new byte[]{1});
            assertThatThrownBy(() -> bugReportService.addAttachments(user, bugReport.getId(), List.<MultipartFile>of(file)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("ListAttachments")
    class ListAttachments {

        @Test
        void listAttachments_returnsAttachmentsForOwnedBugReportTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            var attachment = buildAttachment(bugReport);
            when(attachmentRepository.findByBugReportId(bugReport.getId())).thenReturn(List.of(attachment));

            var actualResult = bugReportService.listAttachments(user, bugReport.getId());

            assertThat(actualResult)
                    .hasSize(1)
                    .extracting(BugReportAttachmentResponse::filename)
                    .containsExactly(ATTACHMENT_FILENAME);
        }

        @Test
        void listAttachments_throwsWhenBugReportNotOwnedTest() {
            var bugReport = buildBugReport(otherUser);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));

            assertThatThrownBy(() -> bugReportService.listAttachments(user, bugReport.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "BUG_REPORT_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("GetAttachmentDownloadUrl")
    class GetAttachmentDownloadUrl {

        @Test
        void getAttachmentDownloadUrl_returnsPresignedUrlTest() {
            var bugReport = buildBugReport(user);
            var attachment = buildAttachment(bugReport);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            when(attachmentRepository.findById(attachment.getId())).thenReturn(Optional.of(attachment));
            when(storageService.getPresignedDownloadUrl(attachment.getStorageKey()))
                    .thenReturn("http://minio/presigned-url");

            var actualResult = bugReportService.getAttachmentDownloadUrl(user, bugReport.getId(), attachment.getId());

            assertThat(actualResult).isEqualTo("http://minio/presigned-url");
        }

        @Test
        void getAttachmentDownloadUrl_throwsWhenAttachmentNotFoundTest() {
            var bugReport = buildBugReport(user);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            when(attachmentRepository.findById(ATTACHMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bugReportService.getAttachmentDownloadUrl(user, bugReport.getId(), ATTACHMENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "ATTACHMENT_NOT_FOUND");
        }

        @Test
        void getAttachmentDownloadUrl_throwsWhenAttachmentBelongsToDifferentBugReportTest() {
            var bugReport = buildBugReport(user);
            var otherBugReport = buildBugReport(BUG_REPORT_ID_2, user, "Other report");
            var attachment = buildAttachment(otherBugReport);
            when(bugReportRepository.findById(bugReport.getId())).thenReturn(Optional.of(bugReport));
            when(attachmentRepository.findById(attachment.getId())).thenReturn(Optional.of(attachment));

            assertThatThrownBy(() -> bugReportService.getAttachmentDownloadUrl(user, bugReport.getId(), attachment.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "ATTACHMENT_NOT_FOUND");
        }
    }
}
