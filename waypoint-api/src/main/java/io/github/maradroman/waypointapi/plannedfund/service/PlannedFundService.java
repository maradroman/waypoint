package io.github.maradroman.waypointapi.plannedfund.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.plannedfund.dto.PlannedFundResponse;
import io.github.maradroman.waypointapi.plannedfund.dto.UpsertPlannedFundRequest;
import io.github.maradroman.waypointapi.plannedfund.model.PlannedFund;
import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PlannedFundService {

    private final PlannedFundRepository plannedFundRepository;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public List<PlannedFundResponse> listPlannedFunds(User user, UUID goalId) {
        goalService.findGoalForUser(user, goalId);
        LocalDate today = LocalDate.now();
        return plannedFundRepository.findByGoalIdAndIsDeletedFalseAndDateGreaterThanEqualOrderByDateAsc(goalId, today)
                .stream()
                .map(PlannedFundResponse::from)
                .toList();
    }

    public PlannedFundResponse upsertPlannedFund(User user, UUID goalId, UpsertPlannedFundRequest request) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        LocalDate date = LocalDate.parse(request.date());

        PlannedFund plannedFund = plannedFundRepository.findByGoalIdAndDate(goalId, date)
                .orElseGet(() -> PlannedFund.builder()
                        .goal(goal)
                        .date(date)
                        .build());

        plannedFund.setAmount(request.amount());
        plannedFund.setIsDeleted(false); // Undelete if it was previously deleted
        plannedFund = plannedFundRepository.save(plannedFund);
        return PlannedFundResponse.from(plannedFund);
    }

    public void deletePlannedFund(User user, UUID goalId, LocalDate date) {
        goalService.findGoalForUser(user, goalId);
        plannedFundRepository.findByGoalIdAndDateAndIsDeletedFalse(goalId, date)
                .ifPresent(plannedFund -> {
                    plannedFund.setIsDeleted(true);
                    plannedFundRepository.save(plannedFund);
                });
    }
}
