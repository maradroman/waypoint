package io.github.maradroman.waypointapi.bugreport.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportDetail;
import io.github.maradroman.waypointapi.bugreport.dto.AdminBugReportListItem;
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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportRepository bugReportRepository;
    private final BugReportAttachmentRepository attachmentRepository;
    private final StorageService storageService;

    private static final int MAX_ATTACHMENTS = 10;
    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024;

    public BugReportResponse createBugReport(User user, CreateBugReportRequest request) {
        BugReport bugReport = BugReport.builder()
                .user(user)
                .description(request.description())
                .metadata(request.metadata() != null ? request.metadata() : Map.of())
                .build();
        bugReport = bugReportRepository.save(bugReport);
        return BugReportResponse.from(bugReport);
    }

    @Transactional(readOnly = true)
    public List<BugReportResponse> listBugReports(User user) {
        return bugReportRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BugReportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BugReportResponse getBugReport(User user, UUID bugReportId) {
        BugReport bugReport = findBugReportForUser(user, bugReportId);
        return BugReportResponse.from(bugReport);
    }

    public List<BugReportAttachmentResponse> addAttachments(User user, UUID bugReportId, List<MultipartFile> files) {
        BugReport bugReport = findBugReportForUser(user, bugReportId);

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("NO_FILES", "At least one file must be provided");
        }

        long existingCount = attachmentRepository.findByBugReportId(bugReportId).size();
        if (existingCount + files.size() > MAX_ATTACHMENTS) {
            throw new BadRequestException(
                    "TOO_MANY_ATTACHMENTS", "Maximum " + MAX_ATTACHMENTS + " attachments per bug report");
        }

        return files.stream()
                .map(file -> {
                    validateFile(file);
                    String storageKey = buildStorageKey(bugReportId, file.getOriginalFilename());
                    try {
                        storageService.store(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
                    } catch (IOException e) {
                        throw new BadRequestException(
                                "UPLOAD_FAILED", "Could not read file: " + file.getOriginalFilename());
                    }
                    BugReportAttachment attachment = BugReportAttachment.builder()
                            .bugReport(bugReport)
                            .filename(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .sizeBytes(file.getSize())
                            .storageKey(storageKey)
                            .build();
                    attachment = attachmentRepository.save(attachment);
                    return BugReportAttachmentResponse.from(attachment);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BugReportAttachmentResponse> listAttachments(User user, UUID bugReportId) {
        findBugReportForUser(user, bugReportId);
        return attachmentRepository.findByBugReportId(bugReportId).stream()
                .map(BugReportAttachmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getAttachmentDownloadUrl(User user, UUID bugReportId, UUID attachmentId) {
        findBugReportForUser(user, bugReportId);
        BugReportAttachment attachment = attachmentRepository
                .findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ATTACHMENT_NOT_FOUND", "Attachment not found"));
        if (!attachment.getBugReport().getId().equals(bugReportId)) {
            throw new ResourceNotFoundException("ATTACHMENT_NOT_FOUND", "Attachment not found");
        }
        return storageService.getPresignedDownloadUrl(attachment.getStorageKey());
    }

    public BugReport findBugReportForUser(User user, UUID bugReportId) {
        BugReport bugReport = bugReportRepository
                .findById(bugReportId)
                .orElseThrow(() -> new ResourceNotFoundException("BUG_REPORT_NOT_FOUND", "Bug report not found"));
        if (!bugReport.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("BUG_REPORT_NOT_FOUND", "Bug report not found");
        }
        return bugReport;
    }

    @Transactional(readOnly = true)
    public List<AdminBugReportListItem> listAllBugReports() {
        return bugReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(report -> {
                    int count = attachmentRepository
                            .findByBugReportId(report.getId())
                            .size();
                    return new AdminBugReportListItem(
                            report.getId(),
                            report.getDescription(),
                            report.getCreatedAt(),
                            new AdminBugReportListItem.ReporterInfo(
                                    report.getUser().getId(),
                                    report.getUser().getEmail(),
                                    report.getUser().getDisplayName()),
                            count);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminBugReportDetail getBugReportDetail(UUID bugReportId) {
        BugReport bugReport = bugReportRepository
                .findById(bugReportId)
                .orElseThrow(() -> new ResourceNotFoundException("BUG_REPORT_NOT_FOUND", "Bug report not found"));
        List<BugReportAttachment> attachments = attachmentRepository.findByBugReportId(bugReportId);
        var attachmentDtos = attachments.stream()
                .map(att -> new AdminBugReportDetail.AttachmentWithUrl(
                        att.getId(),
                        att.getFilename(),
                        att.getContentType(),
                        att.getSizeBytes(),
                        att.getCreatedAt(),
                        storageService.getPresignedDownloadUrl(att.getStorageKey())))
                .toList();
        return new AdminBugReportDetail(
                bugReport.getId(),
                bugReport.getDescription(),
                bugReport.getMetadata(),
                bugReport.getCreatedAt(),
                new AdminBugReportListItem.ReporterInfo(
                        bugReport.getUser().getId(),
                        bugReport.getUser().getEmail(),
                        bugReport.getUser().getDisplayName()),
                attachmentDtos);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("EMPTY_FILE", "File is empty: " + file.getOriginalFilename());
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("FILE_TOO_LARGE", "File exceeds 100MB limit: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new BadRequestException("UNSUPPORTED_FILE_TYPE", "Only image and video files are accepted");
        }
    }

    private String buildStorageKey(UUID bugReportId, String filename) {
        return "bug-reports/" + bugReportId + "/" + UUID.randomUUID() + "-" + filename;
    }
}
