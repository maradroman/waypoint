package io.github.maradroman.waypointapi.goal.service;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ICON;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.createGoalRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.reorderGoalsRequest;
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
import io.github.maradroman.waypointapi.goal.dto.GoalResponse;
import io.github.maradroman.waypointapi.goal.dto.UpdateGoalRequest;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.repository.GoalRepository;
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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalService goalService;

    private final User user = buildUser();
    private final User otherUser = buildUser(USER_ID_2);

    @Nested
    @DisplayName("ListGoals")
    class ListGoals {

        @Test
        void listGoals_returnsGoalsSortedBySortOrderTest() {
            var goal1 = buildGoal(GOAL_ID, user, GOAL_TITLE);
            var goal2 = buildGoal(GOAL_ID_2, user, GOAL_TITLE_2);
            when(goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())).thenReturn(List.of(goal1, goal2));

            var actualResult = goalService.listGoals(user);

            assertThat(actualResult)
                    .hasSize(2)
                    .extracting(GoalResponse::id, GoalResponse::title)
                    .containsExactly(tuple(goal1.getId(), goal1.getTitle()), tuple(goal2.getId(), goal2.getTitle()));
        }

        @Test
        void listGoals_returnsEmptyListWhenNoGoalsTest() {
            when(goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())).thenReturn(List.of());

            var actualResult = goalService.listGoals(user);

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("CreateGoal")
    class CreateGoal {

        @Test
        void createGoal_savesAndReturnsGoalTest() {
            var request = createGoalRequest();
            when(goalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = goalService.createGoal(user, request);

            assertThat(actualResult)
                    .extracting(GoalResponse::title, GoalResponse::description, GoalResponse::icon)
                    .containsExactly(GOAL_TITLE, GOAL_DESCRIPTION, GOAL_ICON);
        }

        @Test
        void createGoal_usesDefaultDescriptionAndIconWhenNullTest() {
            var request = new io.github.maradroman.waypointapi.goal.dto.CreateGoalRequest(GOAL_TITLE, null, null);
            when(goalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var actualResult = goalService.createGoal(user, request);

            assertThat(actualResult)
                    .extracting(GoalResponse::title, GoalResponse::description, GoalResponse::icon)
                    .containsExactly(GOAL_TITLE, "", "target");
        }
    }

    @Nested
    @DisplayName("GetGoal")
    class GetGoal {

        @Test
        void getGoal_returnsGoalWhenFoundAndOwnedByUserTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

            var actualResult = goalService.getGoal(user, goal.getId());

            assertThat(actualResult)
                    .extracting(GoalResponse::id, GoalResponse::title)
                    .containsExactly(goal.getId(), goal.getTitle());
        }

        @Test
        void getGoal_throwsResourceNotFoundExceptionWhenNotFoundTest() {
            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.getGoal(user, GOAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }

        @Test
        void getGoal_throwsResourceNotFoundExceptionWhenNotOwnedByUserTest() {
            var goal = buildGoal(otherUser);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

            assertThatThrownBy(() -> goalService.getGoal(user, goal.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("UpdateGoal")
    class UpdateGoal {

        @Test
        void updateGoal_updatesTitleDescriptionAndIconSelectivelyTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
            when(goalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = new UpdateGoalRequest("New Title", null, null);
            var actualResult = goalService.updateGoal(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(GoalResponse::title, GoalResponse::description, GoalResponse::icon)
                    .containsExactly("New Title", GOAL_DESCRIPTION, GOAL_ICON);
        }

        @Test
        void updateGoal_updatesDescriptionTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
            when(goalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = new UpdateGoalRequest(null, "New description", null);
            var actualResult = goalService.updateGoal(user, goal.getId(), request);

            assertThat(actualResult)
                    .extracting(GoalResponse::title, GoalResponse::description)
                    .containsExactly(GOAL_TITLE, "New description");
        }

        @Test
        void updateGoal_updatesIconTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
            when(goalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var request = new UpdateGoalRequest(null, null, "piggy-bank");
            var actualResult = goalService.updateGoal(user, goal.getId(), request);

            assertThat(actualResult).extracting(GoalResponse::icon).isEqualTo("piggy-bank");
        }
    }

    @Nested
    @DisplayName("DeleteGoal")
    class DeleteGoal {

        @Test
        void deleteGoal_deletesGoalWhenOwnedByUserTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

            goalService.deleteGoal(user, goal.getId());

            verify(goalRepository).delete(goal);
        }
    }

    @Nested
    @DisplayName("ReorderGoals")
    class ReorderGoals {

        @Test
        void reorderGoals_updatesSortOrdersAccordingToProvidedIdsTest() {
            var goal1 = buildGoal(GOAL_ID, user, GOAL_TITLE);
            var goal2 = buildGoal(GOAL_ID_2, user, GOAL_TITLE_2);
            when(goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())).thenReturn(List.of(goal1, goal2));

            var request = reorderGoalsRequest(GOAL_ID_2, GOAL_ID);
            var actualResult = goalService.reorderGoals(user, request);

            assertThat(actualResult)
                    .extracting(GoalResponse::id, GoalResponse::sortOrder)
                    .containsExactly(tuple(GOAL_ID, 1), tuple(GOAL_ID_2, 0));
            verify(goalRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("FindGoalForUser")
    class FindGoalForUser {

        @Test
        void findGoalForUser_returnsGoalWhenOwnedByUserTest() {
            var goal = buildGoal(user);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

            var actualResult = goalService.findGoalForUser(user, goal.getId());

            assertThat(actualResult)
                    .extracting(Goal::getId, Goal::getTitle)
                    .containsExactly(goal.getId(), goal.getTitle());
        }

        @Test
        void findGoalForUser_throwsResourceNotFoundExceptionWhenNotFoundTest() {
            when(goalRepository.findById(GOAL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.findGoalForUser(user, GOAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }

        @Test
        void findGoalForUser_throwsResourceNotFoundExceptionWhenOwnedByDifferentUserTest() {
            var goal = buildGoal(otherUser);
            when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));

            assertThatThrownBy(() -> goalService.findGoalForUser(user, goal.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasFieldOrPropertyWithValue("code", "GOAL_NOT_FOUND");
        }
    }
}
