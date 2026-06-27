package io.github.maradroman.waypointapi.transfer.service;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_TYPE_ALLOCATE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositEntity.buildDeposit;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestone;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneEntity.buildMilestoneWithCost;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.allocateRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.updateTransferRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.withdrawRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferEntity.buildTransfer;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.BadRequestException;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
import io.github.maradroman.waypointapi.transfer.dto.AllocateResponse;
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
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private GoalService goalService;

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private TransferService transferService;

    private final User user = buildUser();
    private final User otherUser = buildUser(USER_ID_2);

    @Nested
    @DisplayName("ListTransfers")
    class ListTransfers {

        @Test
        void listTransfers_returnsTransfersTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            var transfer = buildTransfer(goal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(transferRepository.findByGoalIdOrderByTimestampDesc(goal.getId()))
                    .thenReturn(List.of(transfer));

            var actualResult = transferService.listTransfers(user, goal.getId());

            assertThat(actualResult)
                    .hasSize(1)
                    .extracting(TransferResponse::id, TransferResponse::amount)
                    .containsExactly(tuple(transfer.getId(), transfer.getAmount()));
        }
    }

    @Nested
    @DisplayName("Allocate")
    class Allocate {

        @Test
        void allocate_createsTransferWhenEnoughFundsTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestoneWithCost(goal, 100000);
            var deposit = buildDeposit(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .thenReturn(milestone);
            when(depositRepository.findByGoalId(goal.getId())).thenReturn(List.of(deposit));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var request = allocateRequest(milestone.getId(), 10000);
            var actualResult = transferService.allocate(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(AllocateResponse::applied, AllocateResponse::requested)
                    .containsExactly(10000, 10000);
            verify(transferRepository).save(any());
        }

        @Test
        void allocate_throwsBadRequestExceptionWhenInsufficientFundsTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestoneWithCost(goal, 100000);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .thenReturn(milestone);
            when(depositRepository.findByGoalId(goal.getId())).thenReturn(List.of());
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of());

            var request = allocateRequest(milestone.getId(), 10000);

            assertThatThrownBy(() -> transferService.allocate(user, goal.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("code", "ALLOCATION_FAILED");
        }

        @Test
        void allocate_capsAtRemainingNeedTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestoneWithCost(goal, 1000);
            var deposit = buildDeposit(DEPOSIT_ID, goal, 2000, DEFAULT_TIMESTAMP);
            var existingTransfer =
                    buildTransfer(TRANSFER_ID, goal, milestone, 400, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .thenReturn(milestone);
            when(depositRepository.findByGoalId(goal.getId())).thenReturn(List.of(deposit));
            when(transferRepository.findByGoalId(goal.getId())).thenReturn(List.of(existingTransfer));
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of(existingTransfer));

            var request = allocateRequest(milestone.getId(), 1000);
            var actualResult = transferService.allocate(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(AllocateResponse::applied, AllocateResponse::requested)
                    .containsExactly(600, 1000);
        }
    }

    @Nested
    @DisplayName("Withdraw")
    class Withdraw {

        @Test
        void withdraw_createsWithdrawalTransferTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            var existingTransfer = buildTransfer(goal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .thenReturn(milestone);
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of(existingTransfer));

            var request = withdrawRequest(milestone.getId(), 5000);
            var actualResult = transferService.withdraw(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(AllocateResponse::applied, AllocateResponse::requested)
                    .containsExactly(5000, 5000);
            verify(transferRepository).save(argThat(t -> t.getAmount() == -5000 && "withdraw".equals(t.getType())));
        }

        @Test
        void withdraw_throwsWhenNoFundsAllocatedTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(milestoneService.findMilestoneForUser(user, goal.getId(), milestone.getId()))
                    .thenReturn(milestone);
            when(transferRepository.findByMilestoneIdOrderByTimestampDesc(milestone.getId()))
                    .thenReturn(List.of());

            var request = withdrawRequest(milestone.getId(), 5000);

            assertThatThrownBy(() -> transferService.withdraw(user, goal.getId(), request))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("code", "WITHDRAWAL_FAILED");
        }
    }

    @Nested
    @DisplayName("UpdateTransfer")
    class UpdateTransfer {

        @Test
        void updateTransfer_updatesAmountTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            var transfer = buildTransfer(goal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(transferRepository.findById(transfer.getId())).thenReturn(Optional.of(transfer));
            when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = updateTransferRequest(75000);
            var actualResult = transferService.updateTransfer(user, goal.getId(), transfer.getId(), request);

            assertThat(actualResult).extracting(TransferResponse::amount).isEqualTo(75000);
        }
    }

    @Nested
    @DisplayName("DeleteTransfer")
    class DeleteTransfer {

        @Test
        void deleteTransfer_deletesTransferTest() {
            var goal = buildGoal(user);
            var milestone = buildMilestone(goal);
            var transfer = buildTransfer(goal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(transferRepository.findById(transfer.getId())).thenReturn(Optional.of(transfer));

            transferService.deleteTransfer(user, goal.getId(), transfer.getId());

            verify(transferRepository).delete(transfer);
        }

        @Test
        void deleteTransfer_throwsResourceNotFoundExceptionWhenNotOwnedTest() {
            var goal = buildGoal(user);
            var otherGoal = buildGoal(GOAL_ID_2, user, GOAL_TITLE_2);
            var milestone = buildMilestone(otherGoal);
            var transfer = buildTransfer(otherGoal, milestone);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(transferRepository.findById(transfer.getId())).thenReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.deleteTransfer(user, goal.getId(), transfer.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "TRANSFER_NOT_FOUND");
        }
    }
}
