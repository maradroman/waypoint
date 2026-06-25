package io.github.maradroman.waypointapi.transfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
import io.github.maradroman.waypointapi.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_TYPE_ALLOCATE;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.allocateRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.allocateResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.transferResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.updateTransferRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.withdrawRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        User mockUser = TestDataUserEntity.buildUser();
        lenient().when(currentUserResolver.supportsParameter(any())).thenReturn(true);
        lenient().when(currentUserResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    @Test
    @DisplayName("GET /goals/{goalId}/transfers returns 200 with transfer list")
    void listTransfers_returns200WithTransfersListTest() throws Exception {
        when(transferService.listTransfers(any(), any())).thenReturn(List.of(transferResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/transfers", GOAL_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(TRANSFER_ID.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(TRANSFER_AMOUNT))
                .andExpect(jsonPath("$.data[0].type").value(TRANSFER_TYPE_ALLOCATE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/transfers/allocate returns 200 with allocation result")
    void allocate_returns200WithAllocateResponseTest() throws Exception {
        when(transferService.allocate(any(), any(), any())).thenReturn(allocateResponse(10000, 10000));

        var actualResult = mockMvc.perform(post("/goals/{goalId}/transfers/allocate", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(allocateRequest(MILESTONE_ID, 10000))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.applied").value(10000))
                .andExpect(jsonPath("$.data.requested").value(10000))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/transfers/withdraw returns 200 with withdrawal result")
    void withdraw_returns200WithAllocateResponseTest() throws Exception {
        when(transferService.withdraw(any(), any(), any())).thenReturn(allocateResponse(5000, 5000));

        var actualResult = mockMvc.perform(post("/goals/{goalId}/transfers/withdraw", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest(MILESTONE_ID, 5000))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.applied").value(5000))
                .andExpect(jsonPath("$.data.requested").value(5000))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/transfers/{id} returns 200 with updated transfer")
    void updateTransfer_returns200WithUpdatedTransferTest() throws Exception {
        when(transferService.updateTransfer(any(), any(), any(), any()))
                .thenReturn(transferResponse(TRANSFER_ID, 75000, TRANSFER_TYPE_ALLOCATE, DEFAULT_TIMESTAMP));

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/transfers/{id}", GOAL_ID, TRANSFER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransferRequest(75000))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(TRANSFER_ID.toString()))
                .andExpect(jsonPath("$.data.amount").value(75000))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("DELETE /goals/{goalId}/transfers/{id} returns 204")
    void deleteTransfer_returns204Test() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{goalId}/transfers/{id}", GOAL_ID, TRANSFER_ID));

        actualResult.andExpect(status().isNoContent());
        verify(transferService).deleteTransfer(any(), any(), any());
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {

        @Autowired
        private CurrentUserResolver currentUserResolver;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentUserResolver);
        }
    }
}
