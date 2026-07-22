package io.github.maradroman.waypointapi.deposit.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.deposit.dto.CreateDepositRequest;
import io.github.maradroman.waypointapi.deposit.dto.DepositResponse;
import io.github.maradroman.waypointapi.deposit.dto.UpdateDepositRequest;
import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public List<DepositResponse> listDeposits(User user, UUID goalId) {
        goalService.findGoalForUser(user, goalId);
        return depositRepository.findByGoalIdOrderByTimestampDesc(goalId).stream()
                .map(DepositResponse::from)
                .toList();
    }

    public DepositResponse createDeposit(User user, UUID goalId, CreateDepositRequest request) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Deposit deposit = Deposit.builder()
                .goal(goal)
                .amount(request.amount())
                .note(request.note() != null ? request.note() : "")
                .timestamp(Instant.now())
                .build();
        deposit = depositRepository.save(deposit);
        return DepositResponse.from(deposit);
    }

    public DepositResponse updateDeposit(User user, UUID goalId, UUID depositId, UpdateDepositRequest request) {
        Deposit deposit = findDepositForUser(user, goalId, depositId);
        deposit.setAmount(request.amount());
        deposit = depositRepository.save(deposit);
        return DepositResponse.from(deposit);
    }

    public void deleteDeposit(User user, UUID goalId, UUID depositId) {
        Deposit deposit = findDepositForUser(user, goalId, depositId);
        depositRepository.delete(deposit);
    }

    private Deposit findDepositForUser(User user, UUID goalId, UUID depositId) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Deposit deposit = depositRepository
                .findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("DEPOSIT_NOT_FOUND", "Deposit not found"));
        if (!deposit.getGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("DEPOSIT_NOT_FOUND", "Deposit not found");
        }
        return deposit;
    }
}
