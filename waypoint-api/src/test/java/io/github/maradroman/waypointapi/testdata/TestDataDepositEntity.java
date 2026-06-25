package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.deposit.model.Deposit;
import io.github.maradroman.waypointapi.goal.model.Goal;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataDepositEntity {

    public static Deposit buildDeposit(Goal goal) {
        return Deposit.builder()
                .id(DEPOSIT_ID)
                .goal(goal)
                .amount(DEPOSIT_AMOUNT)
                .note(DEPOSIT_NOTE)
                .timestamp(DEFAULT_TIMESTAMP)
                .build();
    }

    public static Deposit buildDeposit(UUID id, Goal goal, int amount, Instant timestamp) {
        return Deposit.builder()
                .id(id)
                .goal(goal)
                .amount(amount)
                .note(DEPOSIT_NOTE)
                .timestamp(timestamp)
                .build();
    }
}
