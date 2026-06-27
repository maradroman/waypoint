package io.github.maradroman.waypointapi.bugreport.repository;

import io.github.maradroman.waypointapi.bugreport.model.BugReportAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugReportAttachmentRepository extends JpaRepository<BugReportAttachment, UUID> {
    List<BugReportAttachment> findByBugReportId(UUID bugReportId);
}
