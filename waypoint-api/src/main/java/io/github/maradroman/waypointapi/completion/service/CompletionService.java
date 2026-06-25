package io.github.maradroman.waypointapi.completion.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.completion.dto.CompletionResponse;
import io.github.maradroman.waypointapi.completion.model.Completion;
import io.github.maradroman.waypointapi.completion.repository.CompletionRepository;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
import io.github.maradroman.waypointapi.transfer.model.Transfer;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CompletionService {

    private final CompletionRepository completionRepository;
    private final TransferRepository transferRepository;
    private final GoalService goalService;
    private final MilestoneService milestoneService;

    @Transactional(readOnly = true)
    public List<CompletionResponse> listCompletions(User user, UUID goalId) {
        goalService.findGoalForUser(user, goalId);
        return completionRepository.findByGoalIdOrderByTimestampDesc(goalId)
                .stream()
                .map(CompletionResponse::from)
                .toList();
    }

    public void deleteCompletion(User user, UUID goalId, UUID completionId) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Completion completion = completionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException("COMPLETION_NOT_FOUND", "Completion not found"));
        if (!completion.getGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("COMPLETION_NOT_FOUND", "Completion not found");
        }

        Milestone milestone = completion.getMilestone();
        milestone.setCompleted(false);
        milestone.setCompletedAt(null);

        completionRepository.delete(completion);
    }

    public CompletionResponse createCompletion(User user, UUID goalId, UUID milestoneId) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Milestone milestone = milestoneService.findMilestoneForUser(user, goalId, milestoneId);

        int milestoneBalance = transferRepository.findByMilestoneIdOrderByTimestampDesc(milestoneId).stream()
                .mapToInt(Transfer::getAmount)
                .sum();

        Completion completion = Completion.builder()
                .goal(goal)
                .milestone(milestone)
                .amount(milestoneBalance)
                .timestamp(Instant.now())
                .build();
        completion = completionRepository.save(completion);

        milestone.setCompleted(true);
        milestone.setCompletedAt(completion.getTimestamp());

        return CompletionResponse.from(completion);
    }
}
