package io.github.maradroman.waypointapi.analytics.service;

import io.github.maradroman.waypointapi.analytics.dto.GoalAnalyticsResponse;
import io.github.maradroman.waypointapi.analytics.dto.SummaryResponse;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.repository.GoalRepository;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.repository.MilestoneRepository;
import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositEntity.buildDeposit;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildCompletedMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestoneWithCost;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferEntity.buildTransfer;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private TransferRepository transferRepository;
    @Mock private PlannedFundRepository plannedFundRepository;

    @InjectMocks private AnalyticsService analyticsService;

    private User user;
    private User otherUser;
    private Goal goal;

    @BeforeEach
    void setUp() {
        user = buildUser();
        otherUser = buildUser(USER_ID_2);
        goal = buildGoal(user);
        // Mock plannedFundRepository to return empty list by default (lenient for tests that don't use it)
        lenient().when(plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
    }

    @Nested
    @DisplayName("GetGoalAnalytics")
    class GetGoalAnalytics {

        @Test
        @DisplayName("returns analytics with correct calculations")
        void getGoalAnalytics_returnsAnalyticsWithCorrectCalculationsTest() {
            var deposit1 = buildDeposit(goal);
            var deposit2 = buildDeposit(DEPOSIT_ID_2, goal, DEPOSIT_AMOUNT_2, DEFAULT_TIMESTAMP_2);
            var transfer1 = buildTransfer(goal, buildMilestone(goal));
            var milestone1 = buildMilestone(goal);
            var milestone2 = buildCompletedMilestone(goal);

            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
            when(depositRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(deposit1, deposit2));
            when(transferRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(transfer1));
            when(milestoneRepository.findByGoalIdOrderBySortOrderAsc(GOAL_ID)).thenReturn(List.of(milestone1, milestone2));

            var actualResult = analyticsService.getGoalAnalytics(user, GOAL_ID);

            var totalDeposited = DEPOSIT_AMOUNT + DEPOSIT_AMOUNT_2;
            var totalTransferred = TRANSFER_AMOUNT;
            var walletBalance = totalDeposited - totalTransferred;
            var totalMilestoneCost = MILESTONE_COST + MILESTONE_COST;
            var totalMilestoneBalance = TRANSFER_AMOUNT;
            var progressPercent = (int) ((long) totalMilestoneBalance * 100 / totalMilestoneCost);

            assertThat(actualResult)
                .extracting(
                    GoalAnalyticsResponse::goalId,
                    GoalAnalyticsResponse::totalDeposited,
                    GoalAnalyticsResponse::totalAllocated,
                    GoalAnalyticsResponse::walletBalance,
                    GoalAnalyticsResponse::totalMilestoneCost,
                    GoalAnalyticsResponse::totalMilestoneBalance,
                    GoalAnalyticsResponse::completedMilestones,
                    GoalAnalyticsResponse::totalMilestones,
                    GoalAnalyticsResponse::progressPercent,
                    GoalAnalyticsResponse::activeMilestoneId,
                    GoalAnalyticsResponse::activeMilestoneTitle
                )
                .containsExactly(
                    GOAL_ID,
                    totalDeposited,
                    totalTransferred,
                    walletBalance,
                    totalMilestoneCost,
                    totalMilestoneBalance,
                    1,
                    2,
                    progressPercent,
                    milestone1.getId(),
                    milestone1.getTitle()
                );
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when goal not found")
        void getGoalAnalytics_throwsResourceNotFoundException_whenGoalNotFoundTest() {
            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> analyticsService.getGoalAnalytics(user, GOAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when goal not owned by user")
        void getGoalAnalytics_throwsResourceNotFoundException_whenNotOwnedTest() {
            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));

            assertThatThrownBy(() -> analyticsService.getGoalAnalytics(otherUser, GOAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }

        @Test
        @DisplayName("handles empty milestones")
        void getGoalAnalytics_handlesEmptyMilestonesTest() {
            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
            when(depositRepository.findByGoalId(GOAL_ID)).thenReturn(List.of());
            when(transferRepository.findByGoalId(GOAL_ID)).thenReturn(List.of());
            when(milestoneRepository.findByGoalIdOrderBySortOrderAsc(GOAL_ID)).thenReturn(List.of());

            var actualResult = analyticsService.getGoalAnalytics(user, GOAL_ID);

            assertThat(actualResult)
                .extracting(
                    GoalAnalyticsResponse::totalMilestones,
                    GoalAnalyticsResponse::completedMilestones,
                    GoalAnalyticsResponse::totalMilestoneCost,
                    GoalAnalyticsResponse::totalMilestoneBalance,
                    GoalAnalyticsResponse::progressPercent,
                    GoalAnalyticsResponse::activeMilestoneId,
                    GoalAnalyticsResponse::activeMilestoneTitle
                )
                .containsExactly(0, 0, 0, 0, 0, null, null);
        }

        @Test
        @DisplayName("handles negative transfers by filtering them out of total milestone balance")
        void getGoalAnalytics_filtersNegativeTransfersFromMilestoneBalanceTest() {
            var milestone = buildMilestoneWithCost(goal, 1000);
            var positiveTransfer = buildTransfer(goal, milestone);
            positiveTransfer.setAmount(300);
            var negativeTransfer = buildTransfer(goal, milestone);
            negativeTransfer.setAmount(-100);

            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
            when(depositRepository.findByGoalId(GOAL_ID)).thenReturn(List.of());
            when(transferRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(positiveTransfer, negativeTransfer));
            when(milestoneRepository.findByGoalIdOrderBySortOrderAsc(GOAL_ID)).thenReturn(List.of(milestone));

            var actualResult = analyticsService.getGoalAnalytics(user, GOAL_ID);

            assertThat(actualResult)
                .extracting(
                    GoalAnalyticsResponse::totalAllocated,
                    GoalAnalyticsResponse::totalMilestoneBalance,
                    GoalAnalyticsResponse::walletBalance
                )
                .containsExactly(200, 300, -200);
        }

        @Test
        @DisplayName("computes progress percent correctly")
        void getGoalAnalytics_computesProgressPercentCorrectlyTest() {
            var milestone = buildMilestoneWithCost(goal, 1000);
            var transfer = buildTransfer(goal, milestone);
            transfer.setAmount(250);

            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.of(goal));
            when(depositRepository.findByGoalId(GOAL_ID)).thenReturn(List.of());
            when(transferRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(transfer));
            when(milestoneRepository.findByGoalIdOrderBySortOrderAsc(GOAL_ID)).thenReturn(List.of(milestone));

            var actualResult = analyticsService.getGoalAnalytics(user, GOAL_ID);

            assertThat(actualResult)
                .extracting(GoalAnalyticsResponse::progressPercent)
                .isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("GetSummary")
    class GetSummary {

        @Test
        @DisplayName("returns summary across all goals")
        void getSummary_returnsSummaryAcrossAllGoalsTest() {
            var goal2 = buildGoal(GOAL_ID_2, user, GOAL_TITLE_2);
            var deposit1 = buildDeposit(goal);
            var deposit2 = buildDeposit(DEPOSIT_ID_2, goal2, DEPOSIT_AMOUNT_2, DEFAULT_TIMESTAMP_2);
            var milestone1 = buildCompletedMilestone(goal);
            var milestone2 = buildMilestone(goal2);
            milestone2.setCompleted(true);
            var milestone3 = buildMilestone(MILESTONE_ID_2, goal2, MILESTONE_TITLE_2, 1);

            when(goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())).thenReturn(List.of(goal, goal2));
            when(depositRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(deposit1));
            when(depositRepository.findByGoalId(GOAL_ID_2)).thenReturn(List.of(deposit2));
            when(milestoneRepository.findByGoalId(GOAL_ID)).thenReturn(List.of(milestone1));
            when(milestoneRepository.findByGoalId(GOAL_ID_2)).thenReturn(List.of(milestone2, milestone3));

            var actualResult = analyticsService.getSummary(user);

            var totalSaved = DEPOSIT_AMOUNT + DEPOSIT_AMOUNT_2;
            var totalTargets = MILESTONE_COST + MILESTONE_COST + MILESTONE_COST;

            assertThat(actualResult)
                .extracting(SummaryResponse::totalSaved, SummaryResponse::totalTargets, SummaryResponse::activeGoals, SummaryResponse::completedMilestones)
                .containsExactly(totalSaved, totalTargets, 2, 2);
        }

        @Test
        @DisplayName("handles user with no goals")
        void getSummary_handlesUserWithNoGoalsTest() {
            when(goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())).thenReturn(List.of());

            var actualResult = analyticsService.getSummary(user);

            assertThat(actualResult)
                .extracting(SummaryResponse::totalSaved, SummaryResponse::totalTargets, SummaryResponse::activeGoals, SummaryResponse::completedMilestones)
                .containsExactly(0, 0, 0, 0);
        }
    }
}
