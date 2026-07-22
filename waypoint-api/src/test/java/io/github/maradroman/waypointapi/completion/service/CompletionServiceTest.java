package io.github.maradroman.waypointapi.completion.service;

import static io.github.maradroman.waypointapi.testdata.TestDataCompletionEntity.buildCompletion;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferEntity.buildTransfer;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.completion.dto.CompletionResponse;
import io.github.maradroman.waypointapi.completion.model.Completion;
import io.github.maradroman.waypointapi.completion.repository.CompletionRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletionServiceTest {

    @Mock
    private CompletionRepository completionRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private GoalService goalService;

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private CompletionService completionService;

    private User user;
    private Goal goal;
    private Milestone milestone;

    @Captor
    private ArgumentCaptor<Completion> completionCaptor;

    @BeforeEach
    void setUp() {
        user = buildUser();
        goal = buildGoal(user);
        milestone = buildMilestone(goal);
    }

    @Nested
    @DisplayName("CreateCompletion")
    class CreateCompletion {

        @Test
        @DisplayName("creates completion and marks milestone completed")
        void createCompletion_createsCompletionAndMarksMilestoneCompletedTest() {
            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, GOAL_ID, MILESTONE_ID))
                    .thenReturn(milestone);
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(MILESTONE_ID))
                    .thenReturn(List.of());
            when(completionRepository.save(completionCaptor.capture())).thenAnswer(invocation -> {
                var c = invocation.<Completion>getArgument(0);
                c.setId(COMPLETION_ID);
                return c;
            });

            var actualResult = completionService.createCompletion(user, GOAL_ID, MILESTONE_ID);

            assertThat(completionCaptor.getValue())
                    .extracting(Completion::getGoal, Completion::getMilestone, Completion::getAmount)
                    .containsExactly(goal, milestone, 0);
            assertThat(milestone.getCompleted()).isTrue();
            assertThat(milestone.getCompletedAt()).isNotNull();
            assertThat(actualResult)
                    .extracting(
                            CompletionResponse::id,
                            CompletionResponse::goalId,
                            CompletionResponse::milestoneId,
                            CompletionResponse::amount)
                    .containsExactly(COMPLETION_ID, GOAL_ID, MILESTONE_ID, 0);
        }

        @Test
        @DisplayName("computes milestone balance from transfers")
        void createCompletion_computesMilestoneBalanceFromTransfersTest() {
            var transfer1 = buildTransfer(goal, milestone);
            var transfer2 = buildTransfer(
                    TRANSFER_ID_2, goal, milestone, TRANSFER_AMOUNT_2, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP_2);

            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, GOAL_ID, MILESTONE_ID))
                    .thenReturn(milestone);
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(MILESTONE_ID))
                    .thenReturn(List.of(transfer1, transfer2));
            when(completionRepository.save(completionCaptor.capture())).thenAnswer(invocation -> {
                var c = invocation.<Completion>getArgument(0);
                c.setId(COMPLETION_ID);
                return c;
            });

            var actualResult = completionService.createCompletion(user, GOAL_ID, MILESTONE_ID);

            assertThat(completionCaptor.getValue())
                    .extracting(Completion::getAmount)
                    .isEqualTo(TRANSFER_AMOUNT + TRANSFER_AMOUNT_2);
            assertThat(actualResult)
                    .extracting(CompletionResponse::amount)
                    .isEqualTo(TRANSFER_AMOUNT + TRANSFER_AMOUNT_2);
        }
    }

    @Nested
    @DisplayName("ListCompletions")
    class ListCompletions {

        @Test
        @DisplayName("returns completions for goal")
        void listCompletions_returnsCompletionsForGoalTest() {
            var completion = buildCompletion(goal, milestone);
            var anotherCompletion =
                    buildCompletion(COMPLETION_ID_2, goal, milestone, COMPLETION_AMOUNT, DEFAULT_TIMESTAMP_2);

            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(completionRepository.findByGoalIdOrderByTimestampDesc(GOAL_ID))
                    .thenReturn(List.of(completion, anotherCompletion));

            var actualResult = completionService.listCompletions(user, GOAL_ID);

            assertThat(actualResult).extracting(CompletionResponse::id).containsExactly(COMPLETION_ID, COMPLETION_ID_2);
        }
    }

    @Nested
    @DisplayName("DeleteCompletion")
    class DeleteCompletion {

        @Test
        @DisplayName("deletes completion and reverts milestone")
        void deleteCompletion_deletesCompletionAndRevertsMilestoneTest() {
            var completedMilestone = buildMilestone(goal);
            completedMilestone.setCompleted(true);
            completedMilestone.setCompletedAt(DEFAULT_TIMESTAMP);
            var completion = buildCompletion(goal, completedMilestone);

            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(completionRepository.findById(COMPLETION_ID)).thenReturn(Optional.of(completion));

            completionService.deleteCompletion(user, GOAL_ID, COMPLETION_ID);

            assertThat(completedMilestone.getCompleted()).isFalse();
            assertThat(completedMilestone.getCompletedAt()).isNull();
            verify(completionRepository).delete(completion);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void deleteCompletion_throwsResourceNotFoundException_whenNotFoundTest() {
            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(completionRepository.findById(COMPLETION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> completionService.deleteCompletion(user, GOAL_ID, COMPLETION_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "COMPLETION_NOT_FOUND");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not owned")
        void deleteCompletion_throwsResourceNotFoundException_whenNotOwnedTest() {
            var otherGoal = buildGoal(GOAL_ID_2, user, GOAL_TITLE_2);
            var completion = buildCompletion(otherGoal, milestone);

            when(goalService.findGoalForUser(user, GOAL_ID)).thenReturn(goal);
            when(completionRepository.findById(COMPLETION_ID)).thenReturn(Optional.of(completion));

            assertThatThrownBy(() -> completionService.deleteCompletion(user, GOAL_ID, COMPLETION_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "COMPLETION_NOT_FOUND");
        }
    }
}
