package io.github.maradroman.waypointapi.plannedfund.scheduler;

import io.github.maradroman.waypointapi.plannedfund.repository.PlannedFundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlannedFundCleanupScheduler {

    private final PlannedFundRepository plannedFundRepository;

    /**
     * Runs on the first day of every month at 2:00 AM
     * Hard deletes all planned funds with dates in the past
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void cleanupPastPlannedFunds() {
        log.info("Starting cleanup of past planned funds");

        LocalDate today = LocalDate.now();
        int deletedCount = plannedFundRepository.hardDeletePastPlannedFunds(today);

        log.info("Completed cleanup: {} past planned funds permanently deleted", deletedCount);
    }
}
