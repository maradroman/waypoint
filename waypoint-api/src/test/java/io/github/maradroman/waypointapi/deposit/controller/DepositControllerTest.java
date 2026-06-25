package io.github.maradroman.waypointapi.deposit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.deposit.service.DepositService;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
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

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEFAULT_TIMESTAMP;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.DEPOSIT_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositDto.createDepositRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositDto.depositResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataDepositDto.updateDepositRequest;
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

@WebMvcTest(DepositController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private DepositService depositService;

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
    @DisplayName("GET /goals/{goalId}/deposits returns 200 with deposit list")
    void listDeposits_returns200WithDepositsListTest() throws Exception {
        when(depositService.listDeposits(any(), any())).thenReturn(List.of(depositResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/deposits", GOAL_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(DEPOSIT_ID.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(DEPOSIT_AMOUNT))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/deposits returns 201 with created deposit")
    void createDeposit_returns201WithCreatedDepositTest() throws Exception {
        when(depositService.createDeposit(any(), any(), any()))
                .thenReturn(depositResponse(DEPOSIT_ID, 75000, DEFAULT_TIMESTAMP));

        var actualResult = mockMvc.perform(post("/goals/{goalId}/deposits", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDepositRequest(75000))));

        actualResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(DEPOSIT_ID.toString()))
                .andExpect(jsonPath("$.data.amount").value(75000))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/deposits/{id} returns 200 with updated deposit")
    void updateDeposit_returns200WithUpdatedDepositTest() throws Exception {
        when(depositService.updateDeposit(any(), any(), any(), any()))
                .thenReturn(depositResponse(DEPOSIT_ID, 75000, DEFAULT_TIMESTAMP));

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/deposits/{id}", GOAL_ID, DEPOSIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDepositRequest(75000))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(DEPOSIT_ID.toString()))
                .andExpect(jsonPath("$.data.amount").value(75000))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("DELETE /goals/{goalId}/deposits/{id} returns 204")
    void deleteDeposit_returns204Test() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{goalId}/deposits/{id}", GOAL_ID, DEPOSIT_ID));

        actualResult.andExpect(status().isNoContent());
        verify(depositService).deleteDeposit(any(), any(), any());
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
