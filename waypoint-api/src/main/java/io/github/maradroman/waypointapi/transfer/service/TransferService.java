package io.github.maradroman.waypointapi.transfer.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.BadRequestException;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.deposit.repository.DepositRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
import io.github.maradroman.waypointapi.transfer.dto.AllocateRequest;
import io.github.maradroman.waypointapi.transfer.dto.AllocateResponse;
import io.github.maradroman.waypointapi.transfer.dto.TransferResponse;
import io.github.maradroman.waypointapi.transfer.dto.UpdateTransferRequest;
import io.github.maradroman.waypointapi.transfer.dto.WithdrawRequest;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final DepositRepository depositRepository;
    private final GoalService goalService;
    private final MilestoneService milestoneService;

    @Transactional(readOnly = true)
    public List<TransferResponse> listTransfers(User user, UUID goalId) {
        goalService.findGoalForUser(user, goalId);
        return transferRepository.findByGoalIdOrderByTimestampDesc(goalId).stream()
                .map(TransferResponse::from)
                .toList();
    }

    public AllocateResponse allocate(User user, UUID goalId, AllocateRequest request) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Milestone milestone = milestoneService.findMilestoneForUser(user, goalId, request.milestoneId());

        int walletBalance = computeWalletBalance(goalId);
        int milestoneBalance = computeMilestoneBalance(request.milestoneId());
        int remainingNeed = milestone.getCost() - milestoneBalance;
        int allowed = Math.min(walletBalance, Math.min(remainingNeed, request.amount()));

        if (allowed <= 0) {
            throw new BadRequestException(
                    "ALLOCATION_FAILED",
                    "Cannot allocate: insufficient wallet balance or milestone fully funded",
                    new AllocateResponse(0, request.amount()));
        }

        Transfer transfer = Transfer.builder()
                .goal(goal)
                .milestone(milestone)
                .amount(allowed)
                .type("allocate")
                .comment("")
                .timestamp(Instant.now())
                .build();
        transferRepository.save(transfer);

        return new AllocateResponse(allowed, request.amount());
    }

    public AllocateResponse withdraw(User user, UUID goalId, WithdrawRequest request) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Milestone milestone = milestoneService.findMilestoneForUser(user, goalId, request.milestoneId());

        int milestoneBalance = computeMilestoneBalance(request.milestoneId());
        int allowed = Math.min(milestoneBalance, request.amount());

        if (allowed <= 0) {
            throw new BadRequestException(
                    "WITHDRAWAL_FAILED",
                    "Cannot withdraw: milestone has no allocated funds",
                    new AllocateResponse(0, request.amount()));
        }

        Transfer transfer = Transfer.builder()
                .goal(goal)
                .milestone(milestone)
                .amount(-allowed)
                .type("withdraw")
                .comment("")
                .timestamp(Instant.now())
                .build();
        transferRepository.save(transfer);

        return new AllocateResponse(allowed, request.amount());
    }

    public TransferResponse updateTransfer(User user, UUID goalId, UUID transferId, UpdateTransferRequest request) {
        Transfer transfer = findTransferForUser(user, goalId, transferId);
        transfer.setAmount(request.amount());
        transfer = transferRepository.save(transfer);
        return TransferResponse.from(transfer);
    }

    public void deleteTransfer(User user, UUID goalId, UUID transferId) {
        Transfer transfer = findTransferForUser(user, goalId, transferId);
        transferRepository.delete(transfer);
    }

    private int computeWalletBalance(UUID goalId) {
        int totalDeposits = depositRepository.findByGoalId(goalId).stream()
                .mapToInt(Deposit::getAmount)
                .sum();
        int totalTransfers = transferRepository.findByGoalId(goalId).stream()
                .mapToInt(Transfer::getAmount)
                .sum();
        return totalDeposits - totalTransfers;
    }

    private int computeMilestoneBalance(UUID milestoneId) {
        return transferRepository.findByMilestoneIdOrderByTimestampDesc(milestoneId).stream()
                .mapToInt(Transfer::getAmount)
                .sum();
    }

    private Transfer findTransferForUser(User user, UUID goalId, UUID transferId) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Transfer transfer = transferRepository
                .findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("TRANSFER_NOT_FOUND", "Transfer not found"));
        if (!transfer.getGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("TRANSFER_NOT_FOUND", "Transfer not found");
        }
        return transfer;
    }
}
