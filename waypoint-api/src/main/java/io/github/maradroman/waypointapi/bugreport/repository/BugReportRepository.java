package io.github.maradroman.waypointapi.bugreport.repository;

import io.github.maradroman.waypointapi.bugreport.model.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BugReportRepository extends JpaRepository<BugReport, UUID> {
    List<BugReport> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<BugReport> findAllByOrderByCreatedAtDesc();
}
