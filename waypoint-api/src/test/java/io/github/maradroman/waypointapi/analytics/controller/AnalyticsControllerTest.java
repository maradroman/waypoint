package io.github.maradroman.waypointapi.analytics.controller;

import static io.github.maradroman.waypointapi.testdata.TestDataAnalyticsDto.goalAnalyticsResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataAnalyticsDto.summaryResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.maradroman.waypointapi.analytics.service.AnalyticsService;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(AnalyticsController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

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
    @DisplayName("GET /goals/{id}/analytics returns 200 with goal analytics")
    void getGoalAnalytics_returns200WithGoalAnalyticsTest() throws Exception {
        when(analyticsService.getGoalAnalytics(any(), any())).thenReturn(goalAnalyticsResponse());

        var actualResult = mockMvc.perform(get("/goals/{id}/analytics", GOAL_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalDeposited").value(50000))
                .andExpect(jsonPath("$.data.totalAllocated").value(10000))
                .andExpect(jsonPath("$.data.walletBalance").value(40000))
                .andExpect(jsonPath("$.data.progressPercent").value(10))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /analytics/summary returns 200 with user summary")
    void getSummary_returns200WithSummaryTest() throws Exception {
        when(analyticsService.getSummary(any())).thenReturn(summaryResponse());

        var actualResult = mockMvc.perform(get("/analytics/summary"));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalSaved").value(150000))
                .andExpect(jsonPath("$.data.totalTargets").value(500000))
                .andExpect(jsonPath("$.data.activeGoals").value(3))
                .andExpect(jsonPath("$.data.completedMilestones").value(5))
                .andExpect(jsonPath("$.meta").exists());
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
