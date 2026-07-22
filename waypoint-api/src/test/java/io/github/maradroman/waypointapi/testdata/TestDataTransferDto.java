package io.github.maradroman.waypointapi.testdata;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;

import io.github.maradroman.waypointapi.transfer.dto.AllocateRequest;
import io.github.maradroman.waypointapi.transfer.dto.AllocateResponse;
import io.github.maradroman.waypointapi.transfer.dto.TransferResponse;
import io.github.maradroman.waypointapi.transfer.dto.UpdateTransferRequest;
import io.github.maradroman.waypointapi.transfer.dto.WithdrawRequest;
import java.time.Instant;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataTransferDto {

    public static TransferResponse transferResponse() {
        return new TransferResponse(
                TRANSFER_ID,
                GOAL_ID,
                MILESTONE_ID,
                TRANSFER_AMOUNT,
                TRANSFER_TYPE_ALLOCATE,
                "",
                DEFAULT_TIMESTAMP,
                DEFAULT_TIMESTAMP);
    }

    public static TransferResponse transferResponse(UUID id, int amount, String type, Instant timestamp) {
        return new TransferResponse(id, GOAL_ID, MILESTONE_ID, amount, type, "", timestamp, timestamp);
    }

    public static AllocateRequest allocateRequest(UUID milestoneId, int amount) {
        return new AllocateRequest(milestoneId, amount);
    }

    public static WithdrawRequest withdrawRequest(UUID milestoneId, int amount) {
        return new WithdrawRequest(milestoneId, amount);
    }

    public static UpdateTransferRequest updateTransferRequest(int amount) {
        return new UpdateTransferRequest(amount);
    }

    public static AllocateResponse allocateResponse(int applied, int requested) {
        return new AllocateResponse(applied, requested);
    }
}
