package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.completion.model.Completion;
import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public abstract class TestDataJpa {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PersistenceContext
    protected EntityManager em;

    @BeforeEach
    void clearAllData() {
        jdbcTemplate.execute("DELETE FROM completions");
        jdbcTemplate.execute("DELETE FROM transfers");
        jdbcTemplate.execute("DELETE FROM milestones");
        jdbcTemplate.execute("DELETE FROM deposits");
        jdbcTemplate.execute("DELETE FROM planned_funds");
        jdbcTemplate.execute("DELETE FROM bug_report_attachments");
        jdbcTemplate.execute("DELETE FROM bug_reports");
        jdbcTemplate.execute("DELETE FROM goals");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
    }

    protected User persistUser(UUID id, String email) {
        var now = Instant.now();
        jdbcTemplate.update(
                """
                INSERT INTO users (id, email, password_hash, display_name, locale, currency, theme, role, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                email,
                USER_PASSWORD_HASH,
                USER_DISPLAY_NAME,
                USER_LOCALE,
                USER_CURRENCY,
                USER_THEME,
                USER_ROLE,
                now,
                now);
        return em.find(User.class, id);
    }

    protected User persistUser(UUID id) {
        return persistUser(id, USER_EMAIL);
    }

    protected Goal persistGoal(UUID id, UUID userId, String title, int sortOrder) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO goals (id, user_id, title, description, icon, sort_order, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, id, userId, title, GOAL_DESCRIPTION, GOAL_ICON, sortOrder, now, now);
        return em.find(Goal.class, id);
    }

    protected Milestone persistMilestone(UUID id, UUID goalId, String title, int cost, int sortOrder) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO milestones (id, goal_id, title, cost, details, enabled, completed, sort_order, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, goalId, title, cost, "", true, false, sortOrder, now, now);
        return em.find(Milestone.class, id);
    }

    protected Deposit persistDeposit(UUID id, UUID goalId, int amount, Instant timestamp) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO deposits (id, goal_id, amount, note, timestamp, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, goalId, amount, "Test deposit", timestamp, now);
        return em.find(Deposit.class, id);
    }

    protected Transfer persistTransfer(
            UUID id, UUID goalId, UUID milestoneId, int amount, String type, Instant timestamp) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO transfers (id, goal_id, milestone_id, amount, type, comment, timestamp, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, id, goalId, milestoneId, amount, type, "", timestamp, now);
        return em.find(Transfer.class, id);
    }

    protected Completion persistCompletion(UUID id, UUID goalId, UUID milestoneId, int amount, Instant timestamp) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO completions (id, goal_id, milestone_id, amount, timestamp, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, goalId, milestoneId, amount, timestamp, now);
        return em.find(Completion.class, id);
    }

    protected PlannedFund persistPlannedFund(UUID id, UUID goalId, LocalDate date, int amount, boolean isDeleted) {
        var now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO planned_funds (id, goal_id, date, amount, is_deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, goalId, date, amount, isDeleted, now, now);
        return em.find(PlannedFund.class, id);
    }

    protected PlannedFund persistPlannedFund(UUID id, UUID goalId, LocalDate date, int amount) {
        return persistPlannedFund(id, goalId, date, amount, false);
    }

    protected void flushAndClear() {
        em.clear();
    }
}
