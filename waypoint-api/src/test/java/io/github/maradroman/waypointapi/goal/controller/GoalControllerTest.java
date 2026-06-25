package io.github.maradroman.waypointapi.goal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.goal.dto.GoalResponse;
import io.github.maradroman.waypointapi.goal.service.GoalService;
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
import java.util.UUID;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_DESCRIPTION;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.createGoalRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.goalResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.reorderGoalsRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataGoalDto.updateGoalRequest;
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

@WebMvcTest(GoalController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private GoalService goalService;

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
    @DisplayName("GET /goals returns 200 with list of goals")
    void listGoals_returns200_whenAuthenticatedTest() throws Exception {
        when(goalService.listGoals(any())).thenReturn(List.of(goalResponse()));

        var actualResult = mockMvc.perform(get("/goals"));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(GOAL_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value(GOAL_TITLE))
                .andExpect(jsonPath("$.data[0].description").value(GOAL_DESCRIPTION))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals returns 201 with created goal")
    void createGoal_returns201_whenValidTest() throws Exception {
        when(goalService.createGoal(any(), any())).thenReturn(goalResponse());

        var actualResult = mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGoalRequest())));

        actualResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(GOAL_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(GOAL_TITLE))
                .andExpect(jsonPath("$.data.description").value(GOAL_DESCRIPTION))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("GET /goals/{id} returns 200 with goal")
    void getGoal_returns200_whenGoalExistsTest() throws Exception {
        when(goalService.getGoal(any(), any())).thenReturn(goalResponse());

        var actualResult = mockMvc.perform(get("/goals/{id}", GOAL_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(GOAL_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(GOAL_TITLE))
                .andExpect(jsonPath("$.data.description").value(GOAL_DESCRIPTION))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{id} returns 200 with updated goal")
    void updateGoal_returns200_whenValidTest() throws Exception {
        when(goalService.updateGoal(any(), any(), any())).thenReturn(goalResponse());

        var actualResult = mockMvc.perform(patch("/goals/{id}", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateGoalRequest("Updated Title"))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(GOAL_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(GOAL_TITLE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("DELETE /goals/{id} returns 204")
    void deleteGoal_returns204_whenGoalExistsTest() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{id}", GOAL_ID));

        actualResult.andExpect(status().isNoContent());
        verify(goalService).deleteGoal(any(), any());
    }

    @Test
    @DisplayName("PATCH /goals/reorder returns 200 with reordered goals")
    void reorderGoals_returns200_whenValidTest() throws Exception {
        var reordered = List.of(
                goalResponse(GOAL_ID, GOAL_TITLE, null),
                goalResponse(GOAL_ID_2, "Vacation", null)
        );
        when(goalService.reorderGoals(any(), any())).thenReturn(reordered);

        var actualResult = mockMvc.perform(patch("/goals/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderGoalsRequest(GOAL_ID, GOAL_ID_2))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(GOAL_ID.toString()))
                .andExpect(jsonPath("$.data[1].id").value(GOAL_ID_2.toString()))
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
