package io.github.maradroman.waypointapi.milestone.controller;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_COST;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_ID_2;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.MILESTONE_TITLE;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.TRANSFER_TYPE_ALLOCATE;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.createMilestoneRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.milestoneResponse;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.reorderMilestonesRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataMilestoneDto.updateMilestoneRequest;
import static io.github.maradroman.waypointapi.testdata.TestDataTransferDto.transferResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.completion.service.CompletionService;
import io.github.maradroman.waypointapi.milestone.service.MilestoneService;
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

@WebMvcTest(MilestoneController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class MilestoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private MilestoneService milestoneService;

    @MockitoBean
    private CompletionService completionService;

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
    @DisplayName("GET /goals/{goalId}/milestones returns 200 with list of milestones")
    void listMilestones_returns200_whenAuthenticatedTest() throws Exception {
        when(milestoneService.listMilestones(any(), any())).thenReturn(List.of(milestoneResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/milestones", GOAL_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.data[0].cost").value(MILESTONE_COST))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/milestones returns 201 with created milestone")
    void createMilestone_returns201_whenValidTest() throws Exception {
        when(milestoneService.createMilestone(any(), any(), any())).thenReturn(milestoneResponse());

        var actualResult = mockMvc.perform(post("/goals/{goalId}/milestones", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMilestoneRequest())));

        actualResult
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.data.cost").value(MILESTONE_COST))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/milestones/{id} returns 200 with updated milestone")
    void updateMilestone_returns200_whenValidTest() throws Exception {
        when(milestoneService.updateMilestone(any(), any(), any(), any())).thenReturn(milestoneResponse());

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/milestones/{id}", GOAL_ID, MILESTONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateMilestoneRequest("Updated Title", 200000))));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("DELETE /goals/{goalId}/milestones/{id} returns 204")
    void deleteMilestone_returns204_whenMilestoneExistsTest() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{goalId}/milestones/{id}", GOAL_ID, MILESTONE_ID));

        actualResult.andExpect(status().isNoContent());
        verify(milestoneService).deleteMilestone(any(), any(), any());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/milestones/{id}/complete returns 200 with completed milestone")
    void completeMilestone_returns200_whenValidTest() throws Exception {
        when(milestoneService.getMilestoneWithBalance(any(), any(), any())).thenReturn(milestoneResponse());

        var actualResult = mockMvc.perform(post("/goals/{goalId}/milestones/{id}/complete", GOAL_ID, MILESTONE_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.meta").exists());
        verify(completionService).createCompletion(any(), any(), any());
    }

    @Test
    @DisplayName("GET /goals/{goalId}/milestones/{id}/transfers returns 200 with list of transfers")
    void listMilestoneTransfers_returns200_whenAuthenticatedTest() throws Exception {
        when(milestoneService.listMilestoneTransfers(any(), any(), any())).thenReturn(List.of(transferResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/milestones/{id}/transfers", GOAL_ID, MILESTONE_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(TRANSFER_ID.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(TRANSFER_AMOUNT))
                .andExpect(jsonPath("$.data[0].type").value(TRANSFER_TYPE_ALLOCATE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /goals/{goalId}/milestones/{id}/uncomplete returns 200 with uncompleted milestone")
    void uncompleteMilestone_returns200_whenValidTest() throws Exception {
        when(milestoneService.uncompleteMilestone(any(), any(), any())).thenReturn(milestoneResponse());

        var actualResult = mockMvc.perform(post("/goals/{goalId}/milestones/{id}/uncomplete", GOAL_ID, MILESTONE_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/milestones/{id}/toggle returns 200 with toggled milestone")
    void toggleMilestone_returns200_whenValidTest() throws Exception {
        when(milestoneService.toggleMilestone(any(), any(), any())).thenReturn(milestoneResponse());

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/milestones/{id}/toggle", GOAL_ID, MILESTONE_ID));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data.title").value(MILESTONE_TITLE))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/milestones/reorder returns 200 with reordered milestones")
    void reorderMilestones_returns200_whenValidTest() throws Exception {
        var reordered = List.of(
                milestoneResponse(MILESTONE_ID, MILESTONE_TITLE, 0, MILESTONE_COST),
                milestoneResponse(MILESTONE_ID_2, "Second $5000", 0, 500000));
        when(milestoneService.reorderMilestones(any(), any(), any())).thenReturn(reordered);

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/milestones/reorder", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderMilestonesRequest(MILESTONE_ID, MILESTONE_ID_2))));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data[1].id").value(MILESTONE_ID_2.toString()))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /goals/{goalId}/milestones/toggle-all returns 200 when enabled=true")
    void toggleAllMilestones_returns200_whenEnabledTrueTest() throws Exception {
        var toggled = List.of(
                milestoneResponse(MILESTONE_ID, MILESTONE_TITLE, 0, MILESTONE_COST),
                milestoneResponse(MILESTONE_ID_2, "Second $5000", 0, 500000));
        when(milestoneService.toggleAllMilestones(any(), any(), eq(true))).thenReturn(toggled);

        var actualResult = mockMvc.perform(patch("/goals/{goalId}/milestones/toggle-all", GOAL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\": true}"));

        actualResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(MILESTONE_ID.toString()))
                .andExpect(jsonPath("$.data[1].id").value(MILESTONE_ID_2.toString()))
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
