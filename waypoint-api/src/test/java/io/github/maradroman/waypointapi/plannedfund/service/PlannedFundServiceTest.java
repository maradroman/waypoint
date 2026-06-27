package io.github.maradroman.waypointapi.plannedfund.service;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalEntity.buildGoal;
import static io.github.maradroman.waypointapi.testdata.TestDataPlannedFundDto.upsertPlannedFundRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataPlannedFundEntity.buildPlannedFund;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.plannedfund.dto.PlannedFundResponse;
import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
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
class PlannedFundServiceTest {

    @Mock
    private PlannedFundRepository plannedFundRepository;

    @Mock
    private GoalService goalService;

    @InjectMocks
    private PlannedFundService plannedFundService;

    private final User user = buildUser();

    @Nested
    @DisplayName("ListPlannedFunds")
    class ListPlannedFunds {

        @Test
        void listPlannedFunds_returnsOnlyFuturePlannedFundsTest() {
            var goal = buildGoal(user);
            var plannedFund = buildPlannedFund(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(any(), any()))
                    .thenReturn(List.of(plannedFund));

            var actualResult = plannedFundService.listPlannedFunds(user, goal.getId());

            assertThat(actualResult)
                    .hasSize(1)
                    .extracting(PlannedFundResponse::id, PlannedFundResponse::amount)
                    .containsExactly(tuple(plannedFund.getId(), plannedFund.getAmount()));
        }

        @Test
        void listPlannedFunds_filtersOutDeletedFundsTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(any(), any()))
                    .thenReturn(List.of());

            var actualResult = plannedFundService.listPlannedFunds(user, goal.getId());

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("UpsertPlannedFund")
    class UpsertPlannedFund {

        @Test
        void upsertPlannedFund_createsNewPlannedFundTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndDate(goal.getId(), PLANNED_FUND_DATE))
                    .thenReturn(Optional.empty());
            when(plannedFundRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = upsertPlannedFundRequest(PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT);
            var actualResult = plannedFundService.upsertPlannedFund(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(PlannedFundResponse::amount, PlannedFundResponse::date)
                    .containsExactly(PLANNED_FUND_AMOUNT, PLANNED_FUND_DATE.toString());
        }

        @Test
        void upsertPlannedFund_updatesExistingPlannedFundTest() {
            var goal = buildGoal(user);
            var existingPlannedFund = buildPlannedFund(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndDate(goal.getId(), PLANNED_FUND_DATE))
                    .thenReturn(Optional.of(existingPlannedFund));
            when(plannedFundRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = upsertPlannedFundRequest(PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT_2);
            var actualResult = plannedFundService.upsertPlannedFund(user, goal.getId(), request);

            assertThat(actualResult).extracting(PlannedFundResponse::amount).isEqualTo(PLANNED_FUND_AMOUNT_2);
        }

        @Test
        void upsertPlannedFund_undeletesDeletedPlannedFundTest() {
            var goal = buildGoal(user);
            var deletedPlannedFund = buildPlannedFund(goal);
            deletedPlannedFund.setIsDeleted(true);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndDate(goal.getId(), PLANNED_FUND_DATE))
                    .thenReturn(Optional.of(deletedPlannedFund));
            when(plannedFundRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = upsertPlannedFundRequest(PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT);
            plannedFundService.upsertPlannedFund(user, goal.getId(), request);

            verify(plannedFundRepository).save(any());
            assertThat(deletedPlannedFund.getIsDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("DeletePlannedFund")
    class DeletePlannedFund {

        @Test
        void deletePlannedFund_softDeletesPlannedFundTest() {
            var goal = buildGoal(user);
            var plannedFund = buildPlannedFund(goal);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndDateAndIsDeletedFalse(goal.getId(), PLANNED_FUND_DATE))
                    .thenReturn(Optional.of(plannedFund));

            plannedFundService.deletePlannedFund(user, goal.getId(), PLANNED_FUND_DATE);

            verify(plannedFundRepository).save(plannedFund);
            assertThat(plannedFund.getIsDeleted()).isTrue();
        }

        @Test
        void deletePlannedFund_doesNothingWhenNotFoundTest() {
            var goal = buildGoal(user);
            when(goalService.findGoalForUser(user, goal.getId())).thenReturn(goal);
            when(plannedFundRepository.findByGoalIdAndDateAndIsDeletedFalse(goal.getId(), PLANNED_FUND_DATE))
                    .thenReturn(Optional.empty());

            plannedFundService.deletePlannedFund(user, goal.getId(), PLANNED_FUND_DATE);

            verify(plannedFundRepository).findByGoalIdAndDateAndIsDeletedFalse(goal.getId(), PLANNED_FUND_DATE);
        }
    }
}
