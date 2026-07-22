package io.github.maradroman.waypointapi.milestone.service;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_COST;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.createMilestoneRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.reorderMilestonesRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.updateMilestoneRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildCompletedMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferEntity.buildTransfer;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.dto.MilestoneResponse;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.repository.MilestoneRepository;
import io.github.maradroman.waypointapi.transfer.dto.TransferResponse;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private MilestoneService milestoneService;

    private final User user = buildUser();
    private final User otherUser = buildUser(USER_ID_2);

    @Nested
    @DisplayName("ListMilestones")
    class ListMilestones {

        @Test
        void listMilestones_returnsMilestonesWithBalancesTest() {
            var goal = buildGoal(user);
            var milestone1 = buildMilestone(MILESTONE_ID, goal, MILESTONE_TITLE, 0);
            var milestone2 = buildMilestone(MILESTONE_ID_2, goal, MILESTONE_TITLE_2, 1);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findByGoalIdOrderBySortOrderAsc(goal.getId()))
                    .thenReturn(List.of(milestone1, milestone2));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());

            var actualResult = milestoneService.listMilestones(user, goal.getId());

            assertThat(actualResult)
                    .hasSize(2)
                    .extracting(MilestoneResponse::id, MilestoneResponse::balance)
                    .containsExactly(tuple(milestone1.getId(), 0), tuple(milestone2.getId(), 0));
        }
    }

    @Nested
    @DisplayName("CreateMilestone")
    class CreateMilestone {

        @Test
        void createMilestone_createsMilestoneWithGoalOwnershipCheckTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            var request = createMilestoneRequest(MILESTONE_TITLE, MILESTONE_COST, true);
            when(milestoneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = milestoneService.createMilestone(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::title, MilestoneResponse::cost, MilestoneResponse::balance)
                    .containsExactly(MILESTONE_TITLE, MILESTONE_COST, 0);
        }

        @Test
        void createMilestone_usesDefaultValuesWhenFieldsNullTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            var request = new io.github.maradroman.waypointapi.milestone.dto.CreateMilestoneRequest(
                    "Title", null, null, null);
            when(milestoneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = milestoneService.createMilestone(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::cost, MilestoneResponse::enabled)
                    .containsExactly(0, true);
        }
    }

    @Nested
    @DisplayName("UpdateMilestone")
    class UpdateMilestone {

        @Test
        void updateMilestone_updatesFieldsSelectivelyTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));
            when(milestoneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var request = updateMilestoneRequest("New Title", 50000);
            var actualResult = milestoneService.updateMilestone(user, goal.getId(), milestone.getId(), request);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::title, MilestoneResponse::cost)
                    .containsExactly("New Title", 50000);
        }
    }

    @Nested
    @DisplayName("DeleteMilestone")
    class DeleteMilestone {

        @Test
        void deleteMilestone_deletesMilestoneTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));

            milestoneService.deleteMilestone(user, goal.getId(), milestone.getId());

            verify(milestoneRepository).delete(milestone);
        }
    }

    @Nested
    @DisplayName("UncompleteMilestone")
    class UncompleteMilestone {

        @Test
        void uncompleteMilestone_revertsCompletedMilestoneTest() {
            var goal = buildGoal(user);
            var milestone = buildCompletedMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));
            when(milestoneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var actualResult = milestoneService.uncompleteMilestone(user, goal.getId(), milestone.getId());

            assertThat(actualResult)
                    .extracting(MilestoneResponse::completed, MilestoneResponse::completedAt)
                    .containsExactly(false, null);
        }

        @Test
        void uncompleteMilestone_throwsWhenNotCompletedTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));

            assertThatThrownBy(() -> milestoneService.uncompleteMilestone(user, goal.getId(), milestone.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("ToggleMilestone")
    class ToggleMilestone {

        @Test
        void toggleMilestone_togglesEnabledStatusTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));
            when(milestoneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var actualResult = milestoneService.toggleMilestone(user, goal.getId(), milestone.getId());

            assertThat(actualResult)
                    .extracting(MilestoneResponse::id, MilestoneResponse::enabled)
                    .containsExactly(milestone.getId(), false);
        }
    }

    @Nested
    @DisplayName("ReorderMilestones")
    class ReorderMilestones {

        @Test
        void reorderMilestones_reordersAndReturnsWithBalancesTest() {
            var goal = buildGoal(user);
            var milestone1 = buildMilestone(MILESTONE_ID, goal, MILESTONE_TITLE, 0);
            var milestone2 = buildMilestone(MILESTONE_ID_2, goal, MILESTONE_TITLE_2, 1);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findByGoalId(goal.getId())).thenReturn(List.of(milestone1, milestone2));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());

            var request = reorderMilestonesRequest(MILESTONE_ID_2, MILESTONE_ID);
            var actualResult = milestoneService.reorderMilestones(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::id, MilestoneResponse::sortOrder)
                    .containsExactly(tuple(MILESTONE_ID, 1), tuple(MILESTONE_ID_2, 0));
        }
    }

    @Nested
    @DisplayName("ToggleAllMilestones")
    class ToggleAllMilestones {

        @Test
        void toggleAllMilestones_disablesAllMilestonesTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findByGoalId(goal.getId())).thenReturn(List.of(milestone));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());

            var actualResult = milestoneService.toggleAllMilestones(user, goal.getId(), false);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::id, MilestoneResponse::enabled)
                    .containsExactly(tuple(milestone.getId(), false));
        }

        @Test
        void toggleAllMilestones_enablesAllMilestonesTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            milestone.setEnabled(false);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findByGoalId(goal.getId())).thenReturn(List.of(milestone));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());

            var actualResult = milestoneService.toggleAllMilestones(user, goal.getId(), true);

            assertThat(actualResult)
                    .extracting(MilestoneResponse::id, MilestoneResponse::enabled)
                    .containsExactly(tuple(milestone.getId(), true));
        }
    }

    @Nested
    @DisplayName("GetMilestoneWithBalance")
    class GetMilestoneWithBalance {

        @Test
        void getMilestoneWithBalance_returnsMilestoneWithComputedBalanceTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var actualResult = milestoneService.getMilestoneWithBalance(user, goal.getId(), milestone.getId());

            assertThat(actualResult)
                    .extracting(MilestoneResponse::id, MilestoneResponse::balance)
                    .containsExactly(milestone.getId(), 0);
        }
    }

    @Nested
    @DisplayName("ListMilestoneTransfers")
    class ListMilestoneTransfers {

        @Test
        void listMilestoneTransfers_returnsTransfersForMilestoneTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            var transfer = buildTransfer(goal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of(transfer));

            var actualResult = milestoneService.listMilestoneTransfers(user, goal.getId(), milestone.getId());

            assertThat(actualResult)
                    .hasSize(1)
                    .extracting(TransferResponse::id, TransferResponse::amount)
                    .containsExactly(tuple(transfer.getId(), transfer.getAmount()));
        }
    }

    @Nested
    @DisplayName("FindMilestoneForUser")
    class FindMilestoneForUser {

        @Test
        void findMilestoneForUser_validatesOwnershipTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));

            var actualResult = milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId());

            assertThat(actualResult)
                    .extracting(Milestone::getId, Milestone::getTitle)
                    .containsExactly(milestone.getId(), milestone.getTitle());
        }

        @Test
        void findMilestoneForUser_throwsWhenNotFoundTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(MILESTONE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> milestoneService.findMilestoneForUser(user, goal.getId(), MILESTONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "MILESTONE_NOT_FOUND");
        }

        @Test
        void findMilestoneForUser_throwsWhenNotOwnedTest() {
            var goal = buildGoal(user);
            var otherGoal = buildGoal(GOAL_ID_2, otherUser, GOAL_TITLE_2);
            var milestone = buildMilestone(otherGoal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneRepository.findById(milestone.getId())).thenReturn(Optional.of(milestone));

            assertThatThrownBy(() -> milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "MILESTONE_NOT_FOUND");
        }
    }
}
