package io.github.maradroman.waypointapi.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.service.AuthService;
import io.github.maradroman.waypointapi.auth.service.JwtService;
import io.github.maradroman.waypointapi.common.security.CurrentUserResolver;
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

import static io.github.maradroman.waypointapi.testdata.TestDataAuthDto.*;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_DISPLAY_NAME;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_EMAIL;
import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AuthService authService;

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
    @DisplayName("POST /auth/register returns 201 with AuthResponse")
    void register_returns201_whenValidTest() throws Exception {
        when(authService.register(any())).thenReturn(authResponse());

        var actualResult = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest())));

        actualResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.user.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.user.displayName").value(USER_DISPLAY_NAME))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.requestId").exists())
                .andExpect(jsonPath("$.meta.timestamp").exists());
    }

    @Test
    @DisplayName("POST /auth/login returns 200 with AuthResponse")
    void login_returns200_whenValidTest() throws Exception {
        when(authService.login(any())).thenReturn(authResponse());

        var actualResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest())));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.user.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.user.displayName").value(USER_DISPLAY_NAME))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /auth/refresh returns 200 with AuthResponse")
    void refresh_returns200_whenValidTest() throws Exception {
        when(authService.refresh(any())).thenReturn(authResponse());

        var actualResult = mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest())));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("POST /auth/logout returns 204")
    void logout_returns204_whenAuthenticatedTest() throws Exception {
        var actualResult = mockMvc.perform(post("/auth/logout"));

        actualResult.andExpect(status().isNoContent());
        verify(authService).logout(any());
    }

    @Test
    @DisplayName("GET /auth/me returns 200 with UserProfile")
    void me_returns200_whenAuthenticatedTest() throws Exception {
        when(authService.getProfile(any())).thenReturn(authResponse().user());

        var actualResult = mockMvc.perform(get("/auth/me"));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.displayName").value(USER_DISPLAY_NAME))
                .andExpect(jsonPath("$.meta").exists());
    }

    @Test
    @DisplayName("PATCH /auth/me returns 200 with updated UserProfile")
    void updateMe_returns200_whenAuthenticatedTest() throws Exception {
        var expectedProfile = authResponse().user();
        when(authService.updateProfile(any(), any())).thenReturn(expectedProfile);

        var actualResult = mockMvc.perform(patch("/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileRequest("Alice Updated"))));

        actualResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.displayName").value(USER_DISPLAY_NAME))
                .andExpect(jsonPath("$.data.locale").value("en"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
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
