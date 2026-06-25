package io.github.maradroman.waypointapi.testdata;

import io.github.maradroman.waypointapi.deposit.dto.CreateDepositRequest;
import io.github.maradroman.waypointapi.deposit.dto.DepositResponse;
import io.github.maradroman.waypointapi.deposit.dto.UpdateDepositRequest;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

@UtilityClass
public class TestDataDepositDto {

    public static DepositResponse depositResponse() {
        return new DepositResponse(DEPOSIT_ID, GOAL_ID, DEPOSIT_AMOUNT, DEPOSIT_NOTE,
                DEFAULT_TIMESTAMP, DEFAULT_TIMESTAMP);
    }

    public static DepositResponse depositResponse(UUID id, int amount, Instant timestamp) {
        return new DepositResponse(id, GOAL_ID, amount, DEPOSIT_NOTE, timestamp, timestamp);
    }

    public static CreateDepositRequest createDepositRequest(int amount) {
        return new CreateDepositRequest(amount, DEPOSIT_NOTE);
    }

    public static UpdateDepositRequest updateDepositRequest(int amount) {
        return new UpdateDepositRequest(amount);
    }
}
