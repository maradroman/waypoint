package io.github.maradroman.waypointapi.common.security;

import static io.github.maradroman.waypointapi.testdata.TestDataConstant.USER_ID;
import static io.github.maradroman.waypointapi.testdata.TestDataUserEntity.buildUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.github.maradroman.waypointapi.auth.model.User;
import io.github.maradroman.waypointapi.auth.repository.UserRepository;
import io.github.maradroman.waypointapi.common.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class CurrentUserResolverTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CurrentUserResolver resolver;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("supportsParameter returns true when parameter has @CurrentUser and type is User")
    void supportsParameter_returnsTrueWhenAnnotatedAndUserType() {
        when(methodParameter.hasParameterAnnotation(CurrentUser.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) User.class);

        assertThat(resolver.supportsParameter(methodParameter)).isTrue();
    }

    @Test
    @DisplayName("supportsParameter returns false when parameter lacks @CurrentUser")
    void supportsParameter_returnsFalseWhenMissingAnnotation() {
        when(methodParameter.hasParameterAnnotation(CurrentUser.class)).thenReturn(false);

        assertThat(resolver.supportsParameter(methodParameter)).isFalse();
    }

    @Test
    @DisplayName("supportsParameter returns false when parameter has wrong type")
    void supportsParameter_returnsFalseWhenWrongType() {
        when(methodParameter.hasParameterAnnotation(CurrentUser.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) String.class);

        assertThat(resolver.supportsParameter(methodParameter)).isFalse();
    }

    @Test
    @DisplayName("resolveArgument returns null when authentication is null")
    void resolveArgument_returnsNullWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        var result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolveArgument returns null when not authenticated")
    void resolveArgument_returnsNullWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        var result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolveArgument returns user when authenticated and found")
    void resolveArgument_returnsUserWhenAuthenticated() {
        var user = buildUser();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        var result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isSameAs(user);
    }

    @Test
    @DisplayName("resolveArgument throws when user not found")
    void resolveArgument_throwsWhenUserNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(unknownId.toString());
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }
}
