package io.github.maradroman.waypointapi.goal.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.goal.dto.CreateGoalRequest;
import io.github.maradroman.waypointapi.goal.dto.GoalResponse;
import io.github.maradroman.waypointapi.goal.dto.ReorderGoalsRequest;
import io.github.maradroman.waypointapi.goal.dto.UpdateGoalRequest;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    @Transactional(readOnly = true)
    public List<GoalResponse> listGoals(User user) {
        return goalRepository.findByUserIdOrderBySortOrderAsc(user.getId())
                .stream()
                .map(GoalResponse::from)
                .toList();
    }

    public GoalResponse createGoal(User user, CreateGoalRequest request) {
        Goal goal = Goal.builder()
                .user(user)
                .title(request.title())
                .description(request.description() != null ? request.description() : "")
                .icon(request.icon() != null ? request.icon() : "target")
                .build();
        goal = goalRepository.save(goal);
        return GoalResponse.from(goal);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(User user, UUID goalId) {
        Goal goal = findGoalForUser(user, goalId);
        return GoalResponse.from(goal);
    }

    public GoalResponse updateGoal(User user, UUID goalId, UpdateGoalRequest request) {
        Goal goal = findGoalForUser(user, goalId);
        if (request.title() != null) {
            goal.setTitle(request.title());
        }
        if (request.description() != null) {
            goal.setDescription(request.description());
        }
        if (request.icon() != null) {
            goal.setIcon(request.icon());
        }
        goal = goalRepository.save(goal);
        return GoalResponse.from(goal);
    }

    public void deleteGoal(User user, UUID goalId) {
        Goal goal = findGoalForUser(user, goalId);
        goalRepository.delete(goal);
    }

    public List<GoalResponse> reorderGoals(User user, ReorderGoalsRequest request) {
        List<Goal> goals = goalRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        for (int i = 0; i < request.goalIds().size(); i++) {
            UUID id = request.goalIds().get(i);
            int order = i;
            goals.stream()
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .ifPresent(g -> g.setSortOrder(order));
        }
        goalRepository.saveAll(goals);
        return goals.stream().map(GoalResponse::from).toList();
    }

    public Goal findGoalForUser(User user, UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("GOAL_NOT_FOUND", "Goal not found");
        }
        return goal;
    }
}
