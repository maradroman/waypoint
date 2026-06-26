package io.github.maradroman.waypointapi.repository;

import io.github.maradroman.waypointapi.bugreport.model.BugReport;
import io.github.maradroman.waypointapi.bugreport.repository.BugReportRepository;
import io.github.maradroman.waypointapi.testdata.TestDataJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_DESCRIPTION_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.BUG_REPORT_METADATA;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
class BugReportRepositoryTest extends TestDataJpa {

    @Autowired
    private BugReportRepository bugReportRepository;

    @Test
    @DisplayName("findById returns empty when not exists")
    void findById_returnsEmpty_whenNotExistsTest() {
        var actualResult = bugReportRepository.findById(UUID.randomUUID());

        assertThat(actualResult).isEmpty();
    }

    @Test
    @DisplayName("findById returns bug report with metadata when exists")
    void findById_returnsBugReportWithMetadata_whenExistsTest() {
        var user = persistUser(USER_ID);
        var bugReport = BugReport.builder()
                .user(user)
                .description(BUG_REPORT_DESCRIPTION)
                .metadata(BUG_REPORT_METADATA)
                .build();
        em.persist(bugReport);
        em.flush();
        var id = bugReport.getId();
        em.clear();

        var actualResult = bugReportRepository.findById(id);

        assertThat(actualResult).isPresent();
        var loaded = actualResult.get();
        assertThat(loaded)
                .extracting(BugReport::getDescription)
                .isEqualTo(BUG_REPORT_DESCRIPTION);
        assertThat(loaded.getUser().getId()).isEqualTo(USER_ID);
        assertThat(loaded.getMetadata()).containsEntry("url", BUG_REPORT_METADATA.get("url"));
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc returns reports for user newest first")
    void findByUserIdOrderByCreatedAtDesc_returnsReportsNewestFirstTest() {
        var user = persistUser(USER_ID);
        var older = BugReport.builder()
                .user(user)
                .description(BUG_REPORT_DESCRIPTION)
                .metadata(BUG_REPORT_METADATA)
                .build();
        var newer = BugReport.builder()
                .user(user)
                .description(BUG_REPORT_DESCRIPTION_2)
                .metadata(BUG_REPORT_METADATA)
                .build();
        em.persist(older);
        em.persist(newer);
        em.flush();
        jdbcTemplate.update("UPDATE bug_reports SET created_at = ? WHERE id = ?",
                Instant.parse("2024-01-15T10:00:00Z"), older.getId());
        jdbcTemplate.update("UPDATE bug_reports SET created_at = ? WHERE id = ?",
                Instant.parse("2024-02-20T14:30:00Z"), newer.getId());
        em.clear();

        var actualResult = bugReportRepository.findByUserIdOrderByCreatedAtDesc(USER_ID);

        assertThat(actualResult)
                .hasSize(2)
                .extracting(BugReport::getId, BugReport::getDescription)
                .containsExactly(
                        tuple(newer.getId(), BUG_REPORT_DESCRIPTION_2),
                        tuple(older.getId(), BUG_REPORT_DESCRIPTION)
                );
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc returns empty for user with no reports")
    void findByUserIdOrderByCreatedAtDesc_returnsEmpty_whenUserHasNoReportsTest() {
        persistUser(USER_ID);
        em.flush();
        em.clear();

        var actualResult = bugReportRepository.findByUserIdOrderByCreatedAtDesc(USER_ID_2);

        assertThat(actualResult).isEmpty();
    }
}
