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
import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final PlannedFundRepository plannedFundRepository;

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
        List<Milestone> enabledMilestones = milestones.stream()
                .filter(Milestone::getEnabled)
                .toList();
        List<UUID> enabledMilestoneIds = enabledMilestones.stream()
                .map(Milestone::getId)
                .toList();

        long completedCount = enabledMilestones.stream().filter(Milestone::getCompleted).count();
        int totalMilestoneCost = enabledMilestones.stream().mapToInt(Milestone::getCost).sum();
        int totalMilestoneBalance = transferRepository.findByGoalId(goalId).stream()
                .filter(t -> t.getAmount() > 0 && enabledMilestoneIds.contains(t.getMilestone().getId()))
                .mapToInt(Transfer::getAmount)
                .sum();

        int progressPercent = totalMilestoneCost > 0
                ? (int) ((long) totalMilestoneBalance * 100 / totalMilestoneCost)
                : 0;

        Milestone activeMilestone = enabledMilestones.stream()
                .filter(m -> !m.getCompleted())
                .findFirst()
                .orElse(null);

        // Calculate potential completion date based on planned funds
        LocalDate potentialCompletionDate = calculatePotentialCompletionDate(
                goalId, walletBalance, totalMilestoneCost);

        return new GoalAnalyticsResponse(
                goalId,
                totalDeposited,
                totalTransferred,
                walletBalance,
                totalMilestoneCost,
                totalMilestoneBalance,
                (int) completedCount,
                enabledMilestones.size(),
                progressPercent,
                activeMilestone != null ? activeMilestone.getId() : null,
                activeMilestone != null ? activeMilestone.getTitle() : null,
                potentialCompletionDate
        );
    }

    private LocalDate calculatePotentialCompletionDate(UUID goalId, int currentBalance, int targetAmount) {
        if (targetAmount <= 0) {
            return null;
        }

        // If already completed, return null
        if (currentBalance >= targetAmount) {
            return null;
        }

        LocalDate today = LocalDate.now();
        List<PlannedFund> plannedFunds = plannedFundRepository
                .findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(goalId, today);

        if (plannedFunds.isEmpty()) {
            return null;
        }

        int projectedBalance = currentBalance;
        for (PlannedFund fund : plannedFunds) {
            projectedBalance += fund.getAmount();
            if (projectedBalance >= targetAmount) {
                return fund.getDate();
            }
        }

        // Not enough planned funds to reach target
        return null;
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
