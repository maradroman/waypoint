package io.github.maradroman.waypointapi.milestone.service;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import io.github.maradroman.waypointapi.goal.model.Goal;
import io.github.maradroman.waypointapi.goal.service.GoalService;
import io.github.maradroman.waypointapi.milestone.dto.CreateMilestoneRequest;
import io.github.maradroman.waypointapi.milestone.dto.MilestoneResponse;
import io.github.maradroman.waypointapi.milestone.dto.ReorderMilestonesRequest;
import io.github.maradroman.waypointapi.milestone.dto.UpdateMilestoneRequest;
import io.github.maradroman.waypointapi.milestone.model.Milestone;
import io.github.maradroman.waypointapi.milestone.repository.MilestoneRepository;
import io.github.maradroman.waypointapi.transfer.dto.TransferResponse;
import io.github.maradroman.waypointapi.transfer.repository.TransferRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final TransferRepository transferRepository;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public List<MilestoneResponse> listMilestones(User user, UUID goalId) {
        goalService.findGoalForUser(user, goalId);
        List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySortOrderAsc(goalId);
        Map<UUID, Integer> balances = computeBalances(goalId);
        return milestones.stream()
                .map(m -> MilestoneResponse.from(m, balances.getOrDefault(m.getId(), 0)))
                .toList();
    }

    public MilestoneResponse createMilestone(User user, UUID goalId, CreateMilestoneRequest request) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Milestone milestone = Milestone.builder()
                .goal(goal)
                .title(request.title())
                .cost(request.cost() != null ? request.cost() : 0)
                .details(request.details() != null ? request.details() : "")
                .enabled(request.enabled() != null ? request.enabled() : true)
                .build();
        milestone = milestoneRepository.save(milestone);
        return MilestoneResponse.from(milestone, 0);
    }

    public MilestoneResponse updateMilestone(User user, UUID goalId, UUID milestoneId, UpdateMilestoneRequest request) {
        Milestone milestone = findMilestoneForUser(user, goalId, milestoneId);
        if (request.title() != null) {
            milestone.setTitle(request.title());
        }
        if (request.cost() != null) {
            milestone.setCost(request.cost());
        }
        if (request.details() != null) {
            milestone.setDetails(request.details());
        }
        if (request.enabled() != null) {
            milestone.setEnabled(request.enabled());
        }
        milestone = milestoneRepository.save(milestone);
        int balance = computeBalance(goalId, milestoneId);
        return MilestoneResponse.from(milestone, balance);
    }

    public void deleteMilestone(User user, UUID goalId, UUID milestoneId) {
        Milestone milestone = findMilestoneForUser(user, goalId, milestoneId);
        milestoneRepository.delete(milestone);
    }

    public MilestoneResponse uncompleteMilestone(User user, UUID goalId, UUID milestoneId) {
        Milestone milestone = findMilestoneForUser(user, goalId, milestoneId);
        if (!milestone.getCompleted()) {
            throw new IllegalStateException("Milestone is not completed");
        }
        milestone.setCompleted(false);
        milestone.setCompletedAt(null);
        milestone = milestoneRepository.save(milestone);
        int balance = computeBalance(goalId, milestoneId);
        return MilestoneResponse.from(milestone, balance);
    }

    public MilestoneResponse toggleMilestone(User user, UUID goalId, UUID milestoneId) {
        Milestone milestone = findMilestoneForUser(user, goalId, milestoneId);
        milestone.setEnabled(!milestone.getEnabled());
        milestone = milestoneRepository.save(milestone);
        int balance = computeBalance(goalId, milestoneId);
        return MilestoneResponse.from(milestone, balance);
    }

    public List<MilestoneResponse> reorderMilestones(User user, UUID goalId, ReorderMilestonesRequest request) {
        goalService.findGoalForUser(user, goalId);
        List<Milestone> milestones = milestoneRepository.findByGoalId(goalId);
        Map<UUID, Integer> balances = computeBalances(goalId);
        for (int i = 0; i < request.milestoneIds().size(); i++) {
            UUID id = request.milestoneIds().get(i);
            int order = i;
            milestones.stream().filter(m -> m.getId().equals(id)).findFirst().ifPresent(m -> m.setSortOrder(order));
        }
        milestoneRepository.saveAll(milestones);
        return milestones.stream()
                .map(m -> MilestoneResponse.from(m, balances.getOrDefault(m.getId(), 0)))
                .toList();
    }

    public List<MilestoneResponse> toggleAllMilestones(User user, UUID goalId, boolean enabled) {
        goalService.findGoalForUser(user, goalId);
        List<Milestone> milestones = milestoneRepository.findByGoalId(goalId);
        Map<UUID, Integer> balances = computeBalances(goalId);
        for (Milestone m : milestones) {
            m.setEnabled(enabled);
        }
        milestoneRepository.saveAll(milestones);
        return milestones.stream()
                .map(m -> MilestoneResponse.from(m, balances.getOrDefault(m.getId(), 0)))
                .toList();
    }

    public MilestoneResponse getMilestoneWithBalance(User user, UUID goalId, UUID milestoneId) {
        Milestone milestone = findMilestoneForUser(user, goalId, milestoneId);
        int balance = computeBalance(goalId, milestoneId);
        return MilestoneResponse.from(milestone, balance);
    }

    public List<TransferResponse> listMilestoneTransfers(User user, UUID goalId, UUID milestoneId) {
        findMilestoneForUser(user, goalId, milestoneId);
        return transferRepository.findByMilestoneIdOrderByTimestampDesc(milestoneId).stream()
                .map(TransferResponse::from)
                .toList();
    }

    public Milestone findMilestoneForUser(User user, UUID goalId, UUID milestoneId) {
        Goal goal = goalService.findGoalForUser(user, goalId);
        Milestone milestone = milestoneRepository
                .findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("MILESTONE_NOT_FOUND", "Milestone not found"));
        if (!milestone.getGoal().getId().equals(goal.getId())) {
            throw new ResourceNotFoundException("MILESTONE_NOT_FOUND", "Milestone not found");
        }
        return milestone;
    }

    private Map<UUID, Integer> computeBalances(UUID goalId) {
        return transferRepository.findByGoalId(goalId).stream()
                .collect(Collectors.groupingBy(
                        t -> t.getMilestone().getId(),
                        Collectors.summingInt(io.github.maradroman.waypointapi.transfer.model.Transfer::getAmount)));
    }

    private int computeBalance(UUID goalId, UUID milestoneId) {
        return transferRepository.findByMilestoneIdOrderByTimestampDesc(milestoneId).stream()
                .mapToInt(io.github.maradroman.waypointapi.transfer.model.Transfer::getAmount)
                .sum();
    }
}
