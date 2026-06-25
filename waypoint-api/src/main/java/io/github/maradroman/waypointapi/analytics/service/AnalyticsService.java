package io.github.maradroman.waypointapi.analytics.service;

import io.github.maradroman.waypointapi.analytics.dto.GoalAnalyticsResponse;
import io.github.maradroman.waypointapi.analytics.dto.SummaryResponse;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.completion.repository.CompletionRepository;
import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.repository.GoalRepository;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.repository.MilestoneRepository;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final DepositRepository depositRepository;
    private final TransferRepository transferRepository;

    public GoalAnalyticsResponse getGoalAnalytics(User user, UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException(
                        "GOAL_NOT_FOUND", "Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException(
                    "GOAL_NOT_FOUND", "Goal not found");
        }

        int totalDeposited = depositRepository.findByGoalId(goalId).stream()
                .mapToInt(Deposit::getAmount)
                .sum();

        int totalTransferred = transferRepository.findByGoalId(goalId).stream()
                .mapToInt(Transfer::getAmount)
                .sum();
        int walletBalance = totalDeposited - totalTransferred;

        List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySortOrderAsc(goalId);
        long completedCount = milestones.stream().filter(Milestone::getCompleted).count();
        int totalMilestoneCost = milestones.stream().mapToInt(Milestone::getCost).sum();
        int totalMilestoneBalance = transferRepository.findByGoalId(goalId).stream()
                .filter(t -> t.getAmount() > 0)
                .mapToInt(Transfer::getAmount)
                .sum();

        int progressPercent = totalMilestoneCost > 0
                ? (int) ((long) totalMilestoneBalance * 100 / totalMilestoneCost)
                : 0;

        Milestone activeMilestone = milestones.stream()
                .filter(m -> !m.getCompleted() && m.getEnabled())
                .findFirst()
                .orElse(null);

        return new GoalAnalyticsResponse(
                goalId,
                totalDeposited,
                totalTransferred,
                walletBalance,
                totalMilestoneCost,
                totalMilestoneBalance,
                (int) completedCount,
                milestones.size(),
                progressPercent,
                activeMilestone != null ? activeMilestone.getId() : null,
                activeMilestone != null ? activeMilestone.getTitle() : null
        );
    }

    public SummaryResponse getSummary(User user) {
        List<Goal> goals = goalRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        int activeGoals = goals.size();
        int totalSaved = 0;
        int completedMilestones = 0;

        for (Goal goal : goals) {
            totalSaved += depositRepository.findByGoalId(goal.getId()).stream()
                    .mapToInt(Deposit::getAmount)
                    .sum();
            completedMilestones += (int) milestoneRepository.findByGoalId(goal.getId()).stream()
                    .filter(Milestone::getCompleted)
                    .count();
        }

        int totalTargets = goals.stream()
                .flatMap(g -> milestoneRepository.findByGoalId(g.getId()).stream())
                .mapToInt(Milestone::getCost)
                .sum();

        return new SummaryResponse(totalSaved, totalTargets, activeGoals, completedMilestones);
    }
}
