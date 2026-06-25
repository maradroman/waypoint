package io.github.maradroman.waypointapi.completion.controller;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
import io.github.maradroman.waypointapi.completion.service.CompletionService;
import io.github.maradroman.waypointapi.testdata.TestDataUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.COMPLETION_AMOUNT;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.COMPLETION_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.GOAL_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataCompletionDto.completionResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompletionController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class CompletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        lenient().when(currentUserResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    @Test
    @DisplayName("GET /goals/{goalId}/completions returns 200 with completion list")
    void listCompletions_returns200WithCompletionsListTest() throws Exception {
        when(completionService.listCompletions(any(), any())).thenReturn(List.of(completionResponse()));

        var actualResult = mockMvc.perform(get("/goals/{goalId}/completions", GOAL_ID));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(COMPLETION_ID.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(COMPLETION_AMOUNT))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("DELETE /goals/{goalId}/completions/{id} returns 204")
    void deleteCompletion_returns204Test() throws Exception {
        var actualResult = mockMvc.perform(delete("/goals/{goalId}/completions/{id}", GOAL_ID, COMPLETION_ID));

        actualResult.andExpect(status().isNoContent());
        verify(completionService).deleteCompletion(any(), any(), any());
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
