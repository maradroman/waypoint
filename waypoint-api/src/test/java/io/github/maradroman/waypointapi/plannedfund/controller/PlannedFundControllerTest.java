package io.github.maradroman.waypointapi.plannedfund.controller;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.*;
import static io.github.maradroman.waypointapi.testdata.TestDataPlannedFundDto.plannedFundResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataPlannedFundDto.upsertPlannedFundRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.plannedfund.service.PlannedFundService;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(PlannedFundController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class PlannedFundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private PlannedFundService plannedFundService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        User mockUser = TestDataUserEntity.buildUser();
        lenient().when(currentUserResolver.supportsParameter(any())).thenReturn(true);
        lenient()
                .when(currentUserResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    @Test
    @DisplayName("GET /goals/{goalId}/planned-funds returns 200 with planned funds list")
    void listPlannedFunds_returns200WithPlannedFundsListTest() throws Exception {
        when(plannedFundService.listPlannedFunds(any(), any())).thenReturn(List.of(plannedFundResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/planned-funds", GOAL_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(PLANNED_FUND_ID.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(PLANNED_FUND_AMOUNT))
                .andExpect(jsonPath("$.data[0].date").value(PLANNED_FUND_DATE.toString()))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PUT /goals/{goalId}/planned-funds/{date} returns 200 with upserted planned fund")
    void upsertPlannedFund_returns200WithUpsertedPlannedFundTest() throws Exception {
        when(plannedFundService.upsertPlannedFund(any(), any(), any()))
                .thenReturn(plannedFundResponse(PLANNED_FUND_ID, GOAL_ID, PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT_2));

        var actualResult = mockMvc.perform(put("/goals/{goalId}/planned-funds/{date}", GOAL_ID, PLANNED_FUND_DATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        upsertPlannedFundRequest(PLANNED_FUND_DATE, PLANNED_FUND_AMOUNT_2))));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(PLANNED_FUND_ID.toString()))
                .andExpect(jsonPath("$.data.amount").value(PLANNED_FUND_AMOUNT_2))
                .andExpect(jsonPath("$.data.date").value(PLANNED_FUND_DATE.toString()))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PUT /goals/{goalId}/planned-funds/{date} returns 400 for past date")
    void upsertPlannedFund_returns400ForPastDateTest() throws Exception {
        var actualResult = mockMvc.perform(put("/goals/{goalId}/planned-funds/{date}", GOAL_ID, PAST_DATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upsertPlannedFundRequest(PAST_DATE, PLANNED_FUND_AMOUNT))));

        actualResult.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /goals/{goalId}/planned-funds/{date} returns 204")
    void deletePlannedFund_returns204Test() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{goalId}/planned-funds/{date}", GOAL_ID, PLANNED_FUND_DATE));

        actualResult.andExpect(status().isNoContent());
        verify(plannedFundService).deletePlannedFund(any(), any(), any());
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
