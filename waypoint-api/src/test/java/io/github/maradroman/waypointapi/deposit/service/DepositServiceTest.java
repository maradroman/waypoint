package io.github.maradroman.waypointapi.deposit.service;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_AMOUNT_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositDto.createDepositRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositDto.updateDepositRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositEntity.buildDeposit;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.deposit.dto.DepositResponse;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.service.GoalService;
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
class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private DepositService depositService;

    private final User user = buildUser();
    private final User otherUser = buildUser(USER_ID_2);

    @Nested
    @DisplayName("ListDeposits")
    class ListDeposits {

        @Test
        void listDeposits_returnsDepositsTest() {
            var goal = buildGoal(user);
            var deposit = buildDeposit(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(depositRepository.findByGoalIdOrderByTimestampDesc(goal.getId()))
                    .thenReturn(List.of(deposit));

            var actualResult = depositService.listDeposits(user, goal.getId());

            assertThat(actualResult)
                    .hasSize(1)
                    .extracting(DepositResponse::id, DepositResponse::amount)
                    .containsExactly(tuple(deposit.getId(), deposit.getAmount()));
        }
    }

    @Nested
    @DisplayName("CreateDeposit")
    class CreateDeposit {

        @Test
        void createDeposit_createsDepositWithGoalTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            var request = createDepositRequest(DEPOSIT_AMOUNT);
            when(depositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = depositService.createDeposit(user, goal.getId(), request);

            assertThat(actualResult).extracting(DepositResponse::amount).isEqualTo(DEPOSIT_AMOUNT);
        }

        @Test
        void createDeposit_usesDefaultNoteWhenNullTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            var request = new io.github.maradroman.waypointapi.deposit.dto.CreateDepositRequest(DEPOSIT_AMOUNT, null);
            when(depositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = depositService.createDeposit(user, goal.getId(), request);

            assertThat(actualResult).extracting(DepositResponse::amount).isEqualTo(DEPOSIT_AMOUNT);
        }
    }

    @Nested
    @DisplayName("UpdateDeposit")
    class UpdateDeposit {

        @Test
        void updateDeposit_updatesAmountTest() {
            var goal = buildGoal(user);
            var deposit = buildDeposit(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(depositRepository.findById(deposit.getId())).thenReturn(Optional.of(deposit));
            when(depositRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = updateDepositRequest(DEPOSIT_AMOUNT_2);
            var actualResult = depositService.updateDeposit(user, goal.getId(), deposit.getId(), request);

            assertThat(actualResult).extracting(DepositResponse::amount).isEqualTo(DEPOSIT_AMOUNT_2);
        }
    }

    @Nested
    @DisplayName("DeleteDeposit")
    class DeleteDeposit {

        @Test
        void deleteDeposit_deletesDepositTest() {
            var goal = buildGoal(user);
            var deposit = buildDeposit(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(depositRepository.findById(deposit.getId())).thenReturn(Optional.of(deposit));

            depositService.deleteDeposit(user, goal.getId(), deposit.getId());

            verify(depositRepository).delete(deposit);
        }

        @Test
        void deleteDeposit_throwsResourceNotFoundExceptionWhenNotFoundTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(depositRepository.findById(DEPOSIT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> depositService.deleteDeposit(user, goal.getId(), DEPOSIT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "DEPOSIT_NOT_FOUND");
        }

        @Test
        void deleteDeposit_throwsResourceNotFoundExceptionWhenNotOwnedTest() {
            var goal = buildGoal(user);
            var otherGoal = buildGoal(GOAL_ID_2, otherUser, GOAL_TITLE_2);
            var deposit = buildDeposit(otherGoal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(depositRepository.findById(deposit.getId())).thenReturn(Optional.of(deposit));

            assertThatThrownBy(() -> depositService.deleteDeposit(user, goal.getId(), deposit.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "DEPOSIT_NOT_FOUND");
        }
    }
}
