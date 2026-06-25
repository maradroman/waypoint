package io.github.maradroman.waypointapi.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound returns 404 with error envelope")
    void handleNotFound_returns404() {
        var ex = new ResourceNotFoundException("GOAL_NOT_FOUND", "Goal not found", "details");
        var response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("GOAL_NOT_FOUND");
        assertThat(response.getBody().error().message()).isEqualTo("Goal not found");
        assertThat(response.getBody().error().details()).isEqualTo("details");
    }

    @Test
    @DisplayName("handleBadRequest returns 400 with error envelope")
    void handleBadRequest_returns400() {
        var ex = new BadRequestException("INVALID_INPUT", "Invalid input", "field error");
        var response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT");
        assertThat(response.getBody().error().details()).isEqualTo("field error");
    }

    @Test
    @DisplayName("handleDuplicate returns 409 with error envelope")
    void handleDuplicate_returns409() {
        var ex = new DuplicateResourceException("DUPLICATE_EMAIL", "Email already exists");
        var response = handler.handleDuplicate(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("DUPLICATE_EMAIL");
    }

    @Test
    @DisplayName("handleBadCredentials returns 401")
    void handleBadCredentials_returns401() {
        var ex = new BadCredentialsException("bad creds");
        var response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().error().message()).isEqualTo("Invalid email or password");
    }

    @Test
    @DisplayName("handleUserNotFound returns 401")
    void handleUserNotFound_returns401() {
        var ex = new UsernameNotFoundException("user not found");
        var response = handler.handleUserNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assert response.getBody() != null;
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().error().message()).isEqualTo("Invalid email or password");
    }

    @Test
    @DisplayName("handleValidation returns 400 with field errors")
    void handleValidation_returns400WithFieldErrors() {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "email", "must be valid"));
        bindingResult.addError(new FieldError("target", "password", "too short"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assert response.getBody() != null;
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().error().details()).isInstanceOf(java.util.Map.class);
        var errors = (java.util.Map<String, String>) response.getBody().error().details();
        assertThat(errors).containsEntry("email", "must be valid");
        assertThat(errors).containsEntry("password", "too short");
    }

    @Test
    @DisplayName("handleValidation handles field with null default message")
    void handleValidation_handlesNullDefaultMessage() {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "field", null, false, null, null, null));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        var errors = (java.util.Map<String, String>) response.getBody().error().details();
        assertThat(errors).containsEntry("field", "Invalid value");
    }

    @Test
    @DisplayName("handleValidation merges duplicate field errors by keeping last")
    void handleValidation_mergesDuplicateFieldErrors() {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "email", "first error"));
        bindingResult.addError(new FieldError("target", "email", "second error"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        var errors = (java.util.Map<String, String>) response.getBody().error().details();
        assertThat(errors).containsEntry("email", "second error");
    }

    @Test
    @DisplayName("handleGeneral returns 500 with error envelope")
    void handleGeneral_returns500() {
        var ex = new RuntimeException("Unexpected");
        var response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().error().message()).isEqualTo("An unexpected error occurred");
    }
}
